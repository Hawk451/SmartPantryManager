package com.example.smartpantrymanager;

import static org.junit.Assert.*;

import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.data.entity.Recipe;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;
import com.example.smartpantrymanager.logic.IngredientNormalizer;
import com.example.smartpantrymanager.logic.RecipeMatcher;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMatcherTest {

    // ---------- helpers ----------
    private static PantryItem have(String name, double qty, String unit) {
        return new PantryItem(name, IngredientNormalizer.normalize(name), qty, unit, null);
    }

    private static RecipeIngredient need(String name, double qty, String unit) {
        return new RecipeIngredient(0, name, IngredientNormalizer.normalize(name), qty, unit);
    }

    private static RecipeWithIngredients recipe(String name, RecipeIngredient... ingredients) {
        RecipeWithIngredients r = new RecipeWithIngredients();
        r.recipe = new Recipe(name, "", "", 0);
        r.ingredients = new ArrayList<>(Arrays.asList(ingredients));
        return r;
    }

    private static RecipeWithIngredients omelette() {
        return recipe("Tomato Omelette",
                need("Eggs", 3, "each"),
                need("Tomato", 200, "g"),
                need("Milk", 50, "ml"),
                need("Cheese", 30, "g"));
    }

    // ---------- core rule ----------
    @Test
    public void allIngredientsPresent_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "each"), have("Tomato", 300, "g"),
                have("Milk", 1, "L"), have("Cheese", 100, "g"));
        assertTrue(RecipeMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void fourOfFiveIngredients_isNotSuggested() {      // exact example from the brief
        RecipeWithIngredients r = recipe("Five Things",
                need("Egg", 1, "each"), need("Onion", 1, "each"), need("Rice", 100, "g"),
                need("Milk", 100, "ml"), need("Butter", 20, "g"));
        List<PantryItem> pantry = Arrays.asList(
                have("Egg", 2, "each"), have("Onion", 2, "each"),
                have("Rice", 500, "g"), have("Milk", 500, "ml"));   // no butter
        assertFalse(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void notEnoughQuantity_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 2, "each"),                                // needs 3
                have("Tomato", 300, "g"), have("Milk", 1, "L"), have("Cheese", 100, "g"));
        assertFalse(RecipeMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void exactQuantity_isSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 3, "each"), have("Tomato", 200, "g"),
                have("Milk", 50, "ml"), have("Cheese", 30, "g"));
        assertTrue(RecipeMatcher.canMake(omelette(), pantry));
    }

    // ---------- robustness ----------
    @Test
    public void pluralAndCaseDifferences_stillMatch() {
        List<PantryItem> pantry = Arrays.asList(
                have("  EGGS ", 6, "each"), have("Tomatoes", 300, "g"),
                have("milk", 1, "L"), have("Cheese", 100, "g"));
        assertTrue(RecipeMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void unitConversion_kgSatisfiesGrams() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "each"), have("Tomato", 0.5, "kg"),
                have("Milk", 1, "L"), have("Cheese", 0.1, "kg"));
        assertTrue(RecipeMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void incompatibleUnits_isNotSuggested() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "each"), have("Tomato", 3, "each"),    // recipe needs grams
                have("Milk", 1, "L"), have("Cheese", 100, "g"));
        assertFalse(RecipeMatcher.canMake(omelette(), pantry));
    }

    @Test
    public void duplicatePantryEntries_areSummed() {
        RecipeWithIngredients r = recipe("Big Sauce", need("Tomato", 700, "g"));
        List<PantryItem> pantry = Arrays.asList(
                have("Tomatoes", 300, "g"), have("Tomato", 0.5, "kg"));   // 800 g total
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void emptyPantry_suggestsNothing() {
        RecipeMatcher.MatchResult result = RecipeMatcher.match(
                Collections.singletonList(omelette()), Collections.<PantryItem>emptyList());
        assertTrue(result.canMake.isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNeverSuggested() {
        assertFalse(RecipeMatcher.canMake(recipe("Air"), Collections.<PantryItem>emptyList()));
    }

    // ---------- Almost There separation ----------
    @Test
    public void oneMissing_goesToAlmostThere_notCanMake() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "each"), have("Tomato", 300, "g"), have("Milk", 1, "L"));
        RecipeMatcher.MatchResult result =
                RecipeMatcher.match(Collections.singletonList(omelette()), pantry);

        assertTrue(result.canMake.isEmpty());
        assertEquals(1, result.almostThere.size());
        assertEquals("cheese", result.almostThere.get(0).missingIngredient.getNormalizedName());
    }

    @Test
    public void twoMissing_appearsInNeitherList() {
        List<PantryItem> pantry = Arrays.asList(have("Eggs", 6, "each"), have("Tomato", 300, "g"));
        RecipeMatcher.MatchResult result =
                RecipeMatcher.match(Collections.singletonList(omelette()), pantry);

        assertTrue(result.canMake.isEmpty());
        assertTrue(result.almostThere.isEmpty());
    }

    @Test
    public void missingIngredients_listsExactlyWhatIsMissing() {
        List<PantryItem> pantry = Arrays.asList(
                have("Eggs", 6, "each"), have("Milk", 1, "L"));     // no tomato, no cheese
        List<RecipeIngredient> missing = RecipeMatcher.missingIngredients(omelette(), pantry);

        assertEquals(2, missing.size());
        assertEquals("tomato", missing.get(0).getNormalizedName());
        assertEquals("cheese", missing.get(1).getNormalizedName());
    }
}
