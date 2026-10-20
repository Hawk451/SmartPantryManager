package com.example.smartpantrymanager.data.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.smartpantrymanager.data.entity.Recipe;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;

import java.util.List;

/**
 * Not a table - a query result that bundles a Recipe with all of its ingredients.
 * Room fills the 'ingredients' list automatically by matching recipes.id = recipe_ingredients.recipe_id.
 */
public class RecipeWithIngredients {

    @Embedded
    public Recipe recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    public List<RecipeIngredient> ingredients;
}
