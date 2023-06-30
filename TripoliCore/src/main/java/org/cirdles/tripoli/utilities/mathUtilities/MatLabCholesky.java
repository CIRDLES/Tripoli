/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cirdles.tripoli.utilities.mathUtilities;

import jama.CholeskyDecomposition;
import jama.Matrix;
import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * @author James F. Bowring
 */
public enum MatLabCholesky {
    ;
//    static RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
     /*
        if (n == m) && all(all(abs(Sigma - Sigma') < n*tol))
            [T,p] = chol(Sigma);
            if p > 0
                % Test for positive definiteness
                if flag
                    % Can get factors of the form Sigma==T'*T using the eigenvalue
                    % decomposition of a symmetric matrix, so long as the matrix
                    % is positive semi-definite.
                    [U,D] = eig(full((Sigma+Sigma')/2));
                    % Pick eigenvector direction so max abs coordinate is positive
                    [~,maxind] = max(abs(U),[],1);
                    negloc = (U(maxind + (0:n:(m-1)*n)) < 0);
                    U(:,negloc) = -U(:,negloc);
                    D = diag(D);
                    tol = eps(max(D)) * length(D);
                    t = (abs(D) > tol);
                    D = D(t);
                    p = sum(D<0); % number of negative eigenvalues
                    if (p==0)
                        T = diag(sqrt(D)) * U(:,t)';
                    else
                        T = zeros(0,'like',Sigma);
                    end
                else
                    T = zeros(0,'like',Sigma);
                end
            end

        else
            T = zeros(0,'like',Sigma);
            p = nan('like',Sigma);
        end

        if wassparse
            T = sparse(T);
        end

     */

    /**
     * Stripped down matlab function
     * R = MVNRND(MU,SIGMA,N) returns a N-by-D matrix R of random vectors
     * %   chosen from the multivariate normal distribution with 1-by-D mean
     * %   vector MU, and D-by-D covariance matrix SIGMA.
     *
     * @param myMu
     * @param sigma
     * @param cases
     * @return
     */
    public static Matrix mvnrndTripoli(double[] myMu, double[][] sigma, int cases) {

        // mu = repmat(mu,n,1);
        double[][] mu = new double[cases][];
        for (int row = 0; row < cases; row++) {
            mu[row] = myMu;
        }
        /*
                % Factor sigma using a function that will perform a Cholesky-like
        % factorization as long as the sigma matrix is positive
        % semi-definite (can have perfect correlation). Cholesky requires a
        % positive definite matrix.  sigma == T'*T
        [T,err] = cholCov(sigma);%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

        r = randn(n,size(T,1),'like',outtype) * T + mu;
        t = diag(sigma);
        r(:,t==0) = mu(:,t==0); % force exact mean when variance is 0
         */

        //The standard normal distribution (z distribution) is a normal distribution with a mean of 0 and a standard deviation of 1.
        // Any point (x) from a normal distribution can be converted to the standard normal distribution (z) with the formula
        // z = (x-mean) / standard deviation. z for any particular x value shows how many standard deviations x is away from the mean for all x values.
        // For example, if 1.4m is the height of a school pupil where the mean for pupils of his age/sex/ethnicity is 1.2m with a standard deviation of 0.4
        // then z = (1.4-1.2) / 0.4 = 0.5, i.e. the pupil is half a standard deviation from the mean (value at centre of curve).

        Matrix T = cholCov(new Matrix(sigma));
        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        randomDataGenerator.reSeedSecure();

        double[][] rArray = new double[cases][T.getRowDimension()];
        for (int row = 0; row < cases; row++) {
            for (int col = 0; col < T.getRowDimension(); col++) {
                rArray[row][col] = randomDataGenerator.nextGaussian(0.0, 1.0);
            }
        }

        Matrix r = (new Matrix(rArray)).times(T).plus(new Matrix(mu));

        return r;
    }
        /*
            function [T,p] = cholCov(Sigma,flag)
        %CHOLCOV  Cholesky-like decomposition for covariance matrix.
        %   T = CHOLCOV(SIGMA) computes T such that SIGMA = T'*T.  SIGMA must be
        %   square, symmetric, and positive semi-definite.  If SIGMA is positive
        %   definite, then T is the square, upper triangular Cholesky factor.
        %
        %   If SIGMA is not positive definite, T is computed from an eigenvalue
        %   decomposition of SIGMA.  T is not necessarily triangular or square in
        %   this case.  Any eigenvectors whose corresponding eigenvalue is close to
        %   zero (within a small tolerance) are omitted.  If any remaining
        %   eigenvalues are negative, T is empty.
        %
        %   [T,P] = CHOLCOV(SIGMA) returns the number of negative eigenvalues of
        %   SIGMA, and T is empty if P>0.  If P==0, SIGMA is positive semi-definite.
        %
        %   If SIGMA is not square and symmetric, P is NaN and T is empty.
        %
        %   [T,P] = CHOLCOV(SIGMA,0) returns P==0 if SIGMA is positive definite, and
        %   T is the Cholesky factor.  If SIGMA is not positive definite, P is a
        %   positive integer and T is empty.  [...] = CHOLCOV(SIGMA,1) is equivalent
        %   to [...] = CHOLCOV(SIGMA).
        %
        %   Example:
        %   Factor a rank-deficient covariance matrix C.
        %       C = [2 1 1 2;1 2 1 2;1 1 2 2;2 2 2 3]
        %       T = cholCov(C)
        %       C2 = T'*T
        %   Generate data with this covariance (aside from random variation).
        %       C3 = cov(randn(10000,3)*T)
        %
        %   See also CHOL.

        %   Copyright 1993-2017 The MathWorks, Inc.
        if nargin < 2, flag = 1; end

        % Test for square, symmetric
        [n,m] = size(Sigma);
        wassparse = issparse(Sigma);
        tol = 10*eps(max(abs(diag(Sigma))));
        */

    /**
     * Special port of Matlab function - assume square sigma
     *
     * @param sigma
     */
    public static Matrix cholCov(Matrix sigma) {
//        int n = sigma.getRowDimension();
//        double max = Double.MIN_VALUE;
//        for (int row = 0; row < n; row++) {
//            if (Math.abs(sigma.get(row, row)) > max) {
//                max = sigma.get(row, row);
//            }
//        }
//        double[][] tArray = new double[1][1];
//        Matrix matrixT = new Matrix(tArray);
//        double tol = 10.0 * ulp(max);
//        if (all(sigma.minus(sigma.transpose()).getArray(), "<", n * tol)) {
//            EigenvalueDecomposition eig = sigma.plus(sigma.transpose().times(0.5)).eig();
//            Matrix U = eig.getV();
//            Matrix D = eig.getD();
//            // create int[] with indices of greatest ABS value in each column of U
//            // make that entry positive
//            // also process D
//            // column vector
//            /*
//            % Pick eigenvector direction so max abs coordinate is positive
//            [~,maxind] = max(abs(U),[],1);
//            negloc = (U(maxind + (0:n:(m-1)*n)) < 0);
//            U(:,negloc) = -U(:,negloc);
//
//            D = diag(D);
//            tol = eps(max(D)) * length(D);
//            t = (abs(D) > tol);
//            D = D(t);
//            p = sum(D<0); % number of negative eigenvalues
//
//            if (p==0)
//                matrixT = diag(sqrt(D)) * U(:,t)';
//            else
//                matrixT = zeros(0,'like',Sigma);
//            end
//             */
//            double[][] diagDasColumn = new double[D.getRowDimension()][1];
//            double maxValueDiagD = Double.MIN_VALUE;
//            var columnDimensionU = U.getColumnDimension();
//            for (int col = 0; col < columnDimensionU; col++) {
//                int indexOfMax = -1;
//                double maxValueColU = Double.MIN_VALUE;
//                for (int row = 0; row < U.getRowDimension(); row++) {
//                    if (Math.abs(U.get(row, col)) > maxValueColU) {
//                        maxValueColU = Math.abs(U.get(row, col));
//                        indexOfMax = row;
//                    }
//                }
//
//                if (0.0 > U.get(indexOfMax, col)) {
//                    for (int row = 0; row < U.getRowDimension(); row++) {
//                        U.set(row, col, -U.get(row, col));
//                    }
//                }
//                for (int row = 0; row < U.getRowDimension(); row++) {
//                    if (col == row) {
//                        diagDasColumn[row][0] = D.get(row, col);
//                        if (diagDasColumn[row][0] > maxValueDiagD) {
//                            maxValueDiagD = diagDasColumn[row][0];
//                        }
//                    }
//                }
//            }
//            tol = ulp(maxValueDiagD) * diagDasColumn.length;
//            // remove elements of diagDasColumn that are less than tol and then count negative values
//            int countOfNegativeValues = 0;
//            List<Integer> tList = new ArrayList<>();
//            for (int row = 0; row < diagDasColumn.length; row++) {
//                if (Math.abs(diagDasColumn[row][0]) <= tol) {
//                    diagDasColumn[row][0] = 0.0;
//                } else {
//                    tList.add(row);
//                }
//                if (0.0 > diagDasColumn[row][0]) {
//                    countOfNegativeValues++;
//                }
//            }
//
//            int[] t = Ints.toArray(tList);
//            Matrix uOfT = U.getMatrix(0, U.getRowDimension() - 1, t);
//            Matrix diagDasColumnOfT = (new Matrix(diagDasColumn)).getMatrix(t, 0, 0);
//            double[][] diagDasColumnOfTarray = diagDasColumnOfT.getArray();
//
//            for (int row = 0; row < diagDasColumnOfTarray.length; row++) {
//                diagDasColumnOfTarray[row][0] = StrictMath.sqrt(diagDasColumnOfTarray[row][0]);
//            }
//            Matrix DofTDiagonalMatrix = new Matrix(diagDasColumnOfTarray.length, diagDasColumnOfTarray.length);
//            for (int row = 0; row < diagDasColumnOfTarray.length; row++) {
//                DofTDiagonalMatrix.set(row, row, diagDasColumnOfTarray[row][0]);
//            }
//
//            if (0 == countOfNegativeValues) {//(chol.isSPD()){
//                // matrixT = diag(sqrt(D)) * U(:,t)';
//                matrixT = DofTDiagonalMatrix.times(uOfT.transpose());
//            } else {
//                matrixT = new Matrix(new double[1][1]);
//            }
//        }

        // June 2023 - for Tripoli MCMC, just need the following
        CholeskyDecomposition choleskyDecomposition = new CholeskyDecomposition(sigma);

        return choleskyDecomposition.getL().transpose();
    }

    public static boolean all(double[][] array, String operator, double tolerance) {
        boolean retVal = true;
        for (int row = 0; row < array.length; row++) {
            for (int col = 0; col < array[row].length; col++) {
                if (0 == "<".compareTo(operator)) {
                    retVal = retVal && (Math.abs(array[row][col]) < tolerance);
                }
                if (0 == ">".compareTo(operator)) {
                    retVal = retVal && (Math.abs(array[row][col]) > tolerance);
                }
                if (!retVal) break;
            }
        }

        return retVal;
    }
}