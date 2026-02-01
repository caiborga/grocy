package de.skit.grocy.households.invites;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.common.enums.Role;
import de.skit.grocy.households.invites.enums.InviteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "household_invites")
public class HouseholdInviteEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID householdId;

    /**
     * SHA-256 (oder HMAC) Hash des Invite-Tokens
     */
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private int maxUses = 1;

    @Column(nullable = false)
    private int uses = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime acceptedAt;
    private UUID acceptedByUserId;

    // ---------- Lifecycle ----------

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.expiresAt == null) {
            this.expiresAt = OffsetDateTime.now().plusHours(2);
        }
    }

    // ---------- Domain Helper ----------

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsable() {
        return status == InviteStatus.PENDING && uses < maxUses && !isExpired();
    }

    // ---------- Getter / Setter ----------

    public UUID getId() {
        return id;
    }

    public UUID getHouseholdId() {
        return householdId;
    }

    public void setHouseholdId(UUID householdId) {
        this.householdId = householdId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(int maxUses) {
        this.maxUses = maxUses;
    }

    public int getUses() {
        return uses;
    }

    public void incrementUses() {
        this.uses++;
    }

    public InviteStatus getStatus() {
        return status;
    }

    public void setStatus(InviteStatus status) {
        this.status = status;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(UUID createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void markAccepted(UUID userId) {
        this.acceptedByUserId = userId;
        this.acceptedAt = OffsetDateTime.now();
        this.incrementUses();
        if (this.uses >= this.maxUses) {
            this.status = InviteStatus.ACCEPTED;
        }
    }
}
