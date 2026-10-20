package com.example.smartpantrymanager;

import static org.junit.Assert.*;

import com.example.smartpantrymanager.logic.PantryItemValidator;
import com.example.smartpantrymanager.logic.PantryItemValidator.NameError;
import com.example.smartpantrymanager.logic.PantryItemValidator.QuantityError;

import org.junit.Test;

public class PantryItemValidatorTest {

    // ----- names -----
    @Test public void validName()            { assertEquals(NameError.NONE, PantryItemValidator.validateName("Red Onion")); }
    @Test public void nameWithApostrophe()   { assertEquals(NameError.NONE, PantryItemValidator.validateName("Hellmann's Mayo")); }
    @Test public void nameWithAccent()       { assertEquals(NameError.NONE, PantryItemValidator.validateName("Jalapeño")); }
    @Test public void emptyName()            { assertEquals(NameError.REQUIRED, PantryItemValidator.validateName("   ")); }
    @Test public void nullName()             { assertEquals(NameError.REQUIRED, PantryItemValidator.validateName(null)); }
    @Test public void nameWithDigits()       { assertEquals(NameError.INVALID_CHARACTERS, PantryItemValidator.validateName("Egg123")); }
    @Test public void nameStartingWithDash() { assertEquals(NameError.INVALID_CHARACTERS, PantryItemValidator.validateName("-egg")); }
    @Test public void nameTooLong() {
        assertEquals(NameError.TOO_LONG,
                PantryItemValidator.validateName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));   // 41 chars
    }

    // ----- quantities -----
    @Test public void wholeNumber()          { assertEquals(QuantityError.NONE, PantryItemValidator.validateQuantity("3")); }
    @Test public void dotDecimal()           { assertEquals(QuantityError.NONE, PantryItemValidator.validateQuantity("0.5")); }
    @Test public void commaDecimal()         { assertEquals(QuantityError.NONE, PantryItemValidator.validateQuantity("0,5")); }
    @Test public void emptyQuantity()        { assertEquals(QuantityError.REQUIRED, PantryItemValidator.validateQuantity("")); }
    @Test public void letters()              { assertEquals(QuantityError.NOT_A_NUMBER, PantryItemValidator.validateQuantity("two")); }
    @Test public void zero()                 { assertEquals(QuantityError.NOT_POSITIVE, PantryItemValidator.validateQuantity("0")); }
    @Test public void negative()             { assertEquals(QuantityError.NOT_POSITIVE, PantryItemValidator.validateQuantity("-1")); }
    @Test public void tooLarge()             { assertEquals(QuantityError.TOO_LARGE, PantryItemValidator.validateQuantity("999999")); }
    @Test public void commaParsesCorrectly() { assertEquals(0.5, PantryItemValidator.parseQuantity("0,5"), 0.0001); }
}
