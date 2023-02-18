package org.cirdles.tripoli.utilities.mathUtilities;

import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatLabTest {
    public double[][] read_csv_string(String csv) {
        String[] tokens = csv.split(",");
        String dim = tokens[0];
        String[] dims = dim.split("x");
        double m = Double.parseDouble(dims[0]);
        double n = Double.parseDouble(dims[1]);
        int m_int = (int)m;
        int n_int = (int)n;
        double[][] matrix = new double[m_int][n_int];
        String[] nums = Arrays.copyOfRange(tokens, 1, tokens.length);
        int counter = 0;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = Double.parseDouble(nums[counter++]);
            }
            //System.out.println(Arrays.toString(matrix[i]));
        }

        return matrix;
    }


    public ArrayList<double[][]> read_csv(String fn) throws IOException {
        String filename = fn;
        ArrayList<double[][]> matrices = new ArrayList<>();
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        double[][] matrix = read_csv_string(line);
        matrices.add(matrix);
        while (br.ready()) {
            line = br.readLine();
            matrix = read_csv_string(line);
            matrices.add(matrix);
        }
        br.close();
        return matrices;
    }
    // add negs
    @Test
    void kronTest() throws IOException {
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\kronTestFiles\\matA.txt");
        ArrayList<double[][]> B_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\kronTestFiles\\matB.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\kronTestFiles\\answers.txt");
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store A;
        Primitive64Store B;
        double[][] expected;
        Primitive64Store actual;
        for (int i = 0; i < Answer_list.size(); i++){
            A = storeFactory.rows(A_list.get(i));
            B = storeFactory.rows(B_list.get(i));

            expected = Answer_list.get(i);
            actual = MatLab.kron(A, B);

            System.out.println(expected.length);
            System.out.println(actual.countRows());
            System.out.println(actual.countColumns());
            System.out.println(Arrays.deepToString(expected));
            System.out.println(Arrays.deepToString(actual.toRawCopy2D()));
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }

    // add negs
    @Test
    void expMatrixTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\expTestFiles\\matA.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\expTestFiles\\answers.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++){
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.expMatrix(A, 2);
            System.out.println(expected.length);
            System.out.println(actual.countRows());
            System.out.println(actual.countColumns());
            System.out.println(Arrays.deepToString(expected));
            System.out.println(Arrays.deepToString(actual.toRawCopy2D()));
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }

    // add negs
    @Test
    void diffTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\diffTestFiles\\matA.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\diffTestFiles\\answers.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.diff(A);
            System.out.println(expected.length);
            System.out.println(actual.countRows());
            System.out.println(actual.countColumns());
            System.out.println(Arrays.deepToString(expected));
            System.out.println(Arrays.deepToString(actual.toRawCopy2D()));
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }

    @Test
    void greaterOrEqualTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\greaterOrEqualTestFiles\\matA2.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\greaterOrEqualTestFiles\\answers2.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.greaterOrEqual(A, 0);
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }

    @Test
    void sizeTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\sizeTestFiles\\matA2.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\sizeTestFiles\\answers2.txt");
        double expected1;
        double expected2;
        int actual1;
        int actual2;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected1 = Answer_list.get(i)[0][0];
            expected2 = Answer_list.get(i)[0][1];

            actual1 = MatLab.size(A, 1);
            actual2 = MatLab.size(A, 2);
            assertEquals(expected1, actual1);
            assertEquals(expected2, actual2);
        }
    }

    @Test
    void linspaceTest() {

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

        Primitive64Store A = storeFactory.rows(new double[][]{{0, 0, 3}, {0, 0, 3}, {0, 0, 3}});
        Primitive64Store actual = MatLab.any(A, 1);
        double[][] expected = new double[][]{{0, 0, 1}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));

        actual = MatLab.any(A, 2);
        expected = new double[][]{{1}, {1}, {1}};

        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));

    }

    @Test
    void rDivideTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\rDivideTestFiles\\matA2.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\rDivideTestFiles\\answers2.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.rDivide(A, -5);
            System.out.println(expected.length);
            System.out.println(actual.countRows());
            System.out.println(actual.countColumns());
            System.out.println(Arrays.deepToString(expected));
            System.out.println(Arrays.deepToString(actual.toRawCopy2D()));
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }

    // fix error
    @Test
    void maxTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.rows(new double[][]{{1, 7, 3}, {6, 2, 9}});
        Primitive64Store actual = MatLab.max(A, 5);
        double[][] expected = new double[][]{{5, 7, 5}, {6, 5, 9}};
        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    @Test
    void diagTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\diagTestFiles\\matA2.txt");
        ArrayList<double[][]> Answer_list = read_csv("C:\\Users\\neilm\\Desktop\\tripoli\\Tripoli\\TripoliCore\\src\\test\\java\\org\\cirdles\\tripoli\\utilities\\mathUtilities\\diagTestFiles\\answers2.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.diag(A);
            assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
        }
    }
}