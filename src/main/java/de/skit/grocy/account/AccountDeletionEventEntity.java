package de.skit.grocy.account;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_deletion_events")
public class AccountDeletionEventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime deletedAt = OffsetDateTime.now();

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
