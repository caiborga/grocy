package de.skit.grocy.items;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.user.UserEntity;

@Entity
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private ListEntity list;

    @ManyToOne(optional = false)
    private HouseholdEntity household;

    @ManyToOne(optional = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private String title;

    private Double quantity;
    private String unitText;
    private String brand;
    private UUID categoryId;
    private String notes;
    private UUID sourceRecipeId;
    private String sourceRecipeTitle;

    @Column(nullable = false)
    private boolean checked = false;

    @Column(nullable = false)
    private int sortIndex = 0;

    @Version
    private int version;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime checkedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // ---------- Getter ----------

    public UUID getId() {
        return id;
    }

    public ListEntity getList() {
        return list;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public String getTitle() {
        return title;
    }

    public Double getQuantity() {
        return quantity;
    }

    public String getUnitText() {
        return unitText;
    }

    public String getBrand() {
        return brand;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getNotes() {
        return notes;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public int getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getCheckedAt() {
        return checkedAt;
    }

    public UUID getSourceRecipeId() {
        return sourceRecipeId;
    }

    public String getSourceRecipeTitle() {
        return sourceRecipeTitle;
    }

    // ---------- Setter ----------

    public void setTitle(String title) {
        this.title = title;
    }

    public void setList(ListEntity list) {
        this.list = list;
    }

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public void setUnitText(String unitText) {
        this.unitText = unitText;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if (checked) {
            this.checkedAt = OffsetDateTime.now();
        } else {
            this.checkedAt = null;
        }
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setSourceRecipeId(UUID sourceRecipeId) {
        this.sourceRecipeId = sourceRecipeId;
    }

    public void setSourceRecipeTitle(String sourceRecipeTitle) {
        this.sourceRecipeTitle = sourceRecipeTitle;
    }
}