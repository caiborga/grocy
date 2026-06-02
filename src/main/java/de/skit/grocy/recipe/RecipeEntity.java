package de.skit.grocy.recipe;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.skit.grocy.households.HouseholdEntity;
import de.skit.grocy.recipe.ingredient.IngredientEntity;
import de.skit.grocy.recipe.step.StepEntity;
import de.skit.grocy.user.UserEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "recipes")
public class RecipeEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    private HouseholdEntity household;

    @ManyToOne(optional = false)
    private UserEntity createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private int baseServings;

    private Integer prepTimeMinutes;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IngredientEntity> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StepEntity> steps = new ArrayList<>();

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Version
    private int version;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
