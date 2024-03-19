package org.cirdles.tripoli.utilities.mathUtilities;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SplineBasisModelTest {
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

    //transposes when it should not
    // TODO: find out why bbase is only accurate to the first decimal place
/*    @Test
    void bbase2() throws IOException{
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        ArrayList<double[][]> A_list = read_csv("bspline_matrix_A.txt");
        ArrayList<double[][]> B_list = read_csv("bspline_matrix_B.txt");
        ArrayList<double[][]> Answer_list = read_csv("bspline_answers.txt");
        double val1;
        int lower;
        int upper;
        int segments;
        int degree;
        double[][] expected;
        double[][] actualArray;
        Primitive64Store actual;
        for (int i = 0; i < Answer_list.size(); i++) {
            lower = (int) B_list.get(i)[0][0];
            upper = (int) B_list.get(i)[0][1];
            segments = (int) B_list.get(i)[0][3];
            degree = (int) B_list.get(i)[0][4];

            expected = Answer_list.get(i);

            //actual = MatLab.linspace2(val1, val2, points);
            System.out.println(i);

            MatrixStore<Double> inputMatrix = storeFactory.rows(A_list.get(i));
            // TODO: find out if the matrix should really be transposed (see TODO in SplineBasisModel.java)
            MatrixStore<Double> transposedMatrix = inputMatrix.transpose();

            actual = SplineBasisModel.bBase(transposedMatrix, lower, upper, segments, degree);
            actualArray = actual.toRawCopy2D();


            for (int j = 0; j < expected.length; j++) {
                for (int k = 0; k < expected[j].length; k++) {
                    expected[j][k] = MatLab.roundedToSize(expected[j][k], 1);
                }
            }

            for (int j = 0; j < actualArray.length; j++) {
                for (int k = 0; k < actualArray[j].length; k++) {
                    actualArray[j][k] = MatLab.roundedToSize(actualArray[j][k], 1);
                }
            }
            System.out.println(Arrays.deepToString(expected));
            System.out.println(Arrays.deepToString(actualArray));
            assertArrayEquals(expected, actualArray);
        }
    }*/
    @Test
    void bBase() {
        Primitive64Store test = MatLab.linspace(204.83994725925928, 205.10565274074074, 1000);
        Primitive64Store actual = SplineBasisModel.bBase(test, 204.83994725925928, 205.10565274074074, 22, 3);
        MatrixStore<Double> expected = new SplineBasisModel(test, 22, 3).getBSplineMatrix();
        System.out.println(expected.toString());
        System.out.println(test);
        assertTrue(Arrays.deepEquals(expected.toRawCopy2D(), actual.toRawCopy2D()));

    }
}
