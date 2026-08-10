package de.skit.grocy.activity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.lists.ListEntity;
import de.skit.grocy.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "list_activity_events")
public class ListActivityEventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private HouseholdEntity household;

    @ManyToOne(optional = false)
    @JoinColumn(name = "list_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ListEntity list;

    @Column(nullable = false)
    private String listTitle;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserEntity actor;

    @Column(nullable = false)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ListActivityType type;

    private UUID itemId;

    private String itemTitle;

    private Integer metaCount;

    private String metaText;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public HouseholdEntity getHousehold() {
        return household;
    }

    public ListEntity getList() {
        return list;
    }

    public String getListTitle() {
        return listTitle;
    }

    public UserEntity getActor() {
        return actor;
    }

    public String getActorName() {
        return actorName;
    }

    public ListActivityType getType() {
        return type;
    }

    public UUID getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public Integer getMetaCount() {
        return metaCount;
    }

    public String getMetaText() {
        return metaText;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setHousehold(HouseholdEntity household) {
        this.household = household;
    }

    public void setList(ListEntity list) {
        this.list = list;
    }

    public void setListTitle(String listTitle) {
        this.listTitle = listTitle;
    }

    public void setActor(UserEntity actor) {
        this.actor = actor;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public void setType(ListActivityType type) {
        this.type = type;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public void setMetaCount(Integer metaCount) {
        this.metaCount = metaCount;
    }

    public void setMetaText(String metaText) {
        this.metaText = metaText;
    }
}
