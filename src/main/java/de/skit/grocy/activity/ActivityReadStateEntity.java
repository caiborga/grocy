package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "activity_read_states",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "household_id" }))
public class ActivityReadStateEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

    @Column(nullable = false)
    private OffsetDateTime lastReadAt;

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public OffsetDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public void setLastReadAt(OffsetDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
