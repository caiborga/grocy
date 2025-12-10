package de.skit.grocy.households;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.skit.grocy.households.member.HouseholdMemberEntity;

@Entity
public class HouseholdEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Column(nullable = false)
    private boolean archived = false;

    @OneToMany(mappedBy = "household")
    private List<HouseholdMemberEntity> members = new ArrayList<>();

    public HouseholdEntity() {
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ---------- Getter ----------

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getArchived() {
        return archived;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<HouseholdMemberEntity> getMembers() {
        return members;
    }

    // ---------- Setter ----------

    public void setName(String name) {
        this.name = name;
    }

    public void addMember(HouseholdMemberEntity member) {
        members.add(member);
        member.setHousehold(this);
    }

    public void removeMember(HouseholdMemberEntity member) {
        members.remove(member);
        member.setHousehold(null);
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
