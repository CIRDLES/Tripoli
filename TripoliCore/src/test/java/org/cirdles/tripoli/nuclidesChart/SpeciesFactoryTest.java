package org.cirdles.tripoli.nuclidesChart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesFactoryTest {

    @BeforeEach
    void setUp() {
        System.out.println("Testing SpeciesFactory  " + SpeciesFactory.speciesByProtonList.size());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateSpeciesChart(){
        String actualValue = ((Species)((List<?>)SpeciesFactory.speciesByProtonList.get(60)).get(0)).elementSymbol();
        String expectedValue = "Nd";

        assertEquals(actualValue, expectedValue);

    }
}