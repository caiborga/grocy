package de.skit.grocy.lists;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.user.UserEntity;

@Entity
public class ListEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean archived = false;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    // FEHLER IN DEFINITION!
    @Version
    private int version;

    public ListEntity() {
    }

    @PrePersist
    public void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // ---------- Getter ----------

    public UUID getId() {
        return id;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public String getTitle() {
        return title;
    }

    public boolean getArchived() {
        return archived;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getVersion() {
        return version;
    }

    // ---------- Setter ----------

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    // Others

    public boolean isArchived() {
        return archived;
    }

}
