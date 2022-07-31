package org.cirdles.tripoli.utilities.mathUtilities;


import jama.Matrix;
import org.apache.commons.math3.special.Gamma;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;


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

    public static Primitive64Store bBase(Primitive64Store x, double xl, double xr, double numSegments, int basisDegree) {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        double[][] sk;
        double xLower;
        double xUpper;


        xLower = xl > x.get(0, 0) ? x.get(0, 0) : xl;
        xUpper = xr < x.get(x.getRowDim() - 1, x.getColDim() - 1) ? x.get(x.getRowDim() - 1, x.getColDim() - 1) : xr;


        double dx = (xUpper - xLower) / numSegments;
        Primitive64Store knotsOJ = MatLab.linspaceOJ(xLower - basisDegree * dx, xUpper + basisDegree * dx, numSegments + 2 * basisDegree + 1);

        int nx = x.getColDim();
        int nt = knotsOJ.getColDim();


        Primitive64Store kronTerm = storeFactory.make(1, nt);
        kronTerm.fillAll(1.0);

        MatrixStore<Double> matrixXOJ = MatLab.kronOJ(x, kronTerm.transpose());
        matrixXOJ = matrixXOJ.transpose();


        Primitive64Store term2 = storeFactory.make(nx, 1);
        term2.fillAll(1.0);
        MatrixStore<Double> matrixTOJ = MatLab.kronOJ(knotsOJ, term2);

        Primitive64Store testTerm1 = MatLab.expMatrix(matrixXOJ.subtract(matrixTOJ), basisDegree);
        Primitive64Store testTerm2 = MatLab.greatEqual(matrixXOJ, matrixTOJ);
        MatrixStore<Double> matrixPOJ = MatLab.arrayMultiply(testTerm1, testTerm2);


        double v = (basisDegree + 1);
        MatrixStore<Double> matrixDOJ = MatLab.diff(Primitive64Store.FACTORY.makeIdentity(nt), basisDegree + 1).divide((Gamma.gamma(v) * Math.pow(dx, basisDegree)));

        MatrixStore<Double> BaseOJ = matrixPOJ.multiply(matrixDOJ.transpose());

        int nb = MatLab.size(BaseOJ, 2);
        Primitive64Store kronTerm2 = storeFactory.make(nb, 1);
        kronTerm2.fillAll(1.0);
        matrixXOJ = MatLab.kronOJ(x, kronTerm2);
        matrixXOJ = matrixXOJ.transpose();

        sk = new double[1][nb];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < nb; j++) {
                sk[i][j] = knotsOJ.get(i, j + basisDegree + 1);
            }
        }

        Primitive64Store kronTerm3 = storeFactory.make(nx, 1);
        kronTerm3.fillAll(1.0);
        Primitive64Store SKOJ = MatLab.kronOJ(Primitive64Store.FACTORY.rows(sk), kronTerm3);


        Primitive64Store MASKOJ = MatLab.lessThan(matrixXOJ, SKOJ);


        return MatLab.arrayMultiply(BaseOJ, MASKOJ);
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