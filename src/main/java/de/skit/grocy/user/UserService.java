package de.skit.grocy.user;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.skit.grocy.common.NotFoundException;
import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.households.dto.HouseholdUpdate;
import de.skit.grocy.user.dto.UserCreate;
import de.skit.grocy.user.dto.UserResponse;
import de.skit.grocy.user.dto.UserUpdate;
import de.skit.grocy.user.mapper.UserMapper;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final UserMapper mapper;

    public UserService(UserRepository userRepository, PasswordEncoder encoder, UserMapper mapper) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.mapper = mapper;
    }

    public UserResponse createUser(UserCreate dto) {
        UserEntity entity = mapper.toEntity(dto);
        UserEntity saved = userRepository.save(entity);
        return mapper.toDto(saved);
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
}