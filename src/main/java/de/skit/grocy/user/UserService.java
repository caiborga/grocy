package de.skit.grocy.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.common.exceptions.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.HouseholdRepository;
import de.skit.grocy.households.member.HouseholdMemberEntity;
import de.skit.grocy.households.member.HouseholdMemberRepository;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.lists.ListRepository;
import de.skit.grocy.user.dto.UserCreate;
import de.skit.grocy.user.dto.UserResponse;
import de.skit.grocy.user.dto.UserUpdate;
import de.skit.grocy.user.mapper.UserMapper;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final ListRepository listRepository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, HouseholdRepository householdRepository,
            HouseholdMemberRepository householdMemberRepository, UserMapper mapper, PasswordEncoder encoder,
            ListRepository listRepository) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.listRepository = listRepository;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse createUser(UserCreate dto) {
        return mapper.toDto(createUserEntity(dto));
    }

    @Transactional
    public UserEntity createUserEntity(UserCreate dto) {
        String email = normalizeEmail(dto.email());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email ist bereits registriert");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setDisplayName(dto.displayName());
        user.setPasswordHash(encoder.encode(dto.password()));

        UserEntity savedUser = userRepository.save(user);

        HouseholdEntity household = new HouseholdEntity();
        household.setName("Neuer Haushalt");

        HouseholdEntity savedHousehold = householdRepository.save(household);

        savedUser.setActiveHouseholdId(savedHousehold.getId());

        HouseholdMemberEntity member = new HouseholdMemberEntity();
        member.setUser(savedUser);
        member.setHousehold(savedHousehold);
        member.setRole(Role.OWNER);

        householdMemberRepository.save(member);

        ListEntity defaultList = new ListEntity();
        defaultList.setTitle("Einkaufsliste");
        defaultList.setHousehold(savedHousehold);
        defaultList.setCreatedBy(savedUser);
        defaultList.setDefault(true);
        defaultList.setArchived(false);

        listRepository.save(defaultList);

        return savedUser;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public UserResponse findUser(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        return mapper.toDto(entity);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UserUpdate update) {

        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));

        mapper.applyPatch(update, entity);

        UserEntity updated = userRepository.save(entity);

        return mapper.toDto(updated);
    }

    public void deleteUser(UUID id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        userRepository.delete(entity);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
