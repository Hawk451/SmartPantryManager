package com.example.smartpantrymanager.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One ingredient (with quantity) required by a recipe.
 * Many RecipeIngredients belong to one Recipe (one-to-many).
 */
@Entity(tableName = "recipe_ingredients",
        foreignKeys = @ForeignKey(
                entity = Recipe.class,
                parentColumns = "id",
                childColumns = "recipe_id",
                onDelete = ForeignKey.CASCADE),   // deleting a recipe deletes its ingredients
        indices = {
                @Index(value = "recipe_id"),       // fast lookup of a recipe's ingredients
                @Index(value = "normalized_name")
        })
public class RecipeIngredient {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "recipe_id")
    private long recipeId;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "normalized_name")
    private String normalizedName;

    @ColumnInfo(name = "quantity")
    private double quantity;

    @NonNull
    @ColumnInfo(name = "unit")
    private String unit;

    public RecipeIngredient(long recipeId, @NonNull String name, @NonNull String normalizedName,
                            double quantity, @NonNull String unit) {
        this.recipeId = recipeId;
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getRecipeId() { return recipeId; }
    public void setRecipeId(long recipeId) { this.recipeId = recipeId; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(@NonNull String normalizedName) { this.normalizedName = normalizedName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    @NonNull public String getUnit() { return unit; }
    public void setUnit(@NonNull String unit) { this.unit = unit; }
}
