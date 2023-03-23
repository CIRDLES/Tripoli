package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SplineBasisModelTest {

    @Test
    void bBase() {
        Primitive64Store test = MatLab.linspace(204.83994725925928, 205.10565274074074, 1000);
        Primitive64Store actual = SplineBasisModel.bBase(test, 204.83994725925928, 205.10565274074074, 22, 3);
        MatrixStore<Double> expected = new SplineBasisModel(test, 22, 3).getBSplineMatrix();
        System.out.println(expected.toString());
        System.out.println(test.toString());
        assertTrue(Arrays.deepEquals(expected.toRawCopy2D(), actual.toRawCopy2D()));

    }
}