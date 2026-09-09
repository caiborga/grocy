package de.skit.grocy.admin;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.skit.grocy.account.AccountDeletionEventRepository;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.invites.HouseholdInviteRepository;
import de.skit.grocy.households.invites.enums.InviteStatus;
import de.skit.grocy.security.PlatformAdminService;
import de.skit.grocy.security.UserPrincipal;
import de.skit.grocy.user.UserRepository;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdInviteRepository inviteRepository;
    private final AccountDeletionEventRepository deletionEventRepository;
    private final PlatformAdminService platformAdminService;

    public AdminStatsService(
            UserRepository userRepository,
            HouseholdRepository householdRepository,
            HouseholdInviteRepository inviteRepository,
            AccountDeletionEventRepository deletionEventRepository,
            PlatformAdminService platformAdminService) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.inviteRepository = inviteRepository;
        this.deletionEventRepository = deletionEventRepository;
        this.platformAdminService = platformAdminService;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats(UserPrincipal principal) {
        platformAdminService.requireAdmin(principal.getUser());
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime sevenDays = now.minusDays(7);
        OffsetDateTime thirtyDays = now.minusDays(30);

        return new AdminStatsResponse(
                userRepository.countByDeletedAtIsNull(),
                userRepository.countByDeletedAtIsNullAndCreatedAtAfter(sevenDays),
                userRepository.countByDeletedAtIsNullAndCreatedAtAfter(thirtyDays),
                userRepository.countByDeletedAtIsNullAndLastLoginAtAfter(sevenDays),
                userRepository.countByDeletedAtIsNullAndLastLoginAtAfter(thirtyDays),
                householdRepository.count(),
                inviteRepository.countByStatusAndExpiresAtAfter(InviteStatus.PENDING, now),
                deletionEventRepository.count(),
                "Ein Nutzer gilt als aktiv, wenn lastLoginAt innerhalb des Zeitraums liegt.");
    }
}
