package de.skit.grocy.user.mapper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.skit.grocy.user.UserEntity;
import de.skit.grocy.user.dto.UserCreate;
import de.skit.grocy.user.dto.UserResponse;
import de.skit.grocy.user.dto.UserUpdate;

@Component
public class UserMapper {
    private final PasswordEncoder encoder;

    public UserMapper(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public UserEntity toEntity(UserCreate dto) {

        UserEntity entity = new UserEntity();
        entity.setDisplayName(dto.displayName());
        entity.setEmail(dto.email());
        entity.setPasswordHash(encoder.encode(dto.password()));
        return entity;
    }

    public UserResponse toDto(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getActiveHouseholdId()
            );
    }

    public void applyPatch(UserUpdate patch, UserEntity entity) {
        if (patch.displayName() != null) {
            entity.setDisplayName(patch.displayName());
        }
        if (patch.email() != null) {
            entity.setEmail(patch.email());
        }
        if (patch.password() != null) {
            entity.setPasswordHash(encoder.encode(patch.password()));
        }
    }
}
