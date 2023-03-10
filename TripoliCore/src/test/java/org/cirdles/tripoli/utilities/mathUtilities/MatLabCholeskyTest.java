package org.cirdles.tripoli.utilities.mathUtilities;

import org.cirdles.tripoli.Tripoli;
import org.junit.jupiter.api.Test;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import jama.EigenvalueDecomposition;
import jama.Matrix;

import org.cirdles.commons.util.ResourceExtractor;

import static org.junit.jupiter.api.Assertions.*;

class MatLabCholeskyTest{
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
        System.out.println(Double.toString(m) + " : " + Double.toString(n));
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
        String filename = "src/test/resources/org/cirdles/tripoli/core/" + fn;
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
    public void cholCovTest() throws IOException {
        ArrayList<double[][]> aList = read_csv("exp_matrix_A.txt");
        ArrayList<double[][]> answerList = read_csv("exp_answers.txt");
        double[][] expected;
        Matrix actual;
        Matrix a;
        double[][] actualArray;
        for (int i = 0; i < answerList.size(); i++) {
            a = new Matrix(aList.get(i));

            expected = answerList.get(i);
            actual = MatLabCholesky.cholCov(a);
            actualArray = actual.getArray();

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
}