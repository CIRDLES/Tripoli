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

package org.cirdles.tripoli.utilities.mathUtilities.weightedMeans;


import jama.Matrix;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author James F. Bowring
 */
public class WeighteMeanOfLogRatio {


    /**
     * see https://github.com/CIRDLES/Tripoli/issues/185
     *
     * @param logRatioMeans
     * @param logRatioCoVariances
     */
    public static WeightedMeanRecord calculateWeightedMean(double[] logRatioMeans, double[][] logRatioCoVariances) {

        RealMatrix logRatioVariancesRealMatrix = new BlockRealMatrix(logRatioCoVariances);
        DecompositionSolver solver = new QRDecomposition(logRatioVariancesRealMatrix).getSolver();
        RealMatrix inverse = solver.getInverse();
        double[][] invertedLogRatioVariances = inverse.getData();

        int countOfElements = logRatioMeans.length;
        Matrix logRatioMeansMatrix = new Matrix(logRatioMeans, countOfElements);
        double[] ones = new double[countOfElements];
        Arrays.fill(ones, 1);
        Matrix onesMatrix = new Matrix(ones, countOfElements);
        Matrix invertedLogRatioVariancesMatrix = new Matrix(invertedLogRatioVariances);

        double logRatioWeightedMean =
                onesMatrix.transpose().times(invertedLogRatioVariancesMatrix).times(logRatioMeansMatrix).get(0, 0)
                        / onesMatrix.transpose().times(invertedLogRatioVariancesMatrix).times(onesMatrix).get(0, 0);

        double logRatioOneSigmaAbs = Math.sqrt(1.0 / onesMatrix.transpose().times(invertedLogRatioVariancesMatrix).times(onesMatrix).get(0, 0));

        Matrix residualsMatrix = (Matrix) logRatioMeansMatrix.clone();
        for (int i = 0; i < countOfElements; i++) {
            residualsMatrix.set(i, 0, residualsMatrix.get(i, 0) - logRatioWeightedMean);
        }

        double logRatioReducedChiSquareStatistic = residualsMatrix.transpose().times(invertedLogRatioVariancesMatrix).times(residualsMatrix).get(0, 0) / (countOfElements - 1);

        /*
        That means you'll be using a calculated mean and 1-sigma uncertainty of a log-ratio.  To calculate the ratio, just exponentiate.
        To get the uncertainty, you'll want to calculate the log-mean minus one sigma and the log-mean plus one sigma.
         Exponentiate both of those limits, then use them to calculate a -1sigma and +1sigma uncertainty for the ratio mean.
         If the -1sigma and +1sigma are within some tolerance (we used two significant figures), then report them together as ± X.
         If they're different outside that tolerance, then report them separately as +Y/-Z.
         */

        double ratioWeightedMean = StrictMath.exp(logRatioWeightedMean);
        double ratioHigherOneSigmaAbs = StrictMath.exp(logRatioWeightedMean + logRatioOneSigmaAbs) - ratioWeightedMean;
        double ratioLowerOneSigmaAbs = ratioWeightedMean - StrictMath.exp(logRatioWeightedMean - logRatioOneSigmaAbs);

        return new WeightedMeanRecord(
                logRatioWeightedMean,
                logRatioOneSigmaAbs,
                logRatioReducedChiSquareStatistic,
                ratioWeightedMean,
                ratioHigherOneSigmaAbs,
                ratioLowerOneSigmaAbs
        );
    }

    public record WeightedMeanRecord(
            double logRatioWeightedMean,
            double logRatioOneSigmaAbs,
            double logRatioReducedChiSquareStatistic,
            double ratioWeightedMean,
            double ratioHigherOneSigmaAbs,
            double ratioLowerOneSigmaAbs
    ) implements Serializable {
    }

}