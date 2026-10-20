package com.example.smartpantrymanager.logic;

import java.util.regex.Pattern;

/**
 * Validation rules for the Add/Edit form.
 * Returns an error code (not a message) so the UI decides the wording
 * and these rules can be unit-tested without Android.
 */
public final class PantryItemValidator {

    public enum NameError { NONE, REQUIRED, TOO_LONG, INVALID_CHARACTERS }
    public enum QuantityError { NONE, REQUIRED, NOT_A_NUMBER, NOT_POSITIVE, TOO_LARGE }

    public static final int MAX_NAME_LENGTH = 40;
    public static final double MAX_QUANTITY = 100_000;

    /** Must start with a letter; then letters (incl. accents), spaces, hyphens, apostrophes. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}[\\p{L} '\\-]*$");

    private PantryItemValidator() { }

    public static NameError validateName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty()) {
            return NameError.REQUIRED;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return NameError.TOO_LONG;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            return NameError.INVALID_CHARACTERS;
        }
        return NameError.NONE;
    }

    public static QuantityError validateQuantity(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return QuantityError.REQUIRED;
        }
        Double value = parseQuantity(raw);
        if (value == null) {
            return QuantityError.NOT_A_NUMBER;
        }
        if (value <= 0) {
            return QuantityError.NOT_POSITIVE;
        }
        if (value > MAX_QUANTITY) {
            return QuantityError.TOO_LARGE;
        }
        return QuantityError.NONE;
    }

    /**
     * Accepts both "0.5" and "0,5" - South African phones often show a comma
     * as the decimal separator. Returns null if the text isn't a valid number.
     */
    public static Double parseQuantity(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim().replace(',', '.');
        if (s.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(s);
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
