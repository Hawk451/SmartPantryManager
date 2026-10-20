package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;

import com.example.smartpantrymanager.logic.IngredientNormalizer;

import org.junit.Test;

public class IngredientNormalizerTest {

    // --- Basic cleanup ---
    @Test public void lowercasesAndTrims()        { assertEquals("egg",       IngredientNormalizer.normalize("  Eggs ")); }
    @Test public void collapsesInnerSpaces()      { assertEquals("red onion", IngredientNormalizer.normalize("Red   Onions")); }
    @Test public void nullBecomesEmpty()          { assertEquals("",          IngredientNormalizer.normalize(null)); }
    @Test public void blankBecomesEmpty()         { assertEquals("",          IngredientNormalizer.normalize("   ")); }

    // --- Plural rules ---
    @Test public void plainS()                    { assertEquals("carrot",    IngredientNormalizer.normalize("Carrots")); }
    @Test public void oesToO()                    { assertEquals("tomato",    IngredientNormalizer.normalize("Tomatoes")); }
    @Test public void oesToOPotato()              { assertEquals("potato",    IngredientNormalizer.normalize("potatoes")); }
    @Test public void iesToY()                    { assertEquals("berry",     IngredientNormalizer.normalize("Berries")); }
    @Test public void chesToCh()                  { assertEquals("peach",     IngredientNormalizer.normalize("Peaches")); }

    // --- Words that must NOT be changed ---
    @Test public void alreadySingular()           { assertEquals("onion",     IngredientNormalizer.normalize("Onion")); }
    @Test public void endsInE()                   { assertEquals("cheese",    IngredientNormalizer.normalize("Cheese")); }
    @Test public void endsInUs()                  { assertEquals("couscous",  IngredientNormalizer.normalize("Couscous")); }
    @Test public void endsInUsHummus()            { assertEquals("hummus",    IngredientNormalizer.normalize("Hummus")); }
    @Test public void endsInSs()                  { assertEquals("swiss chard", IngredientNormalizer.normalize("Swiss Chard")); }

    // --- Irregular plurals ---
    @Test public void irregularLeaves()           { assertEquals("bay leaf",  IngredientNormalizer.normalize("Bay Leaves")); }

    // --- Only the LAST word is singularised ---
    @Test public void onlyLastWord()              { assertEquals("green bean", IngredientNormalizer.normalize("Green Beans")); }
}
