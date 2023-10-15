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

package org.cirdles.tripoli.plots.histograms;

import org.cirdles.tripoli.species.IsotopicRatio;

/**
 * @author James F. Bowring
 */
public class RatioHistogramBuilder extends HistogramBuilder {
    private static final long serialVersionUID = 2597311797768082265L;

    private IsotopicRatio ratio;
    private HistogramRecord invertedRatioHistogramRecord;
    private HistogramRecord logRatioHistogramRecord;
    private HistogramRecord logInvertedRatioHistogramRecord;


    private RatioHistogramBuilder(int blockID, String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        super(blockID, title, xAxisLabel, yAxisLabel, displayed);
        ratio = null;
        histogramRecord = null;
        invertedRatioHistogramRecord = null;
        logRatioHistogramRecord = null;
        logInvertedRatioHistogramRecord = null;
        this.displayed = displayed;
    }

    public static RatioHistogramBuilder initializeRatioHistogram(int blockID, IsotopicRatio ratio, IsotopicRatio invertedRatio, int binCount) {
        RatioHistogramBuilder ratioHistogramBuilder = new RatioHistogramBuilder(blockID, new String[]{ratio.prettyPrint()}, "Ratios", "Frequency", ratio.isDisplayed());
        ratioHistogramBuilder.ratio = ratio;
        ratioHistogramBuilder.histogramRecord = ratioHistogramBuilder.generateHistogram(blockID, ratio.getRatioValuesForBlockEnsembles(), binCount, new String[]{ratio.prettyPrint()}, "Ratio");
        ratioHistogramBuilder.invertedRatioHistogramRecord = ratioHistogramBuilder.generateHistogram(blockID, invertedRatio.getRatioValuesForBlockEnsembles(), binCount, new String[]{invertedRatio.prettyPrint()}, "Ratio");
        ratioHistogramBuilder.logRatioHistogramRecord = ratioHistogramBuilder.generateHistogram(blockID, ratio.getLogRatioValuesForBlockEnsembles(), binCount, new String[]{"Log " + ratio.prettyPrint()}, "LogRatio");
        ratioHistogramBuilder.logInvertedRatioHistogramRecord = ratioHistogramBuilder.generateHistogram(blockID, invertedRatio.getLogRatioValuesForBlockEnsembles(), binCount, new String[]{"Log " + invertedRatio.prettyPrint()}, "LogRatio");
        return ratioHistogramBuilder;
    }

    public HistogramRecord getInvertedRatioHistogramRecord() {
        return invertedRatioHistogramRecord;
    }

    public HistogramRecord getLogRatioHistogramRecord() {
        return logRatioHistogramRecord;
    }

    public HistogramRecord getLogInvertedRatioHistogramRecord() {
        return logInvertedRatioHistogramRecord;
    }

    public IsotopicRatio getRatio() {
        return ratio;
    }
}