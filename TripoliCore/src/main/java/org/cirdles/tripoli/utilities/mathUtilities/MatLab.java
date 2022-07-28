package org.cirdles.tripoli.utilities.mathUtilities;

import jama.Matrix;

import java.util.ArrayList;
import java.util.List;

public class MatLab {
    /**
     * The Kronecker product
     *
     * @param A Matrix
     * @param B Matrix
     * @return the Kronecker product of matrices A and B
     */
    public static Matrix kron(Matrix A, Matrix B) {
        int rowA = A.getRowDimension();
        int colA = A.getColumnDimension();
        int rowB = B.getRowDimension();
        int colB = B.getColumnDimension();

        double[][] newKron = new double[rowA * rowB][colA * colB];
        if (rowB == 1) {
            int kronRow = 0;
            for (int i = 0; i < rowA; i++) {
                kronRow++;
                for (int j = 0; j < rowB; j++) {

                    int kronCol = 0;
                    for (int k = 0; k < colA; k++) {

                        for (int h = 0; h < colB; h++) {
                            newKron[kronRow - 1][kronCol] = A.get(i, k) * B.get(j, h);
                            kronCol++;
                        }
                    }
                }
            }

            return new Matrix(newKron);

        } else {
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
            return new Matrix(newKron);
        }

    }

    /**
     * Sets each element in matrix to the power deg
     *
     * @param A   Matrix
     * @param deg Degree exponent
     * @return A^deg
     */
    public static Matrix expMatrix(Matrix A, int deg) {
        double[][] matrix = A.getArray();
        int row = matrix.length;
        int col = matrix[0].length;
        double[][] mat = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                mat[i][j] = Math.pow(matrix[i][j], deg);
            }
        }

        return new Matrix(mat);
    }

    /**
     * Returns matrix of each element of matrix A by param divide
     *
     * @param A      Matrix
     * @param divide dividend
     * @return A/div
     */
    public static Matrix divMatrix(Matrix A, double divide) {
        for (int i = 0; i < A.getRowDimension(); i++) {
            for (int j = 0; j < A.getColumnDimension(); j++) {
                A.set(i, j, A.get(i, j) / divide);
            }
        }

        return A;
    }

    /**
     * calculates differences between adjacent elements of X along the first array dimension whose size does not equal 1
     *
     * @param mat Matrix
     * @return Vector matrix of differences between elements
     */
    public static Matrix diff(Matrix mat) {
        int row = mat.getRowDimension();
        int col = mat.getColumnDimension() - 1;
        double[][] newDiff;

        if (row == 1) {
            newDiff = new double[row][col];
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    newDiff[i][j] = (mat.get(i, j) - mat.get(i, j + 1));
                }
            }
            return new Matrix(newDiff);
        } else {
            newDiff = new double[row - 1][col + 1];
            for (int i = 0; i < row - 1; i++) {
                for (int j = 0; j < col + 1; j++) {
                    newDiff[i][j] = mat.get(i, j) - mat.get(i + 1, j);
                }
            }
            return new Matrix(newDiff);
        }
    }

    /**
     * calculates the nth difference by applying the diff(mat) operator recursively num times.
     *
     * @param mat Matrix
     * @param num Number of iterations
     * @return Vector matrix of differences between elements recursively num times
     */
    public static Matrix diff(Matrix mat, int num) {
        Matrix refDiff;
        refDiff = mat;
        for (int i = 0; i < num; i++) {
            refDiff = diff(refDiff);
        }

        return refDiff;
    }

    /**
     * Compares param mat1 and mat2 and sets elements of new matrix to 1 or 0 based on if element
     * in mat1 is greater than element in mat2
     *
     * @param mat1 Matrix
     * @param mat2 Matrix
     * @return A matrix of 1's and 0's based on if element in mat1 is greater than element in mat2
     */
    public static Matrix greatEqual(Matrix mat1, Matrix mat2) {
        int maxRow = Math.min(mat1.getRowDimension(), mat2.getRowDimension());
        int maxCol = Math.min(mat1.getColumnDimension(), mat2.getColumnDimension());
        int i, j = 0;
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
        return new Matrix(ge);
    }

    /**
     * Compares elements of matrix to number and determines if element is greater or equal to num
     *
     * @param mat Matrix
     * @param num number to be compared
     * @return A copy of param mat with elements either 1 or 0 based on if the element is greater than or equal to param
     */
    public static Matrix greaterOrEqual(Matrix mat, double num) {
        int row = mat.getRowDimension();
        int col = mat.getColumnDimension();
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

        return new Matrix(ge);
    }

    /**
     * Returns a copy of param mat with elements either 1 or 0 based on if the element is greater than param
     * num
     *
     * @param mat Matrix
     * @param num Number compared
     */
    public static Matrix greaterThan(Matrix mat, double num) {
        int row = mat.getRowDimension();
        int col = mat.getColumnDimension();
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

        return new Matrix(ge);
    }

    /**
     * Returns a copy of param mat with elements either 1 or 0 based on if the element is less than or equal to param
     * num
     *
     * @param mat Matrix
     * @param num Number compared
     */
    public static Matrix lessOrEqual(Matrix mat, double num) {
        int row = mat.getRowDimension();
        int col = mat.getColumnDimension();

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

        return new Matrix(le);
    }

    /**
     * Returns a matrix comparing param mat1 and mat2 and sets elements of new matrix to 1 or 0 based on if element
     * in mat1 is less than element in mat2
     *
     * @param mat1 Matrix
     * @param mat2 Matrix
     */
    public static Matrix lessThan(Matrix mat1, Matrix mat2) {
        int maxRow = Math.min(mat1.getRowDimension(), mat2.getRowDimension());
        int maxCol = Math.min(mat1.getColumnDimension(), mat2.getColumnDimension());
        int i, j = 0;
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

        return new Matrix(lt);
    }

    /**
     * Calculates the size of both the row or column size
     *
     * @param A   Matrix
     * @param num number compared
     * @return The size of the dimension in matrix A chosen by param num
     */
    public static int size(Matrix A, int num) {
        double[][] mat = A.getArray();
        if (num > 2) {
            return -1;
        } else {
            int[] choice = new int[]{mat.length, mat[0].length};
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
    public static Matrix linspace(double min, double max, double points) {
        double[][] d = new double[1][(int) points];
        for (int i = 0; i < points; i++) {
            d[0][i] = min + i * (max - min) / (points - 1);
        }
        return new Matrix(d);
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
    public static Matrix find(Matrix mat, int num, String dir) {
        double[][] found = new double[num][1];
        int row = mat.getRowDimension();
        int col = mat.getColumnDimension();
        int numCheck = 0;
        int i = 0;
        int index;
        if (dir.equalsIgnoreCase("first")) {
            index = 0;
            for (int startCol = 0; startCol < col; startCol++) {
                for (int startRow = 0; startRow < row; startRow++) {
                    index++;
                    if (numCheck != num) {
                        if (mat.get(startRow, startCol) > 0) {
                            found[i][0] = index - 1;
                            numCheck++;
                            i++;
                        }

                    } else {
                        break;
                    }
                }

            }

        } else if (dir.equalsIgnoreCase("last")) {
            index = (row * col) - 1;
            for (int startCol = col - 1; startCol >= 0; startCol--) {
                for (int startRow = row - 1; startRow >= 0; startRow--) {
                    index--;
                    if (numCheck != num) {
                        if (mat.get(startRow, startCol) > 0) {
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
        return new Matrix(found);
    }

    /**
     * Determines if any array elements are nonzero. Returns elements along dimension dim. The dim input is a positive integer scalar.
     *
     * @param matrix Matrix matrix
     * @param dim    Dimension dim
     */
    public static Matrix any(Matrix matrix, int dim) {
        int row = matrix.getRowDimension();
        int col = matrix.getColumnDimension();
        double[][] anyMat = null;
        double sum = 0;
        if (dim == 1) {
            anyMat = new double[1][col];

            for (int i = 0; i < col; i++) {
                for (int j = 0; j < row; j++) {
                    sum += matrix.get(j, i);

                }
                if (sum > 0) {
                    anyMat[0][i] = 1;
                } else {
                    anyMat[0][i] = 0;
                }
                sum = 0;

            }


        } else if (dim == 2) {
            anyMat = new double[row][1];

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    sum += matrix.get(i, j);

                }
                if (sum > 0) {
                    anyMat[i][0] = 1;
                } else {
                    anyMat[i][0] = 0;
                }
                sum = 0;

            }
        }


        assert anyMat != null;
        return new Matrix(anyMat);
    }

    /**
     * Divides param div by the elements in param A
     *
     * @param A   Matrix A
     * @param div dividend div
     * @return div/A
     */
    public static Matrix rDivide(Matrix A, double div) {
        int row = A.getRowDimension();
        int col = A.getColumnDimension();
        double[][] divMat = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                divMat[i][j] = div / A.get(i, j);
            }
        }

        return new Matrix(divMat);
    }

    /**
     * returns the maximum element along dimension dim.
     *
     * @param matrix Matrix matrix
     * @param max    comparable max
     * @return max > matrix
     */
    public static Matrix max(Matrix matrix, int max) {
        int row = matrix.getRowDimension();
        int col = matrix.getColumnDimension();
        double[][] maxMat = new double[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                maxMat[i][j] = (max > matrix.get(i, j)) ? max : matrix.get(i, j);
            }
        }
        return new Matrix(maxMat);
    }

    /**
     * returns a square diagonal matrix with the elements of param mat on the main diagonal.
     *
     * @param mat Matrix mat
     */
    public static Matrix diag(Matrix mat) {
        int row = mat.getRowDimension();
        double[][] diagMat = new double[row][row];
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

        return new Matrix(diagMat);
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
    public static Matrix solveNNLS(Matrix A, Matrix b) {
        List<Integer> p = new ArrayList<Integer>();
        List<Integer> z = new ArrayList<Integer>();
        int i = 0;
        int xm = A.getColumnDimension();
        int xn = 1;
        while (i < A.getColumnDimension())
            z.add(i++);
        Matrix x = new Matrix(xm, xn);
        /*
         * You need a finite number of iterations. Without this condition, the finite precision nature
         * of the math being done almost makes certain that the <1e-15 conditions won't ever hold up.
         * However, after so many iterations, it should at least be close to the correct answer.
         * For the intrepid coder, however, one could replace this again with an infinite while
         * loop and make the <1e-15 conditions into something like c*norm(A) or c*norm(b).
         */
        for (int iterations = 0; iterations < 300 * A.getColumnDimension() * A.getRowDimension(); iterations++) {
            //System.out.println(z.size() + " " + p.size());
            Matrix w = A.transpose().times(b.minus(A.times(x)));
            //w.print(7, 5);
            if (z.size() == 0 || isAllNegative(w)) {
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
                Matrix Ep = new Matrix(b.getRowDimension(), p.size());
                for (i = 0; i < p.size(); i++)
                    for (int j = 0; j < Ep.getRowDimension(); j++)
                        Ep.set(j, i, A.get(j, p.get(i)));
                Matrix Zprime = Ep.solve(b);
                Ep = null;
                Matrix Z = new Matrix(xm, xn);
                for (i = 0; i < p.size(); i++)
                    Z.set(p.get(i), 0, Zprime.get(i, 0));
                //Step 7
                allPositive = true;
                for (i = 0; i < p.size(); i++)
                    allPositive &= Z.get(p.get(i), 0) > 0;
                if (allPositive)
                    x = Z;
                else {
                    double alpha = Double.MAX_VALUE;
                    for (i = 0; i < p.size(); i++) {
                        int q = p.get(i);
                        if (Z.get(q, 0) <= 0) {
                            double xq = x.get(q, 0);
                            if (xq / (xq - Z.get(q, 0)) < alpha)
                                alpha = xq / (xq - Z.get(q, 0));
                        }
                    }
                    //Finished getting alpha. Onto step 10
                    x = x.plus(Z.minus(x).times(alpha));
                    for (i = p.size() - 1; i >= 0; i--)
                        if (Math.abs(x.get(p.get(i), 0)) < 1e-15)//Close enough to zero, no?
                            z.add(p.remove(i));
                }
            }
        }
        return x;
    }

    public static boolean isAllNegative(Matrix w) {
        boolean result = true;
        int m = w.getRowDimension();
        for (int i = 0; i < m; i++)
            result &= w.get(i, 0) <= 1e-15;
        return result;
    }
}