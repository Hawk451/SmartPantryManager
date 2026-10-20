package com.example.smartpantrymanager.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.smartpantrymanager.data.entity.PantryItem;

import java.util.List;

/**
 * Full CRUD for the user's pantry.
 * Synchronous methods - always call via AppDatabase.databaseWriteExecutor.
 */
@Dao
public interface PantryItemDao {

    /** CREATE - returns the new row id. */
    @Insert
    long insert(PantryItem item);

    /** READ (list) - COLLATE NOCASE so "apple" and "Banana" sort alphabetically together. */
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE")
    List<PantryItem> getAll();

    /** READ (single) - used by the Edit screen. Returns null if not found. */
    @Query("SELECT * FROM pantry_items WHERE id = :id")
    PantryItem getById(long id);

    /** READ (count) - shown on the Settings screen. */
    @Query("SELECT COUNT(*) FROM pantry_items")
    int count();

    /** UPDATE - matches the row by primary key; returns number of rows changed. */
    @Update
    int update(PantryItem item);

    /** DELETE - matches the row by primary key; returns number of rows removed. */
    @Delete
    int delete(PantryItem item);

    /** DELETE ALL - "Clear pantry" on the Settings screen; returns number of rows removed. */
    @Query("DELETE FROM pantry_items")
    int deleteAll();
}
