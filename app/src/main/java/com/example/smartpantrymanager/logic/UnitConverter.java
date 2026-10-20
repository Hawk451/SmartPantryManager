package com.example.smartpantrymanager.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities into a base unit so different units can be compared:
 *   MASS   -> grams
 *   VOLUME -> millilitres
 *   COUNT  -> "each"
 * Units from different dimensions (e.g. grams vs each) can NOT be compared.
 */
public final class UnitConverter {

    public enum Dimension { MASS, VOLUME, COUNT }

    /** Units offered in the Add/Edit screen's dropdown. */
    public static final List<String> DISPLAY_UNITS = Collections.unmodifiableList(
            Arrays.asList("g", "kg", "ml", "L", "each", "cup", "tbsp", "tsp"));

    private static final Map<String, Dimension> DIMENSION = new HashMap<>();
    private static final Map<String, Double> FACTOR_TO_BASE = new HashMap<>();

    static {
        register(Dimension.MASS,   1,    "g", "gram", "grams");
        register(Dimension.MASS,   1000, "kg", "kilogram", "kilograms");
        register(Dimension.VOLUME, 1,    "ml", "millilitre", "millilitres", "milliliter", "milliliters");
        register(Dimension.VOLUME, 1000, "l", "litre", "litres", "liter", "liters");
        register(Dimension.VOLUME, 250,  "cup", "cups");                 // metric cup
        register(Dimension.VOLUME, 15,   "tbsp", "tablespoon", "tablespoons");
        register(Dimension.VOLUME, 5,    "tsp", "teaspoon", "teaspoons");
        register(Dimension.COUNT,  1,    "each", "ea", "pc", "pcs", "piece", "pieces", "whole");
    }

    private UnitConverter() { }

    private static void register(Dimension dim, double factor, String... names) {
        for (String n : names) {
            DIMENSION.put(n, dim);
            FACTOR_TO_BASE.put(n, factor);
        }
    }

    private static String key(String unit) {
        return unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isKnownUnit(String unit) {
        return DIMENSION.containsKey(key(unit));
    }

    /** Returns the dimension, or null if the unit is unknown. */
    public static Dimension dimensionOf(String unit) {
        return DIMENSION.get(key(unit));
    }

    /** True only if both units are known AND measure the same kind of thing. */
    public static boolean areCompatible(String unitA, String unitB) {
        Dimension a = dimensionOf(unitA);
        return a != null && a == dimensionOf(unitB);
    }

    /** e.g. toBase(1.5, "kg") -> 1500.0 (grams). */
    public static double toBase(double quantity, String unit) {
        Double factor = FACTOR_TO_BASE.get(key(unit));
        if (factor == null) {
            throw new IllegalArgumentException("Unknown unit: " + unit);
        }
        return quantity * factor;
    }
}
