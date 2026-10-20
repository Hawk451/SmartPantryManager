package com.example.smartpantrymanager.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A recipe from the pre-loaded collection.
 * Its ingredients live in a separate table (RecipeIngredient).
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "description")
    private String description;

    /** Preparation steps, one per line ("1. Beat the eggs\n2. ..."). */
    @NonNull
    @ColumnInfo(name = "steps")
    private String steps;

    @ColumnInfo(name = "prep_minutes")
    private int prepMinutes;

    public Recipe(@NonNull String name, @NonNull String description,
                  @NonNull String steps, int prepMinutes) {
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.prepMinutes = prepMinutes;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getDescription() { return description; }
    public void setDescription(@NonNull String description) { this.description = description; }

    @NonNull public String getSteps() { return steps; }
    public void setSteps(@NonNull String steps) { this.steps = steps; }

    public int getPrepMinutes() { return prepMinutes; }
    public void setPrepMinutes(int prepMinutes) { this.prepMinutes = prepMinutes; }
}
