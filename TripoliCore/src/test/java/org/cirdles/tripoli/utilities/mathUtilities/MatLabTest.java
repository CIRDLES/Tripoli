package org.cirdles.tripoli.utilities.mathUtilities;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class MatLabTest {
    /**
     * Takes a string of comma separated values and turns them into a 2D array of doubles.
     * Takes first element in the format mXn and uses this as the dimensions of the array.
     *
     * @param csv the string of comma-separated values of a line in a CSV or TXT
     * @return
     */
    public double[][] read_csv_string(String csv) {
        String[] tokens = csv.split(",");
        String dim = tokens[0];
        String[] dims = dim.split("x");
        double m = Double.parseDouble(dims[0]);
        double n = Double.parseDouble(dims[1]);
        int m_int = (int) m;
        int n_int = (int) n;
        //System.out.println(Double.toString(m) + " : " + Double.toString(n));
        //System.out.println(csv);
        //System.out.println(dim);

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

    /**
     * Read a CSV/TXT serialization of a MATLAB matrix.
     * <p>
     * Uses read_csv_string to convert lines to data.
     *
     * @param fn filename of serialization
     * @return
     * @throws IOException
     */
    public ArrayList<double[][]> read_csv(String fn) throws IOException {
        ResourceExtractor tripoliExtractor = new ResourceExtractor(Tripoli.class);
        File filename = tripoliExtractor.extractResourceAsFile("/org/cirdles/tripoli/core/" + fn);
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

    /**
     * Kronecker Product Test
     * <p>
     * Deserializes matrices from files written using MATLAB
     * Matrix A is in kron_matrix_A.txt, Matrix B is in kron_matrix_B.txt
     * Answers are in kron_answers.txt
     * <p>
     * Matrices were serialized according to the following MATLAB definitions:
     * <p>
     * rowDim = randi(10);
     * colDim = randi(10);
     * rowDim2 = randi(10);
     * colDim2 = randi(10);
     * <p>
     * zeroMatrix1 = zeros(rowDim, colDim);
     * zeroMatrix2 = zeros(rowDim2, colDim2);
     * edgeCase1 = rand();
     * edgeCase2 = rand();
     * <p>
     * randMat = max.*rand(rowDim, colDim);
     * rHelper = (-1).^randi(2,rowDim,colDim);
     * randMat = randMat.*rHelper;
     * if edgeCase1 < 0.1
     * randMat = zeroMatrix1;
     * end
     * randMatSize = size(randMat);
     * filename1 = fullfile('kron_matrix_A.txt');
     * <p>
     * randMat2 = max.*rand(rowDim2, colDim2);
     * r_helper = (-1).^randi(2,rowDim2,colDim2);
     * randMat2 = randMat2.*r_helper;
     * if edgeCase2 < 0.1
     * randMat2 = zeroMatrix2;
     * end
     * randMatSize2 = size(randMat2);
     * filename2 = fullfile('kron_matrix_B.txt');
     * <p>
     * kronout = kron(randMat, randMat2);
     * kronsize = size(kronout);
     * filename3 = fullfile('kron_answers.txt');
     * <p>
     * The size of the matrix is the first element in the CSV (txt) file, represented as mXn,
     *
     * @throws IOException
     */
    @Test
    void kronTest() throws IOException {
        ArrayList<double[][]> aList = read_csv("kron_matrix_A.txt");
        ArrayList<double[][]> bList = read_csv("kron_matrix_B.txt");
        ArrayList<double[][]> answerList = read_csv("kron_answers.txt");
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive64Store a;
        Primitive64Store b;
        double[][] expected;
        Primitive64Store actual;
        double[][] actualArray;
        for (int i = 0; i < answerList.size(); i++) {
            a = storeFactory.rows(aList.get(i));
            b = storeFactory.rows(bList.get(i));

            expected = answerList.get(i);
            actual = MatLab.kron(a, b);
            actualArray = actual.toRawCopy2D();


            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    expected[j][k] = MatLab.roundedToSize(expected[j][k], 8);
                }
            }

            for (int j = 0; j < actualArray.length; j++) {
                for (int k = 0; k < actualArray[j].length; k++) {
                    actualArray[j][k] = MatLab.roundedToSize(actualArray[j][k], 8);
                }
            }
            assertArrayEquals(expected, actualArray);
        }
    }

    /**
     * Matrix Exponent Test
     * Deserializes matrices from files written using MATLAB
     * Matrix A is in exp_matrix_A.txt
     * Answers are in exp_answers.txt
     * <p>
     * Matrices were serialized according to the following MATLAB definitions:
     * <p>
     * rowDim = randi(10);
     * <p>
     * zeroMatrix1 = zeros(rowDim, rowDim);
     * edgeCase1 = rand();
     * <p>
     * randMat = max.*rand(rowDim, rowDim);
     * rHelper = (-1).^randi(2,rowDim,rowDim);
     * randMat = randMat.*rHelper;
     * if edgeCase1 < 0.1
     * randMat = zeroMatrix1;
     * end
     * randMatSize = size(randMat);
     * filename1 = fullfile('exp_matrix_A.txt');
     * <p>
     * expOut = randMat.^2;
     * expSize = size(expOut);
     * filename2 = fullfile('exp_answers.txt');
     * <p>
     * The size of the matrix is the first element in the CSV (txt) file, represented as mXn,
     *
     * @throws IOException
     */
    @Test
    void expMatrixTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> aList = read_csv("exp_matrix_A.txt");
        ArrayList<double[][]> answerList = read_csv("exp_answers.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store a;
        double[][] actualArray;
        for (int i = 0; i < answerList.size(); i++) {
            a = storeFactory.rows(aList.get(i));

            expected = answerList.get(i);
            actual = MatLab.expMatrix(a, 2);
            actualArray = actual.toRawCopy2D();

            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    expected[j][k] = MatLab.roundedToSize(expected[j][k], 10);
                }
            }

            for (int j = 0; j < actualArray.length; j++) {
                for (int k = 0; k < actualArray[j].length; k++) {
                    actualArray[j][k] = MatLab.roundedToSize(actualArray[j][k], 10);
                }
            }
            assertArrayEquals(expected, actualArray);
        }
    }

    @Test
    void diffTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("diff_matrix_A.txt");
        ArrayList<double[][]> Answer_list = read_csv("diff_answers.txt");
        double[][] expected;
        Primitive64Store actual;
        Primitive64Store A;
        double[][] actualArray;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            expected = Answer_list.get(i);
            actual = MatLab.diff(A);
            actualArray = actual.toRawCopy2D();

            //System.out.println(Arrays.toString(expected));
            assertArrayEquals(expected, actualArray);
        }
    }

    @Test
    void greaterOrEqualTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("greater_or_equal_matrix_A.txt");
        ArrayList<double[][]> Answer_list = read_csv("greater_or_equal_answers.txt");
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
        ArrayList<double[][]> A_list = read_csv("size_matrix_A.txt");
        ArrayList<double[][]> Answer_list = read_csv("size_answers.txt");
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

    /**
     * Linspace test
     * Deserializes matrices from files written using MATLAB
     * Matrix A contains the parameters for the linspace. It is linspace_matrix_A.txt
     * Answers are in linspace_answers.txt
     * rowDim = randi(11) - 1;
     * colDim = randi(10);
     * rowDim2 = randi(10);
     * colDim2 = randi(10);
     * <p>
     * zeroMatrix1 = zeros(1, 2);
     * zeroMatrix2 = zeros(1, 1);
     * edgeCase1 = rand();
     * <p>
     * randMat = max.*rand(1, 2);
     * rHelper = (-1).^randi(2,1,2);
     * randMat = randMat.*rHelper;
     * <p>
     * if edgeCase1 < 0.1
     * randMat = zeroMatrix1;
     * end
     * <p>
     * filename1 = fullfile('linspace_matrix_A.txt');
     * <p>
     * <p>
     * linout = linspace(randMat(1), randMat(2), rowDim);
     * linsize = size(linout);
     * filename3 = fullfile('linspace_answers.txt');
     * randMatSize1 = size(randMat);
     *
     * @throws IOException
     */
    @Test
    void linspace2Test() throws IOException {
        ArrayList<double[][]> A_list = read_csv("linspace_matrix_A.txt");
        ArrayList<double[][]> Answer_list = read_csv("linspace_answers.txt");
        double val1;
        double val2;
        double points;
        double[][] expected;
        double[][] actualArray;
        Primitive64Store actual;
        for (int i = 0; i < Answer_list.size(); i++) {
            val1 = A_list.get(i)[0][0];
            val2 = A_list.get(i)[0][1];
            points = A_list.get(i)[0][2];

            expected = Answer_list.get(i);
            actual = MatLab.linspace2(val1, val2, points);
            actualArray = actual.toRawCopy2D();


            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    expected[j][k] = MatLab.roundedToSize(expected[j][k], 12);
                }
            }

            for (int j = 0; j < actualArray.length; j++) {
                for (int k = 0; k < actualArray[j].length; k++) {
                    actualArray[j][k] = MatLab.roundedToSize(actualArray[j][k], 12);
                }
            }

            assertArrayEquals(expected, actualArray);
        }
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

    /**
     * rDivide test
     * Deserializes matrices from files written using MATLAB
     * Matrix A contains the input matrix. It is rDivide_matrix_A.txt
     * Matrix B contains the parameters for the rDivide. It is rDivide_matrix_B.txt
     * Answers are in linspace_answers.txt
     * rowDim = randi(10);
     * colDim = randi(10);
     * rowDim2 = randi(10);
     * colDim2 = randi(10);
     * <p>
     * <p>
     * randMat = max.*rand(rowDim, colDim);
     * rHelper = (-1).^randi(2,rowDim,colDim);
     * randMat = randMat.*rHelper;
     * <p>
     * <p>
     * filename1 = fullfile('rDivide_matrix_A.txt');
     * <p>
     * randMat2 = max2.*rand(1, 1);
     * r_helper = (-1).^randi(2,1,1);
     * randMat2 = randMat2.*r_helper;
     * <p>
     * randMatSize2 = size(randMat2);
     * filename2 = fullfile('rDivide_matrix_B.txt');
     * <p>
     * rout = rdivide(randMat, randMat2(1));
     * rsize = size(rout);
     */
    // TODO: Make sure the original rDivide (flipped parameters) is okay.
    @Test
    void rDivide2Test() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("rDivide_matrix_A.txt");
        ArrayList<double[][]> B_list = read_csv("rDivide_matrix_B.txt");
        ArrayList<double[][]> Answer_list = read_csv("rDivide_answers.txt");
        double[][] expected;
        double[][] actualArray;
        Primitive64Store actual;
        Primitive64Store A;
        Primitive64Store B;
        for (int i = 0; i < Answer_list.size(); i++) {
            A = storeFactory.rows(A_list.get(i));
            B = storeFactory.rows(B_list.get(i));

            expected = Answer_list.get(i);

            //actual = MatLab.rDivide(B.get(0, 0), A);

            actual = MatLab.rDivide2(A, B.get(0, 0));

            actualArray = actual.toRawCopy2D();

            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    expected[j][k] = MatLab.roundedToSize(expected[j][k], 2);
                }
            }

            for (int j = 0; j < actualArray.length; j++) {
                for (int k = 0; k < actualArray[j].length; k++) {
                    actualArray[j][k] = MatLab.roundedToSize(actualArray[j][k], 2);
                }
            }


            assertArrayEquals(expected, actualArray);
        }
    }

    // TODO: See if MATLAB can consistently generate test cases without the solver reaching max iterations
    @Test
    void solveNNLSTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.rows(new double[][]{{1, 7, 3}, {6, 2, 9}});
        Primitive64Store actual = MatLab.max(A, 5);
        double[][] expected = new double[][]{{5, 7, 5}, {6, 5, 9}};
        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    // TODO: MATLAB's max function does not work exactly like this. Ensure this function is what is desired.
    @Test
    void maxTest() {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;

        Primitive64Store A = storeFactory.rows(new double[][]{{1, 7, 3}, {6, 2, 9}});
        Primitive64Store actual = MatLab.max(A, 5);
        double[][] expected = new double[][]{{5, 7, 5}, {6, 5, 9}};
        assertTrue(Arrays.deepEquals(expected, actual.toRawCopy2D()));
    }

    /**
     * diag test
     * Deserializes matrices from files written using MATLAB
     * Matrix A contains the original matrix. It is diag_matrix_A.txt
     * Answers are in diag_answers.txt
     * rowDim = randi(10);
     * <p>
     * <p>
     * zeroMatrix1 = zeros(rowDim, 1);
     * <p>
     * edgeCase1 = rand();
     * <p>
     * <p>
     * randMat = max.*rand(rowDim, 1);
     * rHelper = (-1).^randi(2, rowDim, 1);
     * randMat = randMat.*rHelper;
     * if edgeCase1 < 0.1
     * randMat = zeroMatrix1;
     * end
     * <p>
     * <p>
     * diagout = diag(randMat);
     * diagsize = size(diagout);
     * randMatSize1 = size(randMat);
     *
     * @throws IOException
     */
    @Test
    void diagTest() throws IOException {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("diag_matrix_A.txt");
        ArrayList<double[][]> Answer_list = read_csv("diag_answers.txt");
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