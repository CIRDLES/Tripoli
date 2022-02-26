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

package org.cirdles.tripoli.matrices.matrixModels;

import jama.Matrix;
// import org.earthtime.dataDictionaries.MeasuredRatios;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author James F. Bowring
 */
public class CovarianceMatrixModel extends AbstractMatrixModel {

    /**
     *
     */
    public CovarianceMatrixModel() {
        super("Covariances");
    }

    @Override
    public AbstractMatrixModel copy() {
        AbstractMatrixModel retval = new CovarianceMatrixModel();

        retval.setRows(rows);
        retval.copyCols(cols);
        retval.setMatrix(matrix.copy());

        return retval;
    }

    /**
     * @param variances
     * @return
     */
    public boolean initializeMatrixModelWithVariances(
            Map<String, BigDecimal> variances) {

        boolean retVal = !(getRows().isEmpty());
        if (retVal) {
            initializeMatrix();

            // walk the rows of covariance matrix and for each row get the name
            // if that name is in the variances (for example, inputVariances)
            // then populate the diagonal at that row with the value
            Iterator<Integer> rowKeys = getRows().keySet().iterator();
            while (rowKeys.hasNext()) {
                int rowKey = rowKeys.next();
                String rowName = getRows().get(rowKey);

                BigDecimal varianceModel = variances.get(rowName);
                if (varianceModel != null) {
                    matrix.set(rowKey, rowKey, varianceModel.doubleValue());
                }
            }
        }

        return retVal;
    }

    /**
     * @param coVariances
     * @return
     */
    public boolean initializeCoVariances(
            Map<String, BigDecimal> coVariances) {

        boolean retVal = !(getRows().isEmpty() || getCols().isEmpty());
        if (retVal) {
            Iterator<String> covKeys = coVariances.keySet().iterator();
            while (covKeys.hasNext()) {
                String covName = covKeys.next();
                BigDecimal covariance = coVariances.get(covName);
                setCovarianceCells(covName, covariance.doubleValue());
            }
        }
        return retVal;
    }

    /**
     * @param covarianceName
     * @param coVariance
     */
    protected void setCovarianceCells(String covarianceName, double coVariance) {

        // name is of form covXXX__YYY
        String both = covarianceName.substring(3);
        String[] each = both.split("__");
        // left side needs lowercase
        String leftSide = each[0].substring(0, 1).toLowerCase() + each[0].substring(1);

        setCovarianceCell(leftSide, each[1], coVariance);

    }

    /**
     * @param leftSide
     * @param rightSide
     * @param coVariance
     * @return
     */
    protected boolean setCovarianceCell(String leftSide, String rightSide, double coVariance) {
        boolean retval = false;

        Integer left = getCols().get(leftSide);
        Integer right = getCols().get(rightSide);
        if ((left != null) && (right != null)) {
            matrix.set(left, right, coVariance);
            matrix.set(right, left, coVariance);
            retval = true;
        }
        return retval;
    }

    /**
     * Recalculates covariances due to moving a slider Returns Matrix with
     * diagonal = oneSigmaAbs, for use when lockingVariances
     *
     * @param covaryingTerms
     * @param oneSigma
     * @param lockVariancesTogether
     * @return
     */
    public Matrix recalculateSubCovariances(
            String[] covaryingTerms, BigDecimal oneSigma, boolean lockVariancesTogether) {
        // array is at least length 1 for the uncertainty changed by slider
        // walk the array and extract mini matrices from the covariance matrix
        Matrix dOldInvert = new Matrix(covaryingTerms.length, covaryingTerms.length);
        Matrix dNew = new Matrix(covaryingTerms.length, covaryingTerms.length);
        Matrix sOldCov = new Matrix(covaryingTerms.length, covaryingTerms.length);

        double sigmaConverter = 1.0;
        // depends on covaryingTerms having the subject as first element
        for (int i = 0; i < covaryingTerms.length; i++) {
            int colA = getCols().get(covaryingTerms[i]);

            // set dOldInvert diagonal to inverse of one-sigmas
            if (matrix.get(colA, colA) == 0.0) {
                dOldInvert.set(i, i, 0.0);
            } else {
                dOldInvert.set(i, i, 1.0 / Math.sqrt(matrix.get(colA, colA)));
            }

            // set dNew diagonal to one-sigmas
            if (i == 0) {
                sigmaConverter = oneSigma.doubleValue() / Math.sqrt(matrix.get(colA, colA));
            }
            if (lockVariancesTogether) {
                dNew.set(i, i, sigmaConverter * Math.sqrt(matrix.get(colA, colA)));
            } else {
                if (i == 0) {
                    dNew.set(0, 0, oneSigma.doubleValue());
                } else {
                    dNew.set(i, i, Math.sqrt(matrix.get(colA, colA)));
                }
            }

            // extract a mini-covariance matrix of affected terms
            sOldCov.set(i, i, matrix.get(colA, colA));
            for (int j = i + 1; j < covaryingTerms.length; j++) {
                int colB = getCols().get(covaryingTerms[j]);
                sOldCov.set(i, j, matrix.get(colA, colB));
                sOldCov.set(j, i, matrix.get(colB, colA));
            }
        }

        // calculate new mini covariance matrix
        Matrix R =
                dOldInvert.times(
                        sOldCov).times(
                        dOldInvert);

        Matrix sNewCov =//
                dNew.times(R).times(dNew);

        // push new values back into all covariance matrices
        for (int i = 0; i < covaryingTerms.length; i++) {
            int colA = getCols().get(covaryingTerms[i]);
            for (int j = 0; j < covaryingTerms.length; j++) {
                int colB = getCols().get(covaryingTerms[j]);
                matrix.set(colA, colB, sNewCov.get(i, j));
            }
        }

        return dNew;

    }

    /**
     * This method evauates the correlations between pairs of measured ratio
     * uncertainties. If any are out of the range [-1,1], a report is returned.
     *
     * @param fractionID
     * @return
     */
    public String checkValidityOfMeasuredRatioUncertainties(String fractionID) {
        String retval = "";
        // first, we detect which measured ratios are present
        // TODO: generaliz the source of measured ratios on the fly
        String[] measuredNames = new String[0];//MeasuredRatios.getNames();
        ArrayList<String> measuredNamesFound = new ArrayList<String>();
        for (int i = 0; i < measuredNames.length; i++) {
            if (getCols().containsKey(measuredNames[i])) {
                measuredNamesFound.add(measuredNames[i]);
            }
        }

        Matrix dInvert = new Matrix(measuredNamesFound.size(), measuredNamesFound.size());
        Matrix sCov = new Matrix(measuredNamesFound.size(), measuredNamesFound.size());

        for (int i = 0; i < measuredNamesFound.size(); i++) {
            int colA = getCols().get(measuredNamesFound.get(i));

            // set dInvert diagonal to inverse of one-sigmas (0 for div by 0)
            try {
                dInvert.set(i, i, 1.0 / Math.sqrt(matrix.get(colA, colA)));
            } catch (Exception e) {
                dInvert.set(i, i, 0.0);
            }

            // extract a mini-covariance matrix of affected terms
            sCov.set(i, i, matrix.get(colA, colA));
            for (int j = i + 1; j < measuredNamesFound.size(); j++) {
                int colB = getCols().get(measuredNamesFound.get(j));
                sCov.set(i, j, matrix.get(colA, colB));
                sCov.set(j, i, matrix.get(colB, colA));
            }
        }

        Matrix rhos = dInvert.times(sCov).times(dInvert);
        // poll every cell of rhos to make sure it is in range [-1,1] or report it
        NumberFormat formatter = new DecimalFormat("###0.000");

        for (int i = 0; i < rhos.getRowDimension() - 1; i++) {
            for (int j = i + 1; j < rhos.getColumnDimension(); j++) {
                if (Math.abs(rhos.get(i, j)) > 1.0) {
                    // bad rhos
                    if (retval.length() == 0) {
                        retval += "For fraction " + fractionID + ", the out-of-range calculated correlation coefficient between inputs<br> ";
                    }
                    retval += "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + measuredNamesFound.get(i)
                            + " and "
                            + measuredNamesFound.get(j)
                            + " is " + formatter.format(rhos.get(i, j))
                            + "<br>";
                }
            }
        }

        if (retval.length() > 0) {
            retval += "<br>";
        }

        return retval;
    }

    /**
     * @param leftSide
     * @param rightSide
     * @return
     */
    public double getCovarianceCell(String leftSide, String rightSide) {
        double retval = 0.0;

        Integer left = getCols().get(leftSide);
        Integer right = getCols().get(rightSide);

        if ((left != null) && (right != null)) {
            return matrix.get(left, right);
        }
        return retval;
    }

    /**
     * @param leftSide
     * @param rightSide
     * @param value
     */
    public void setCovarianceCells(String leftSide, String rightSide, double value) {
        Integer left = getCols().get(leftSide);
        Integer right = getCols().get(rightSide);

        if ((left != null) && (right != null)) {
            matrix.set(left, right, value);
            matrix.set(right, left, value);
        }
    }

    /**
     * @param row
     * @param col
     * @param value
     */
    @Override
    public void setValueAt(int row, int col, double value) {
        matrix.set(row, col, value);
        if (col != row) {
            matrix.set(col, row, value);
        }
    }
}