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

package org.cirdles.tripoli.plots.analysisPlotBuilders;

import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.plots.histograms.HistogramRecord;
import org.cirdles.tripoli.utilities.mathUtilities.weightedMeans.WeighteMeanOfLogRatio;

import java.io.Serializable;
import java.util.Map;

import static java.lang.StrictMath.exp;

public record AnalysisRatioRecord(
        IsotopicRatio ratio, int blockCount,
        Map<Integer, HistogramRecord> mapBlockIdToHistogramRecord,
        double[] blockIds,
        double[] blockLogRatioMeans,
        double[] blockLogRatioOneSigmas,
        double analysisLogRatioMean,
        double analysisLogRatioOneSigma,
        WeighteMeanOfLogRatio.WeightedMeanRecord weightedMeanRecord,
        String[] title,
        String xAxisLabel,
        String yAxisLabel
) implements Serializable {

    public double[] blockRatioMeans() {
        double[] blockRatioMeans = new double[blockLogRatioMeans.length];
        for (int i = 0; i < blockLogRatioMeans.length; i++) {
            blockRatioMeans[i] = exp(blockLogRatioMeans[i]);
        }

        return blockRatioMeans;
    }

    public double[][] blockRatioOneSigmas() {
        double[][] blockRatioOneSigmas = new double[2][blockLogRatioMeans.length];
        for (int i = 0; i < blockLogRatioOneSigmas.length; i++) {
            double ratioMean = exp(blockLogRatioMeans[i]);
            double ratioHigherOneSigmaAbs = exp(blockLogRatioMeans[i] + blockLogRatioOneSigmas[i]) - ratioMean;
            double ratioLowerOneSigmaAbs = ratioMean - exp(blockLogRatioMeans[i] - blockLogRatioOneSigmas[i]);
            //TODO: make this two-sided valueModel
            blockRatioOneSigmas[0][i] = ratioHigherOneSigmaAbs;
            blockRatioOneSigmas[1][i] = ratioLowerOneSigmaAbs;
        }

        return blockRatioOneSigmas;
    }
}