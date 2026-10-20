package com.example.smartpantrymanager;

import static org.junit.Assert.*;

import com.example.smartpantrymanager.logic.UnitConverter;

import org.junit.Test;

public class UnitConverterTest {

    private static final double DELTA = 0.0001;

    @Test public void kgToGrams()               { assertEquals(1500, UnitConverter.toBase(1.5, "kg"), DELTA); }
    @Test public void litresToMl()              { assertEquals(2000, UnitConverter.toBase(2, "L"), DELTA); }
    @Test public void caseAndSpaceInsensitive() { assertEquals(500, UnitConverter.toBase(0.5, " KG "), DELTA); }
    @Test public void gAndKgCompatible()        { assertTrue(UnitConverter.areCompatible("g", "kg")); }
    @Test public void gAndMlNotCompatible()     { assertFalse(UnitConverter.areCompatible("g", "ml")); }
    @Test public void unknownNotCompatible()    { assertFalse(UnitConverter.areCompatible("handful", "g")); }

    @Test(expected = IllegalArgumentException.class)
    public void unknownUnitThrows()             { UnitConverter.toBase(1, "handful"); }
}
