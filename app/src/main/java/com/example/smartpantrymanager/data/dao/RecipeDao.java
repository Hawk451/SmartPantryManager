package com.example.smartpantrymanager.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.smartpantrymanager.data.entity.Recipe;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;

import java.util.List;

/**
 * Data access for the read-only recipe collection.
 * All methods are synchronous - call them from a background thread.
 */
@Dao
public interface RecipeDao {

    @Insert
    long insertRecipe(Recipe recipe);              // returns the new row's id

    @Insert
    void insertIngredients(List<RecipeIngredient> ingredients);

    /**
     * Inserts a recipe and its ingredients as ONE transaction:
     * either everything is saved, or nothing is.
     */
    @Transaction
    default void insertRecipeWithIngredients(Recipe recipe, List<RecipeIngredient> ingredients) {
        long recipeId = insertRecipe(recipe);
        for (RecipeIngredient ingredient : ingredients) {
            ingredient.setRecipeId(recipeId);      // link each ingredient to its parent
        }
        insertIngredients(ingredients);
    }

    /** Every recipe with its ingredient list - this is what the matcher runs against. */
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name")
    List<RecipeWithIngredients> getAllWithIngredients();

    /** One recipe for the detail screen. */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    RecipeWithIngredients getById(long recipeId);

    @Query("SELECT COUNT(*) FROM recipes")
    int count();
}
