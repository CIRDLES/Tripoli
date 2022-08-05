package org.cirdles.tripoli.utilities.mathUtilities;


import org.apache.commons.math3.special.Gamma;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;


public class SplineBasisModel {

    private final MatrixStore<Double> BSplineMatrix;

    public SplineBasisModel(MatrixStore<Double> x, int numSegments, int basisDegree) {
        this.BSplineMatrix = bBase(x, numSegments, basisDegree);
    }


    public static Primitive64Store bBase(MatrixStore<Double> x, int numSegments, int basisDegree) {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        double[][] sk;
        double xLower = x.get(0, 0);
        double xUpper = x.get(x.getRowDim() - 1, x.getColDim() - 1);

        MatrixStore<Double> transposeX = x.transpose();

        double dx = (xUpper - xLower) / numSegments;
        Primitive64Store knots = MatLab.linspace(xLower - basisDegree * dx, xUpper + basisDegree * dx, numSegments + 2 * basisDegree + 1);

        int nx = transposeX.getRowDim();
        int nt = knots.getColDim();


        Primitive64Store kronTerm = storeFactory.make(1, nt);
        kronTerm.fillAll(1.0);

        MatrixStore<Double> matrixX = MatLab.kron(transposeX, kronTerm);


        Primitive64Store term2 = storeFactory.make(nx, 1);
        term2.fillAll(1.0);
        MatrixStore<Double> matrixT = MatLab.kron(knots, term2);

        Primitive64Store testTerm1 = MatLab.expMatrix(matrixX.subtract(matrixT), basisDegree);
        Primitive64Store testTerm2 = MatLab.greaterOrEqual(matrixX, matrixT);
        MatrixStore<Double> matrixP = MatLab.arrayMultiply(testTerm1, testTerm2);


        double v = (basisDegree + 1);
        MatrixStore<Double> matrixD = MatLab.diff(Primitive64Store.FACTORY.makeIdentity(nt), basisDegree + 1).divide((Gamma.gamma(v) * Math.pow(dx, basisDegree)));

        MatrixStore<Double> Base = matrixP.multiply(matrixD.transpose());

        int nb = MatLab.size(Base, 2);
        Primitive64Store kronTerm2 = storeFactory.make(1, nb);
        kronTerm2.fillAll(1.0);
        matrixX = MatLab.kron(transposeX, kronTerm2);

        sk = new double[1][nb];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < nb; j++) {
                sk[i][j] = knots.get(i, j + basisDegree + 1);
            }
        }

        Primitive64Store kronTerm3 = storeFactory.make(nx, 1);
        kronTerm3.fillAll(1.0);
        Primitive64Store SK = MatLab.kron(Primitive64Store.FACTORY.rows(sk), kronTerm3);
        Primitive64Store MASK = MatLab.lessThan(matrixX, SK);


        return MatLab.arrayMultiply(Base, MASK);
    }


    public static Primitive64Store bBase(MatrixStore<Double> x, double xl, double xr, double numSegments, int basisDegree) {
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        double[][] sk;
        double xLower;
        double xUpper;


        xLower = xl > x.get(0, 0) ? x.get(0, 0) : xl;
        xUpper = xr < x.get(x.getRowDim() - 1, x.getColDim() - 1) ? x.get(x.getRowDim() - 1, x.getColDim() - 1) : xr;

        MatrixStore<Double> transposeX = x.transpose();

        double dx = (xUpper - xLower) / numSegments;
        Primitive64Store knots = MatLab.linspace(xLower - basisDegree * dx, xUpper + basisDegree * dx, numSegments + 2 * basisDegree + 1);

        int nx = transposeX.getRowDim();
        int nt = knots.getColDim();


        Primitive64Store kronTerm = storeFactory.make(1, nt);
        kronTerm.fillAll(1.0);

        MatrixStore<Double> matrixX = MatLab.kron(transposeX, kronTerm);


        Primitive64Store term2 = storeFactory.make(nx, 1);
        term2.fillAll(1.0);
        MatrixStore<Double> matrixT = MatLab.kron(knots, term2);

        Primitive64Store testTerm1 = MatLab.expMatrix(matrixX.subtract(matrixT), basisDegree);
        Primitive64Store testTerm2 = MatLab.greaterOrEqual(matrixX, matrixT);
        MatrixStore<Double> matrixP = MatLab.arrayMultiply(testTerm1, testTerm2);


        double v = (basisDegree + 1);
        MatrixStore<Double> matrixD = MatLab.diff(Primitive64Store.FACTORY.makeIdentity(nt), basisDegree + 1).divide((Gamma.gamma(v) * Math.pow(dx, basisDegree)));

        MatrixStore<Double> Base = matrixP.multiply(matrixD.transpose());

        int nb = MatLab.size(Base, 2);
        Primitive64Store kronTerm2 = storeFactory.make(1, nb);
        kronTerm2.fillAll(1.0);
        matrixX = MatLab.kron(transposeX, kronTerm2);

        sk = new double[1][nb];
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < nb; j++) {
                sk[i][j] = knots.get(i, j + basisDegree + 1);
            }
        }

        Primitive64Store kronTerm3 = storeFactory.make(nx, 1);
        kronTerm3.fillAll(1.0);
        Primitive64Store SK = MatLab.kron(Primitive64Store.FACTORY.rows(sk), kronTerm3);
        Primitive64Store MASK = MatLab.lessThan(matrixX, SK);


        return MatLab.arrayMultiply(Base, MASK);
    }


    public MatrixStore<Double> getBSplineMatrix() {
        return BSplineMatrix;
    }
}