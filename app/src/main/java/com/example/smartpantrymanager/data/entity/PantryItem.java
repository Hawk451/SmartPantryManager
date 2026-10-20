package com.example.smartpantrymanager.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One ingredient the user currently has at home.
 * This is the table the user performs full CRUD on.
 */
@Entity(tableName = "pantry_items",
        indices = {@Index(value = "normalized_name")})
public class PantryItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Name exactly as the user typed it, e.g. "Tomatoes" - used for display. */
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    /** Cleaned-up name used for matching, e.g. "tomato". */
    @NonNull
    @ColumnInfo(name = "normalized_name")
    private String normalizedName;

    @ColumnInfo(name = "quantity")
    private double quantity;

    /** e.g. "g", "kg", "ml", "L", "each". */
    @NonNull
    @ColumnInfo(name = "unit")
    private String unit;

    /** Optional expiry date stored as epoch milliseconds; null = no expiry set. */
    @Nullable
    @ColumnInfo(name = "expiry_date")
    private Long expiryDate;

    public PantryItem(@NonNull String name, @NonNull String normalizedName,
                      double quantity, @NonNull String unit, @Nullable Long expiryDate) {
        this.name = name;
        this.normalizedName = normalizedName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // --- Getters and setters (Room uses these to read/write private fields) ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull public String getName() { return name; }
    public void setName(@NonNull String name) { this.name = name; }

    @NonNull public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(@NonNull String normalizedName) { this.normalizedName = normalizedName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    @NonNull public String getUnit() { return unit; }
    public void setUnit(@NonNull String unit) { this.unit = unit; }

    @Nullable public Long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(@Nullable Long expiryDate) { this.expiryDate = expiryDate; }
}
