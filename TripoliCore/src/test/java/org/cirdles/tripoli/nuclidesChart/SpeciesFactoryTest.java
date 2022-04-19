package org.cirdles.tripoli.nuclidesChart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesFactoryTest {

    @BeforeEach
    void setUp() {
        System.err.println("Testing SpeciesFactory  " + SpeciesFactory.speciesByProtonList.size());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateSpeciesChart(){
        int actualValue = ((Species)((List<?>)SpeciesFactory.speciesByProtonList.get("Nd")).get(0)).neutronsN();
        int expectedValue = 65;

        assertEquals(actualValue, expectedValue);

    }
}