package org.cirdles.tripoli.utilities.mathUtilities;

import jama.Matrix;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.Primitive32Matrix;
import org.ojalgo.matrix.Primitive64Matrix;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive32Store;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.SolverTask;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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

        Matrix linTest1 = MatLab.linspace(24.06, 25.08, 22);
        Primitive64Store linTest2 = MatLab.linspaceOJ(24.06, 25.08, 22);
        Primitive64Store test = Primitive64Store.FACTORY.make(5, 5);
        Primitive64Matrix test2 = Primitive64Matrix.FACTORY.make(5, 5);
        SolverTask<Double> solver = SolverTask.PRIMITIVE.make(test, linTest2);
    }

    @Test
    void findTest() {
        Matrix A = new Matrix(new double[][]{{1, 2, 3}, {4, 5, 6}});
        double[][] firstExpected = {{0.0}, {1.0}, {2.0}, {3.0}};
        Matrix firstActual = MatLab.find(A, 4, "first"); // actual
        double[][] lastExpected = {{5.0}, {4.0}, {3.0}, {2.0}};
        Matrix lastActual = MatLab.find(A, 4, "last"); // actual

        assertTrue(Arrays.deepEquals(firstActual.getArray(), firstExpected));
        assertTrue(Arrays.deepEquals(lastActual.getArray(), lastExpected));
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