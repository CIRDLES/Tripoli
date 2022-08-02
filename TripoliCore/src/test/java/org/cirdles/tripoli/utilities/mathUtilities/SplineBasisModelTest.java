package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.Primitive64Store;

class SplineBasisModelTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void bBase() {
        Primitive64Store test = MatLab.linspace(204.83994725925928, 205.10565274074074, 1000);
        Primitive64Store bBaseTest = SplineBasisModel.bBase(test, 204.83994725925928, 205.10565274074074, 22, 3);
    }
}