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

    private HistogramRecord invertedRatioHistogramRecord;


    private RatioHistogramBuilder(int blockID, String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        super(blockID, title, xAxisLabel, yAxisLabel, displayed);
        histogramRecord = generateHistogram(blockID, new double[0], 0, new String[]{""});
        invertedRatioHistogramRecord = generateHistogram(blockID, new double[0], 0, new String[]{""});
        this.displayed = displayed;
    }

    public static RatioHistogramBuilder initializeRatioHistogram(int blockID, IsotopicRatio ratio, IsotopicRatio invertedRatio, int binCount) {
        RatioHistogramBuilder ratioHistogramBuilder = new RatioHistogramBuilder(blockID, new String[]{ratio.prettyPrint()}, "Ratios", "Frequency", ratio.isDisplayed());
        ratioHistogramBuilder.histogramRecord = ratioHistogramBuilder.generateHistogram(blockID, ratio.getRatioValues(), binCount, new String[]{ratio.prettyPrint()});
        ratioHistogramBuilder.invertedRatioHistogramRecord = ratioHistogramBuilder.generateHistogram(blockID, invertedRatio.getRatioValues(), binCount, new String[]{invertedRatio.prettyPrint()});
        return ratioHistogramBuilder;
    }

    public HistogramRecord getInvertedRatioHistogramRecord() {
        return invertedRatioHistogramRecord;
    }
}