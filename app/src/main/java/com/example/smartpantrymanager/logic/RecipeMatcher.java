package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.data.entity.PantryItem;
import com.example.smartpantrymanager.data.entity.RecipeIngredient;
import com.example.smartpantrymanager.data.relation.RecipeWithIngredients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * THE STRICT-MATCHING RULE (Assignment section 2.3)
 *
 * A recipe is "suggested" ONLY if EVERY ingredient it needs is in the pantry
 * in AT LEAST the required quantity (after unit conversion).
 * One missing or short ingredient = not suggested. No partial matches.
 *
 * Bonus: recipes missing exactly ONE ingredient go into a separate
 * "Almost There" list - never mixed into the strict suggestions.
 */
public final class RecipeMatcher {

    /** Tolerance for floating-point rounding, e.g. 0.3 kg * 1000 = 300.00000000000006 g. */
    private static final double EPSILON = 1e-6;

    private RecipeMatcher() { }   // utility class - no instances

    // ------------------------------------------------------------------
    // Result types
    // ------------------------------------------------------------------

    /** Holds the two clearly separated lists the UI displays. */
    public static final class MatchResult {
        public final List<RecipeWithIngredients> canMake;      // strict suggestions
        public final List<AlmostThere> almostThere;            // bonus: missing exactly 1

        MatchResult(List<RecipeWithIngredients> canMake, List<AlmostThere> almostThere) {
            this.canMake = Collections.unmodifiableList(canMake);
            this.almostThere = Collections.unmodifiableList(almostThere);
        }
    }

    /** A recipe that is one ingredient short, plus which ingredient is missing. */
    public static final class AlmostThere {
        public final RecipeWithIngredients recipe;
        public final RecipeIngredient missingIngredient;

        AlmostThere(RecipeWithIngredients recipe, RecipeIngredient missingIngredient) {
            this.recipe = recipe;
            this.missingIngredient = missingIngredient;
        }
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /** Runs every recipe against the current pantry and sorts them into the two lists. */
    public static MatchResult match(List<RecipeWithIngredients> recipes, List<PantryItem> pantry) {
        Map<String, List<PantryItem>> pantryByName = groupByName(pantry);

        List<RecipeWithIngredients> canMake = new ArrayList<>();
        List<AlmostThere> almostThere = new ArrayList<>();

        for (RecipeWithIngredients recipe : recipes) {
            // A recipe with no ingredients would "match" any pantry - skip it.
            if (recipe.ingredients == null || recipe.ingredients.isEmpty()) {
                continue;
            }

            List<RecipeIngredient> missing = findMissing(recipe.ingredients, pantryByName);

            if (missing.isEmpty()) {
                canMake.add(recipe);                                   // strict match
            } else if (missing.size() == 1) {
                almostThere.add(new AlmostThere(recipe, missing.get(0)));
            }
            // 2+ missing: shown nowhere
        }
        return new MatchResult(canMake, almostThere);
    }

    /** Convenience check for a single recipe (used by tests and the detail screen). */
    public static boolean canMake(RecipeWithIngredients recipe, List<PantryItem> pantry) {
        if (recipe.ingredients == null || recipe.ingredients.isEmpty()) {
            return false;
        }
        return findMissing(recipe.ingredients, groupByName(pantry)).isEmpty();
    }

    /**
     * Which ingredients of this recipe the pantry can NOT satisfy.
     * Empty list = the recipe can be made. Used by the detail screen
     * to tick/cross each ingredient individually.
     */
    public static List<RecipeIngredient> missingIngredients(RecipeWithIngredients recipe,
                                                            List<PantryItem> pantry) {
        if (recipe.ingredients == null || recipe.ingredients.isEmpty()) {
            return Collections.emptyList();
        }
        return findMissing(recipe.ingredients, groupByName(pantry));
    }

    // ------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------

    /**
     * Groups pantry items by normalized name, so a lookup is O(1) instead of
     * scanning the whole pantry for every ingredient of every recipe.
     * A list is kept per name because the user may have several entries
     * for the same ingredient (e.g. "Tomatoes 300 g" and "Tomato 0.5 kg").
     */
    private static Map<String, List<PantryItem>> groupByName(List<PantryItem> pantry) {
        Map<String, List<PantryItem>> map = new HashMap<>();
        for (PantryItem item : pantry) {
            List<PantryItem> bucket = map.get(item.getNormalizedName());
            if (bucket == null) {
                bucket = new ArrayList<>();
                map.put(item.getNormalizedName(), bucket);
            }
            bucket.add(item);
        }
        return map;
    }

    /** Returns every ingredient the pantry can NOT satisfy (empty list = can make it). */
    private static List<RecipeIngredient> findMissing(List<RecipeIngredient> needed,
                                                      Map<String, List<PantryItem>> pantryByName) {
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : needed) {
            if (!isSatisfied(need, pantryByName)) {
                missing.add(need);
            }
        }
        return missing;
    }

    /**
     * One ingredient is satisfied when the TOTAL of all compatible pantry entries
     * with the same name, converted to the base unit, is >= the required amount.
     */
    private static boolean isSatisfied(RecipeIngredient need,
                                       Map<String, List<PantryItem>> pantryByName) {
        // Recipe uses a unit we can't convert -> can't prove we have enough -> not satisfied.
        if (!UnitConverter.isKnownUnit(need.getUnit())) {
            return false;
        }

        List<PantryItem> candidates = pantryByName.get(need.getNormalizedName());
        if (candidates == null) {
            return false;                                     // ingredient not in pantry at all
        }

        double requiredBase = UnitConverter.toBase(need.getQuantity(), need.getUnit());
        double availableBase = 0;

        for (PantryItem item : candidates) {
            // Only count entries measured the same way (g/kg vs ml/L vs each).
            if (UnitConverter.areCompatible(item.getUnit(), need.getUnit())) {
                availableBase += UnitConverter.toBase(item.getQuantity(), item.getUnit());
            }
        }

        return availableBase + EPSILON >= requiredBase;
    }
}
