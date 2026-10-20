package com.example.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Turns user-typed ingredient names into a consistent "matching key".
 * e.g. "  Red  Onions " -> "red onion",  "Tomatoes" -> "tomato".
 *
 * Both pantry items AND recipe ingredients pass through this same method,
 * so even if a rare word is singularised imperfectly, both sides end up
 * with the same key and still match.
 */
public final class IngredientNormalizer {

    /** Plurals that don't follow the regular rules. */
    private static final Map<String, String> IRREGULAR = new HashMap<>();
    static {
        IRREGULAR.put("leaves", "leaf");
        IRREGULAR.put("loaves", "loaf");
        IRREGULAR.put("halves", "half");
        IRREGULAR.put("molasses", "molasses");   // looks plural, isn't
    }

    private IngredientNormalizer() { }   // utility class - no instances

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }

        // 1. Clean up: trim, lowercase, collapse repeated whitespace to one space.
        //    Locale.ROOT avoids locale bugs (e.g. Turkish dotless 'i').
        String cleaned = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

        if (cleaned.isEmpty()) {
            return "";
        }

        // 2. Only singularise the LAST word: "green beans" -> "green bean".
        int lastSpace = cleaned.lastIndexOf(' ');
        String prefix = cleaned.substring(0, lastSpace + 1);   // "" when there's no space
        String lastWord = cleaned.substring(lastSpace + 1);

        return prefix + singularize(lastWord);
    }

    /** Rules are checked most-specific first; the first match wins. */
    private static String singularize(String word) {
        if (IRREGULAR.containsKey(word)) {
            return IRREGULAR.get(word);
        }
        if (word.length() <= 3) {                         // "gas", "oil", "egg" - too short to guess safely
            return word;
        }
        if (word.endsWith("ss") || word.endsWith("us")) { // "swiss", "couscous", "hummus"
            return word;
        }
        if (word.endsWith("ies")) {                       // berries -> berry
            return word.substring(0, word.length() - 3) + "y";
        }
        if (word.endsWith("oes")) {                       // tomatoes -> tomato
            return word.substring(0, word.length() - 2);
        }
        if (word.endsWith("ches") || word.endsWith("shes") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);  // peaches -> peach, radishes -> radish
        }
        if (word.endsWith("s")) {                         // carrots -> carrot, olives -> olive
            return word.substring(0, word.length() - 1);
        }
        return word;                                      // already singular: "cheese", "rice"
    }
}
