package de.skit.grocy.account;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountDeletionEventRepository extends JpaRepository<AccountDeletionEventEntity, UUID> {
}
