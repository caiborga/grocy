package de.skit.grocy.account;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.activity.ActivityReadStateRepository;
import de.skit.grocy.activity.ListActivityEventRepository;
import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.common.exceptions.OwnershipTransferRequiredException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.invites.HouseholdInviteRepository;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.items.ItemRepository;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.recipe.RecipeRepository;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.security.crypto.TokenHashes;
import de.skit.grocy.security.session.AuthSessionService;
import de.skit.grocy.security.token.AuthTokenService;
import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;

@Service
public class AccountService {

    public static final String ANONYMIZED_NAME = "Gelöschter Nutzer";

    private final UserRepository userRepository;
    private final HouseholdMemberRepository memberRepository;
    private final HouseholdRepository householdRepository;
    private final ListRepository listRepository;
    private final ItemRepository itemRepository;
    private final RecipeRepository recipeRepository;
    private final ListActivityEventRepository activityEventRepository;
    private final ActivityReadStateRepository activityReadStateRepository;
    private final HouseholdInviteRepository inviteRepository;
    private final AccountDeletionEventRepository deletionEventRepository;
    private final AuthTokenService authTokenService;
    private final AuthSessionService authSessionService;
    private final PasswordEncoder passwordEncoder;

    public AccountService(
            UserRepository userRepository,
            HouseholdMemberRepository memberRepository,
            HouseholdRepository householdRepository,
            ListRepository listRepository,
            ItemRepository itemRepository,
            RecipeRepository recipeRepository,
            ListActivityEventRepository activityEventRepository,
            ActivityReadStateRepository activityReadStateRepository,
            HouseholdInviteRepository inviteRepository,
            AccountDeletionEventRepository deletionEventRepository,
            AuthTokenService authTokenService,
            AuthSessionService authSessionService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.householdRepository = householdRepository;
        this.listRepository = listRepository;
        this.itemRepository = itemRepository;
        this.recipeRepository = recipeRepository;
        this.activityEventRepository = activityEventRepository;
        this.activityReadStateRepository = activityReadStateRepository;
        this.inviteRepository = inviteRepository;
        this.deletionEventRepository = deletionEventRepository;
        this.authTokenService = authTokenService;
        this.authSessionService = authSessionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> export(UserPrincipal principal) {
        UserEntity user = reload(principal);
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("exportedAt", OffsetDateTime.now().toString());
        export.put("account", Map.of(
                "id", user.getId(),
                "displayName", user.getName(),
                "email", user.getEmail(),
                "createdAt", String.valueOf(user.getCreatedAt()),
                "lastLoginAt", String.valueOf(user.getLastLoginAt()),
                "emailVerified", user.isEmailVerified()));

        List<Map<String, Object>> households = new ArrayList<>();
        for (HouseholdMemberEntity membership : memberRepository.findByUserId(user.getId())) {
            HouseholdEntity household = membership.getHousehold();
            Map<String, Object> householdExport = new LinkedHashMap<>();
            householdExport.put("id", household.getId());
            householdExport.put("name", household.getName());
            householdExport.put("role", membership.getRole().name());
            householdExport.put("lists", listRepository.findByHouseholdId(household.getId()).stream()
                    .map(list -> Map.of(
                            "id", list.getId(),
                            "title", list.getTitle(),
                            "archived", list.isArchived(),
                            "default", list.isDefault()))
                    .toList());
            householdExport.put("recipes", recipeRepository.findByHouseholdId(household.getId()).stream()
                    .map(recipe -> Map.of(
                            "id", recipe.getId(),
                            "title", recipe.getTitle(),
                            "description", recipe.getDescription() == null ? "" : recipe.getDescription()))
                    .toList());
            householdExport.put("members", memberRepository.findByHouseholdId(household.getId()).stream()
                    .map(member -> Map.of(
                            "displayName", member.getUser().getName(),
                            "role", member.getRole().name()))
                    .toList());
            households.add(householdExport);
        }
        export.put("households", households);
        return export;
    }

    @Transactional
    public void deleteAccount(UserPrincipal principal) {
        UserEntity user = reload(principal);
        List<HouseholdMemberEntity> memberships = memberRepository.findByUserId(user.getId());

        List<UUID> blocked = new ArrayList<>();
        for (HouseholdMemberEntity membership : memberships) {
            UUID householdId = membership.getHousehold().getId();
            long memberCount = memberRepository.countByHouseholdId(householdId);
            long ownerCount = memberRepository.countByHouseholdIdAndRole(householdId, Role.OWNER);
            if (membership.getRole() == Role.OWNER && ownerCount <= 1 && memberCount > 1) {
                blocked.add(householdId);
            }
        }
        if (!blocked.isEmpty()) {
            throw new OwnershipTransferRequiredException(blocked);
        }

        for (HouseholdMemberEntity membership : List.copyOf(memberships)) {
            UUID householdId = membership.getHousehold().getId();
            long memberCount = memberRepository.countByHouseholdId(householdId);
            if (memberCount <= 1) {
                deleteSoleHousehold(householdId);
            } else {
                memberRepository.deleteByHouseholdIdAndUserId(householdId, user.getId());
            }
        }

        activityEventRepository.anonymizeActorName(user.getId(), ANONYMIZED_NAME);
        activityReadStateRepository.deleteByUserId(user.getId());
        authTokenService.deleteAllForUser(user);
        authSessionService.deleteAll(user);

        user.setEmail("deleted-" + user.getId() + "@invalid.local");
        user.setDisplayName(ANONYMIZED_NAME);
        user.setPasswordHash(passwordEncoder.encode(TokenHashes.randomUrlToken()));
        user.setEmailVerified(false);
        user.setEmailVerifiedAt(null);
        user.setLastLoginAt(null);
        user.setActiveHouseholdId(null);
        user.setCredentialsValidFrom(OffsetDateTime.now());
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);

        AccountDeletionEventEntity event = new AccountDeletionEventEntity();
        event.setDeletedAt(OffsetDateTime.now());
        deletionEventRepository.save(event);
    }

    private void deleteSoleHousehold(UUID householdId) {
        itemRepository.deleteByHouseholdId(householdId);
        activityEventRepository.deleteByHouseholdId(householdId);
        activityReadStateRepository.deleteByHouseholdId(householdId);
        recipeRepository.deleteByHouseholdId(householdId);
        listRepository.deleteByHouseholdId(householdId);
        inviteRepository.deleteByHouseholdId(householdId);
        memberRepository.deleteByHouseholdId(householdId);
        householdRepository.deleteById(householdId);
    }

    private UserEntity reload(UserPrincipal principal) {
        return userRepository.findById(principal.getUser().getId())
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Nicht gefunden"));
    }
}
