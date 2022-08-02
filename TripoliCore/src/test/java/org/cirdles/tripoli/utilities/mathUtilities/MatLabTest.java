package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.rows(new double[][]{{1, 2, 3}, {4, 5, 6}});
        Primitive64Store B = storeFactory.make(2, 2);
        B.fillAll(1.0);
        Primitive64Store actual = MatLab.kron(A, B);
        double[][] expected = new double[][]{{1.0, 1.0, 2.0, 2.0, 3.0, 3.0}, {1.0, 1.0, 2.0, 2.0, 3.0, 3.0}, {4.0, 4.0, 5.0, 5.0, 6.0, 6.0}, {4.0, 4.0, 5.0, 5.0, 6.0, 6.0}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    @Test
    void expMatrixTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store A = storeFactory.rows(new double[]{1, 2, 3, 4, 5});
        Primitive64Store actual = MatLab.expMatrix(A, 2);
        double[][] expected = new double[][]{{1, 4, 9, 16, 25}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));

        A = storeFactory.rows(new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}});
        actual = MatLab.expMatrix(A, -1);
        expected = new double[][]{{1.0, 0.5, 0.3333333333333333}, {0.25, 0.2, 0.16666666666666666}, {0.14285714285714285, 0.125, 0.1111111111111111}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    @Test
    void diffTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store X = storeFactory.rows(new double[]{1, 1, 2, 3, 5, 8, 13, 21});
        Primitive64Store actual = MatLab.diff(X);
        double[][] expected = new double[][]{{0.0, 1.0, 1.0, 2.0, 3.0, 5.0, 8.0}};
        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));


        X = storeFactory.rows(new double[][]{{1, 1, 1}, {5, 5, 5}, {25, 25, 25}});
        actual = MatLab.diff(X);
        expected = new double[][]{{4.0, 4.0, 4.0}, {20.0, 20.0, 20.0}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));


        X = storeFactory.rows(new double[]{0, 5, 15, 30, 50, 75, 105});
        MatrixStore<Double> actual2 = MatLab.diff(X, 2);
        expected = new double[][]{{5, 5, 5, 5, 5}};

        assertTrue(Arrays.deepEquals(expected, actual2.toRawCopy2D()));

    }

    @Test
    void greaterOrEqualTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.rows(new double[][]{{1, 12, 18, 7, 9, 11, 2, 15}});
        Primitive64Store actual = MatLab.greaterOrEqual(A, 11);
        double[][] expected = new double[][]{{0, 1, 1, 0, 0, 1, 0, 1}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    @Test
    void sizeTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.make(2, 3);
        int actual = MatLab.size(A, 1);
        int expected = 2;
        assertEquals(expected, actual);


        actual = MatLab.size(A, 2);
        expected = 3;
        assertEquals(expected, actual);

    }

    @Test
    void linspaceTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store actual = MatLab.linspace(-5, 5, 7);
        double[][] expected = new double[][]{{-5.0, -3.333333333333333, -1.6666666666666665, 0.0, 1.666666666666667, 3.333333333333334, 5.0}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
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
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
    }

    @Test
    void rDivideTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
    }

    @Test
    void maxTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
    }

    @Test
    void diagTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
    }
}