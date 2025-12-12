package de.skit.grocy.user.mapper;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.UserRepository;
import de.skit.grocy.user.dto.UserCreate;
import de.skit.grocy.user.dto.UserResponse;
import de.skit.grocy.user.dto.UserUpdate;

@Component
public class UserMapper {
    private final PasswordEncoder encoder;

    public UserMapper(UserRepository repository, PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public UserEntity toEntity(UserCreate dto) {

        UserEntity entity = new UserEntity();
        entity.setDisplayName(dto.displayName());
        entity.setEmail(dto.email());
        entity.setPassword(encoder.encode(dto.password()));
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setUpdatedAt(OffsetDateTime.now());
        return entity;
    }

    public UserResponse toDto(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public void applyPatch(UserUpdate patch, UserEntity entity) {
        if (patch.displayName() != null) {
            entity.setDisplayName(patch.displayName());
        }
        if (patch.email() != null) {
            entity.setEmail(patch.email());
        }
        if (patch.password() != null) {
            entity.setPassword(encoder.encode(patch.password()));
        }
    }
}
