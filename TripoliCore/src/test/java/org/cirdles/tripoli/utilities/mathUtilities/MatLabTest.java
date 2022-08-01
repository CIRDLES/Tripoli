package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MatLabTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void kronTest() {
    }

    @Test
    void expMatrixTest() {
    }

    @Test
    void divMatrixTest() {
    }

    @Test
    void diffTest() {
    }

    @Test
    void greatEqualTest() {
    }

    @Test
    void sizeTest() {
    }

    @Test
    void linspaceTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store linTest2 = MatLab.linspace(24.06, 25.08, 22);
        Primitive64Store test = Primitive64Store.FACTORY.make(5, 5);

    }

    @Test
    void findTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store A = storeFactory.rows(new double[][]{{1, 2, 3}, {4, 5, 6}});
        double[][] firstExpected = {{0.0}, {1.0}, {2.0}, {3.0}};
        Primitive64Store firstActual = MatLab.find(A, 4, "first"); // actual
        double[][] lastExpected = {{5.0}, {4.0}, {3.0}, {2.0}};
        Primitive64Store lastActual = MatLab.find(A, 4, "last"); // actual

        assertTrue(Arrays.deepEquals(firstActual.toRawCopy2D(), firstExpected));
        assertTrue(Arrays.deepEquals(lastActual.toRawCopy2D(), lastExpected));
    }

    @Test
    void anyTest() {
    }

    @Test
    void rDivideTest() {
    }

    @Test
    void maxTest() {
    }

    @Test
    void diagTest() {
    }
}