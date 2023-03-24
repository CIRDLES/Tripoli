package org.cirdles.tripoli.utilities.mathUtilities;

import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.task.SolverTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public enum MatLab {
    ;

    public static double roundedToSize(double value, int sigFigs) {
        BigDecimal valueBDtoSize = BigDecimal.ZERO;
        if (Double.isFinite(value)) {
            BigDecimal valueBD = new BigDecimal(value);
            int newScale = sigFigs - (valueBD.precision() - valueBD.scale());
            valueBDtoSize = valueBD.setScale(newScale, RoundingMode.HALF_UP);
        }
        return valueBDtoSize.doubleValue();
    }

    /**
     * The Kronecker product
     *
     * @param A Matrix
     * @param B Matrix
     * @return the Kronecker product of matrices A and B
     */

    public static Primitive64Store kron(MatrixStore<Double> A, MatrixStore<Double> B) {
        int rowA = A.getRowDim();
        int colA = A.getColDim();
        int rowB = B.getRowDim();
        int colB = B.getColDim();

        double[][] newKron = new double[rowA * rowB][colA * colB];
        int kronRow = 0;
        for (int i = 0; i < rowA; i++) {
            for (int j = 0; j < rowB; j++) {
                kronRow++;
                int kronCol = 0;
                for (int k = 0; k < colA; k++) {
                    for (int h = 0; h < colB; h++) {
                        newKron[kronRow - 1][kronCol] = A.get(i, k) * B.get(j, h);
                        kronCol++;
                    }
                }
            }
        }
        return Primitive64Store.FACTORY.rows(newKron);

    }

    /**
     * Sets each element in matrix to the power deg
     *
     * @param A   Matrix
     * @param deg Degree exponent
     * @return A^deg
     */

    public static Primitive64Store expMatrix(MatrixStore<Double> A, int deg) {
        double[][] matrix = A.toRawCopy2D();
        int row = matrix.length;
        int col = matrix[0].length;
        double[][] mat = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                mat[i][j] = Math.pow(matrix[i][j], deg);
            }
        }

        return Primitive64Store.FACTORY.rows(mat);
    }

    /**
     * Multiplies matrices of the same dimension element by element
     *
     * @param A Matrix A
     * @param B Matrix B
     * @return A*B
     */
    public static Primitive64Store arrayMultiply(MatrixStore<Double> A, MatrixStore<Double> B) {
        int aRow = A.getRowDim();
        int aCol = A.getColDim();
        int bRow = B.getRowDim();
        int bCol = B.getColDim();
        double[][] mArray = new double[0][];

        if (aRow != bRow || aCol != bCol) {
            System.err.println("Either row or column dimensions do not match.");
        } else {
            mArray = new double[aRow][aCol];
            for (int i = 0; i < aRow; i++) {
                for (int j = 0; j < aCol; j++) {
                    mArray[i][j] = A.get(i, j) * B.get(i, j);
                }
            }

        }


        return Primitive64Store.FACTORY.rows(mArray);
    }

    /**
     * calculates differences between adjacent elements of X along the first array dimension whose size does not equal 1
     *
     * @param mat Matrix
     * @return Vector matrix of differences between elements
     */

    public static Primitive64Store diff(MatrixStore<Double> mat) {
        int row = mat.getRowDim();
        int col = mat.getColDim() - 1;
        double[][] newDiff;

        if (1 == row) {
            newDiff = new double[1][col];
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    newDiff[i][j] = mat.get(i, j + 1) - mat.get(i, j);
                }
            }
            return Primitive64Store.FACTORY.rows(newDiff);
        } else {
            newDiff = new double[row - 1][col + 1];
            for (int i = 0; i < row - 1; i++) {
                for (int j = 0; j < col + 1; j++) {
                    newDiff[i][j] = mat.get(i + 1, j) - mat.get(i, j);
                }
            }
            return Primitive64Store.FACTORY.rows(newDiff);
        }
    }

    /**
     * calculates the nth difference by applying the diff(mat) operator recursively num times.
     *
     * @param mat Matrix
     * @param num Number of iterations
     * @return Vector matrix of differences between elements recursively num times
     */

    public static MatrixStore<Double> diff(MatrixStore<Double> mat, int num) {
        MatrixStore<Double> refDiff;
        refDiff = mat;
        for (int i = 0; i < num; i++) {
            refDiff = diff(refDiff);
        }

        return refDiff;
    }


    /**
     * Compares param mat1 and mat2 and sets elements of new matrix to 1 or 0 based on if element
     * in mat1 is greater than or equal element in mat2
     *
     * @param mat1 Matrix
     * @param mat2 Matrix
     * @return A matrix of 1's and 0's based on if element in mat1 is greater than or equal to the element in mat2
     */
    public static Primitive64Store greaterOrEqual(MatrixStore<Double> mat1, MatrixStore<Double> mat2) {
        int maxRow = Math.min(mat1.getRowDim(), mat2.getRowDim());
        int maxCol = Math.min(mat1.getColDim(), mat2.getColDim());
        double[][] ge = new double[maxRow][maxCol];
        for (int k = 0; k < maxRow; k++) {
            for (int h = 0; h < maxCol; h++) {
                if (mat1.get(k, h) >= mat2.get(k, h)) {
                    ge[k][h] = 1;
                } else {
                    ge[k][h] = 0;
                }
            }
        }
        return Primitive64Store.FACTORY.rows(ge);
    }

    /**
     * Compares elements of matrix to number and determines if element is greater or equal to num
     *
     * @param mat Matrix
     * @param num number to be compared
     * @return A copy of param mat with elements either 1 or 0 based on if the element is greater than or equal to param
     */

    public static Primitive64Store greaterOrEqual(MatrixStore<Double> mat, double num) {
        int row = mat.getRowDim();
        int col = mat.getColDim();
        double[][] ge = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (mat.get(i, j) >= num) {
                    ge[i][j] = 1;
                } else {
                    ge[i][j] = 0;
                }
            }
        }

        return Primitive64Store.FACTORY.rows(ge);
    }

    /**
     * Returns a copy of param mat with elements either 1 or 0 based on if the element is greater than param
     * num
     *
     * @param mat Matrix
     * @param num Number compared
     */

    public static Primitive64Store greaterThan(MatrixStore<Double> mat, double num) {
        int row = mat.getRowDim();
        int col = mat.getColDim();
        double[][] ge = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (mat.get(i, j) > num) {
                    ge[i][j] = 1;
                } else {
                    ge[i][j] = 0;
                }
            }
        }

        return Primitive64Store.FACTORY.rows(ge);
    }

    /**
     * Returns a copy of param mat with elements either 1 or 0 based on if the element is less than or equal to param
     * num
     *
     * @param mat Matrix
     * @param num Number compared
     */

    public static Primitive64Store lessOrEqual(MatrixStore<Double> mat, double num) {
        int row = mat.getRowDim();
        int col = mat.getColDim();

        double[][] le = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (mat.get(i, j) <= num) {
                    le[i][j] = 1;
                } else {
                    le[i][j] = 0;
                }
            }
        }

        return Primitive64Store.FACTORY.rows(le);
    }

    /**
     * Returns a matrix comparing param mat1 and mat2 and sets elements of new matrix to 1 or 0 based on if element
     * in mat1 is less than element in mat2
     *
     * @param mat1 Matrix
     * @param mat2 Matrix
     */

    public static Primitive64Store lessThan(MatrixStore<Double> mat1, MatrixStore<Double> mat2) {
        int maxRow = Math.min(mat1.getRowDim(), mat2.getRowDim());
        int maxCol = Math.min(mat1.getColDim(), mat2.getColDim());

        double[][] lt = new double[maxRow][maxCol];
        for (int k = 0; k < maxRow; k++) {
            for (int h = 0; h < maxCol; h++) {
                if (mat1.get(k, h) < mat2.get(k, h)) {
                    lt[k][h] = 1;
                } else {
                    lt[k][h] = 0;
                }
            }
        }

        return Primitive64Store.FACTORY.rows(lt);
    }

    /**
     * Calculates the size of both the row or column size
     *
     * @param A   Matrix
     * @param num number compared
     * @return The size of the dimension in matrix A chosen by param num
     */

    public static int size(MatrixStore<Double> A, int num) {
        double[][] mat = A.toRawCopy2D();
        if (2 < num) {
            return -1;
        } else {
            int[] choice = {mat.length, mat[0].length};
            int matDim;
            matDim = choice[num - 1];
            return matDim;

        }
    }

    /**
     * Generates a linearly spaced vector of n points of (max - min)/(points - 1)
     *
     * @param min    min value
     * @param max    max value
     * @param points number of points spaced between
     * @return A vector matrix of linearly spaced vector
     */

    public static Primitive64Store linspace(double min, double max, double points) {
        double[][] d = new double[1][(int) points];
        for (int i = 0; i < points; i++) {
            d[0][i] = min + i * (max - min) / (points - 1);
        }
        return Primitive64Store.FACTORY.rows(d);
    }

    /**
     * Finds indices and values of nonzero elements and returns a vector of them
     * depending on param dir is 'last' finds the last n indices corresponding to nonzero elements in mat. And if param
     * dir is 'first' finds the first n indices corresponding to nonzero elements.
     *
     * @param mat Matrix mat
     * @param num Number of indices to find
     * @param dir Direction
     */

    public static Primitive64Store find(MatrixStore<Double> mat, int num, String dir) {
        double[][] found = new double[num][1];
        int row = mat.getRowDim();
        int col = mat.getColDim();
        int numCheck = 0;
        int i = 0;
        int index;
        if ("first".equalsIgnoreCase(dir)) {
            index = 0;
            for (int startCol = 0; startCol < col; startCol++) {
                for (int startRow = 0; startRow < row; startRow++) {
                    index++;
                    if (numCheck != num) {
                        if (0 < mat.get(startRow, startCol)) {
                            found[i][0] = index - 1;
                            numCheck++;
                            i++;
                        }

                    } else {
                        break;
                    }
                }

            }

        } else if ("last".equalsIgnoreCase(dir)) {
            index = (row * col) - 1;
            for (int startCol = col - 1; 0 <= startCol; startCol--) {
                for (int startRow = row - 1; 0 <= startRow; startRow--) {
                    index--;
                    if (numCheck != num) {
                        if (0 < mat.get(startRow, startCol)) {
                            found[i][0] = index + 1;
                            numCheck++;
                            i++;
                        }
                    } else {
                        break;
                    }
                }

            }
        }
        return Primitive64Store.FACTORY.rows(found);
    }

    /**
     * Determines if any array elements are nonzero. Returns elements along dimension dim. The dim input is a positive integer scalar.
     *
     * @param matrix Matrix matrix
     * @param dim    Dimension dim
     */

    public static Primitive64Store any(MatrixStore<Double> matrix, int dim) {
        int row = matrix.getRowDim();
        int col = matrix.getColDim();
        double[][] anyMat = new double[0][];
        double sum = 0;
        if (1 == dim) {
            anyMat = new double[1][col];

            for (int i = 0; i < col; i++) {
                for (int j = 0; j < row; j++) {
                    sum += matrix.get(j, i);

                }
                if (0 < sum) {
                    anyMat[0][i] = 1;
                } else {
                    anyMat[0][i] = 0;
                }
                sum = 0;

            }


        } else if (2 == dim) {
            anyMat = new double[row][1];

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    sum += matrix.get(i, j);

                }
                if (0 < sum) {
                    anyMat[i][0] = 1;
                } else {
                    anyMat[i][0] = 0;
                }
                sum = 0;

            }
        }

        return Primitive64Store.FACTORY.rows(anyMat);
    }


    /**
     * Divides param div by the elements in param A
     *
     * @param A   Matrix A
     * @param div dividend div
     * @return div/A
     */

    public static Primitive64Store rDivide(MatrixStore<Double> A, double div) {
        int row = A.getRowDim();
        int col = A.getColDim();
        double[][] divMat = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                divMat[i][j] = div / A.get(i, j);
            }
        }

        return Primitive64Store.FACTORY.rows(divMat);
    }

    /**
     * returns the maximum element along dimension dim.
     *
     * @param matrix Matrix matrix
     * @param max    comparable max
     * @return max > matrix
     */

    public static Primitive64Store max(MatrixStore<Double> matrix, int max) {
        int row = matrix.getRowDim();
        int col = matrix.getColDim();
        double[][] maxMat = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                maxMat[i][j] = (max > matrix.get(i, j)) ? max : matrix.get(i, j);
            }
        }
        return Primitive64Store.FACTORY.rows(maxMat);
    }

    /**
     * returns a square diagonal matrix with the elements of param mat on the main diagonal.
     *
     * @param mat Matrix mat
     */

    public static Primitive64Store diag(MatrixStore<Double> mat) {
        int size = Math.max(mat.getRowDim(), mat.getColDim());
        double[][] diagMat = new double[size][size];
        int dag = 0;

        for (int i = 0; i < diagMat.length; i++) {
            for (int j = 0; j < diagMat[0].length; j++) {
                if (i == j) {
                    diagMat[i][j] = mat.get(dag, 0);
                    dag++;
                } else {
                    diagMat[i][j] = 0;
                }
            }
        }

        return Primitive64Store.FACTORY.rows(diagMat);
    }

//    /**
//     * Concatenates 2 matrices together creating an even larger matrix of the dimension size of each matrix row size
//     * and column size added together.
//     *
//     * @param A Matrix A
//     * @param B Matrix B
//     */
//    public static Matrix concatMatrix(Matrix A, Matrix B) {
//        Matrix concated = new Matrix(A.getRowDimension() + B.getRowDimension(), A.getColumnDimension());
//        int indexBRow = 0;
//
//
//        for (int i = 0; i < A.getRowDimension(); i++) {
//            for (int j = 0; j < concated.getColumnDimension(); j++) {
//                concated.set(i, j, A.get(i, j));
//            }
//        }
//
//        for (int i = A.getRowDimension(); i < concated.getRowDimension(); i++) {
//            indexBRow++;
//            int indexBCol = 0;
//            for (int j = 0; j < concated.getColumnDimension(); j++) {
//                concated.set(i, j, B.get(indexBRow - 1, indexBCol));
//                indexBCol++;
//            }
//
//        }
//
//        return concated;
//    }
//
//    /**
//     * returns the block diagonal matrix created by aligning the input matrices A, B along the diagonal of new matrix.
//     *
//     * @param A Matrix A
//     * @param B Matrix B
//     */
//    public static Matrix blockDiag(Matrix A, Matrix B) {
//        Matrix diag = new Matrix(A.getRowDimension() + B.getRowDimension(), A.getRowDimension() + B.getRowDimension());
//        int indexBRow = 0;
//        int indexBCol = 0;
//
//        for (int i = 0; i < A.getRowDimension(); i++) {
//            for (int j = 0; j < A.getColumnDimension(); j++) {
//                if (i == j) {
//                    diag.set(i, j, A.get(i, j));
//                } else {
//                    diag.set(i, j, 0);
//                }
//
//            }
//        }
//
//        for (int i = A.getRowDimension(); i < diag.getRowDimension(); i++) {
//
//
//            for (int j = A.getColumnDimension(); j < diag.getColumnDimension(); j++) {
//                if (i == j) {
//                    diag.set(i, j, B.get(indexBRow, indexBCol));
//                    indexBCol++;
//                    indexBRow++;
//                }
//
//
//            }
//
//        }
//        return diag;
//    }

    // * Copyright 2008 Josh Vermaas, except he's nice and instead prefers
    // * this to be licensed under the LGPL. Since the license itself is longer
    // * than the code, if this truly worries you, you can look up the text at
    // * http://www.gnu.org/licenses/

    public static MatrixStore<Double> solveNNLS(MatrixStore<Double> A, MatrixStore<Double> b) throws RecoverableCondition {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        List<Integer> p = new ArrayList<>();
        List<Integer> z = new ArrayList<>();
        int i = 0;
        int xm = A.getColDim();
        int xn = 1;
        while (i < A.getColDim()) {
            z.add(i);
            i++;
        }
        Primitive64Store x = storeFactory.make(xm, xn);
        /*
         * You need a finite number of iterations. Without this condition, the finite precision nature
         * of the math being done almost makes certain that the <1e-15 conditions won't ever hold up.
         * However, after so many iterations, it should at least be close to the correct answer.
         * For the intrepid coder, however, one could replace this again with an infinite while
         * loop and make the <1e-15 conditions into something like c*norm(A) or c*norm(b).
         */
        for (int iterations = 0; iterations < 300 * A.getColDim() * A.getRowDim(); iterations++) {
            //System.out.println(z.size() + " " + p.size());
            MatrixStore<Double> w = A.transpose().multiply(b.subtract(A.multiply(x)));
            //w.print(7, 5);
            if (0 == z.size() || isAllNegative(w)) {
                //System.out.println("Computation should break");
                //We are done with the computation. Break here!
                break;//Should break out of the outer while loop.
            }
            //Step 4
            int t = z.get(0);
            double max = w.get(t, 0);
            for (i = 1; i < z.size(); i++) {
                if (w.get(z.get(i), 0) > max) {
                    t = z.get(i);
                    max = w.get(z.get(i), 0);
                }
            }
            //Step 5
            p.add(t);
            z.remove((Integer) t);
            boolean allPositive = false;
            while (!allPositive) {
                //Step 6
                Primitive64Store Ep = storeFactory.make(b.getRowDim(), p.size());
                for (i = 0; i < p.size(); i++)
                    for (int j = 0; j < Ep.getRowDim(); j++)
                        Ep.set(j, i, A.get(j, p.get(i)));
                SolverTask<Double> solverTask = SolverTask.PRIMITIVE.make(Ep, b);
                MatrixStore<Double> Zprime = solverTask.solve(Ep, b);
                Ep = null;
                Primitive64Store Z = storeFactory.make(xm, xn);
                for (i = 0; i < p.size(); i++)
                    Z.set((long) p.get(i), 0, Zprime.get(i, 0));
                //Step 7
                allPositive = true;
                for (i = 0; i < p.size(); i++)
                    allPositive &= 0 < Z.get(p.get(i), 0);
                if (allPositive)
                    x = Z;
                else {
                    double alpha = Double.MAX_VALUE;
                    for (i = 0; i < p.size(); i++) {
                        int q = p.get(i);
                        if (0 >= Z.get(q, 0)) {
                            double xq = x.get(q, 0);
                            if (xq / (xq - Z.get(q, 0)) < alpha)
                                alpha = xq / (xq - Z.get(q, 0));
                        }
                    }
                    //Finished getting alpha. Onto step 10
                    x = (Primitive64Store) x.add(Z.subtract(x).multiply(alpha));
                    for (i = p.size() - 1; 0 <= i; i--)
                        if (1.0e-10 > Math.abs(x.get(p.get(i), 0)))//Close enough to zero, no?
                            z.add(p.remove(i));
                }
            }
        }
        return x;
    }

    public static boolean isAllNegative(MatrixStore<Double> w) {
        boolean result = true;
        int m = w.getRowDim();
        for (int i = 0; i < m; i++)
            result &= 1.0e-10 >= w.get(i, 0);
        return result;
    }


    public static double normInf(MatrixStore<Double> mat) {
        double[][] A = mat.toRawCopy2D();
        int m = mat.getRowDim();
        int n = mat.getColDim();
        double f = 0;
        for (int i = 0; i < m; i++) {
            double s = 0;
            for (int j = 0; j < n; j++) {
                s += Math.abs(A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }
}