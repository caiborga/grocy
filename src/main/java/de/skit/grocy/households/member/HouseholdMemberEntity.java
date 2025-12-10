package de.skit.grocy.households.member;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.user.UserEntity;
import de.skit.grocy.households.HouseholdEntity;

@Entity
public class HouseholdMemberEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String role = "MEMBER";

    @Column(nullable = false)
    private OffsetDateTime joinedAt = OffsetDateTime.now();

    public HouseholdMemberEntity() {}

    // ---------- Getter ----------

    public UUID getId() {
        return id;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getRole() {
        return role;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    // ---------- Setter ----------

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
