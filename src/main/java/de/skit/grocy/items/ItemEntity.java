package de.skit.grocy.items;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID listId;

    @Column(nullable = false)
    private UUID householdId;

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private String title;

    private Double quantity;

    private String unitText;

    private UUID categoryId;

    @Column(nullable = false)
    private boolean checked = false;

    private String notes;

    @Column(nullable = false)
    private int sortIndex = 0;

    @Column(nullable = false)
    private int version = 0;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public ItemEntity() {
        // required by JPA
    }

    public ItemEntity(
            UUID listId,
            UUID householdId,
            UUID createdBy,
            String title,
            Double quantity,
            String unitText,
            UUID categoryId,
            boolean checked,
            String notes,
            int sortIndex,
            int version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt) {
        this.listId = listId;
        this.householdId = householdId;
        this.createdBy = createdBy;
        this.title = title;
        this.quantity = quantity;
        this.unitText = unitText;
        this.categoryId = categoryId;
        this.checked = checked;
        this.notes = notes;
        this.sortIndex = sortIndex;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ---------- Getter ----------

    public UUID getId() { return id; }
    public UUID getListId() { return listId; }
    public UUID getHouseholdId() { return householdId; }
    public UUID getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public Double getQuantity() { return quantity; }
    public String getUnitText() { return unitText; }
    public UUID getCategoryId() { return categoryId; }
    public boolean isChecked() { return checked; }
    public String getNotes() { return notes; }
    public int getSortIndex() { return sortIndex; }
    public int getVersion() { return version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // ---------- Setter ----------

    public void setTitle(String title) { this.title = title; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setUnitText(String unitText) { this.unitText = unitText; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSortIndex(int sortIndex) { this.sortIndex = sortIndex; }
    public void setVersion(int version) { this.version = version; }
    public void setChecked(boolean checked) { this.checked = checked; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
