package org.cirdles.tripoli.utilities.mathUtilities;


import jama.Matrix;
import org.apache.commons.math3.special.Gamma;

public class SplineBasisModel {

    private final Matrix x; // vector of x values

    private final double basisDegree;

    private final double numSegments;

    private final Matrix BSplineMatrix;


    private SplineBasisModel() {
        this.x = null;
        this.basisDegree = 0;
        this.numSegments = 0;
        this.BSplineMatrix = null;
    }

    private SplineBasisModel(Matrix x, int numSegments, int basisDegree) {
        this.x = x;
        this.basisDegree = basisDegree;
        this.numSegments = numSegments;
        this.BSplineMatrix = bBase(x, numSegments, basisDegree);
    }


    public static SplineBasisModel initializeSpline(Matrix x, int numSegments, int basisDegree) {
        return new SplineBasisModel(x, numSegments, basisDegree);
    }


    public static Matrix bBase(Matrix x, int numSegments, int basisDegree) {
        double[][] sk;
        double xLower = x.get(0, 0);
        double xUpper = x.get(x.getRowDimension() - 1, x.getColumnDimension() - 1);

        double dx = (xUpper - xLower) / numSegments;
        Matrix knots = MatLab.linspace(xLower - basisDegree * dx, xUpper + basisDegree * dx, numSegments + 2 * basisDegree + 1);

        int nx = x.getColumnDimension();
        int nt = knots.getColumnDimension();

        Matrix matrixX = MatLab.kron(x, new Matrix(1, nt, 1).transpose()).transpose();
        Matrix matrixT = MatLab.kron(knots, new Matrix(nx, 1, 1));
        Matrix matrixP = MatLab.expMatrix(matrixX.minus(matrixT), basisDegree).arrayTimes(MatLab.greatEqual(matrixX, matrixT));

        double v = (basisDegree + 1);
        Matrix matrixD = MatLab.divMatrix(MatLab.diff(Matrix.identity(nt, nt), basisDegree + 1), (Gamma.gamma(v) * (Math.pow(dx, basisDegree))));
        Matrix Base = matrixP.times(matrixD.transpose());
        int nb = MatLab.size(Base, 2);
        matrixX = MatLab.kron(x, new Matrix(1, nb, 1).transpose()).transpose();

        sk = new double[1][nb];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < nb; j++) {
                sk[i][j] = knots.get(i, j + basisDegree + 1);
            }
        }
        Matrix SK = MatLab.kron(new Matrix(sk), new Matrix(nx, 1, 1));

        Matrix MASK = MatLab.lessThan(matrixX, SK);


        return Base.arrayTimes(MASK);
    }

    public static Matrix bBase(Matrix x, double xl, double xr, double numSegments, int basisDegree) {

        double[][] sk;
        double xLower;
        double xUpper;


        xLower = xl > x.get(0, 0) ? x.get(0, 0) : xl;
        xUpper = xr < x.get(x.getRowDimension() - 1, x.getColumnDimension() - 1) ? x.get(x.getRowDimension() - 1, x.getColumnDimension() - 1) : xr;


        double dx = (xUpper - xLower) / numSegments;
        Matrix knots = MatLab.linspace(xLower - basisDegree * dx, xUpper + basisDegree * dx, numSegments + 2 * basisDegree + 1);

        int nx = x.getColumnDimension();
        int nt = knots.getColumnDimension();


        Matrix matrixX = MatLab.kron(x, new Matrix(1, nt, 1).transpose()).transpose();

        Matrix matrixT = MatLab.kron(knots, new Matrix(nx, 1, 1));

        Matrix matrixP = MatLab.expMatrix(matrixX.minus(matrixT), basisDegree).arrayTimes(MatLab.greatEqual(matrixX, matrixT));

        double v = (basisDegree + 1);
        Matrix matrixD = MatLab.divMatrix(MatLab.diff(Matrix.identity(nt, nt), basisDegree + 1), (Gamma.gamma(v) * (Math.pow(dx, basisDegree))));
        Matrix Base = matrixP.times(matrixD.transpose());

        int nb = MatLab.size(Base, 2);
        matrixX = MatLab.kron(x, new Matrix(1, nb, 1).transpose()).transpose();

        sk = new double[1][nb];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < nb; j++) {
                sk[i][j] = knots.get(i, j + basisDegree + 1);
            }
        }
        Matrix SK = MatLab.kron(new Matrix(sk), new Matrix(nx, 1, 1));
        Matrix MASK = MatLab.lessThan(matrixX, SK);


        return Base.arrayTimes(MASK);
    }

    public double getBasisDegree() {
        return basisDegree;
    }

    public double getNumSegments() {
        return numSegments;
    }


    public Matrix getX() {
        return x;
    }

    public Matrix getBSplineMatrix() {
        return BSplineMatrix;
    }
}