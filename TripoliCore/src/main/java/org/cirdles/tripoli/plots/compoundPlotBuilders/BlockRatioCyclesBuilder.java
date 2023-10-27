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

package org.cirdles.tripoli.plots.compoundPlotBuilders;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.cirdles.tripoli.plots.PlotBuilder;

/**
 * @author James F. Bowring
 */
public class BlockRatioCyclesBuilder extends PlotBuilder {

    //    @Serial
//    private static final long serialVersionUID = 9180059676626735662L;
    protected BlockRatioCyclesRecord blockRatioCyclesRecord;

    protected BlockRatioCyclesBuilder(int blockID, boolean processed, double dalyFaradayGain, String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        super(title, xAxisLabel, yAxisLabel, displayed);
        blockRatioCyclesRecord = generateBlockCyclesPlot(blockID, processed, dalyFaradayGain, new double[0], new double[0], new boolean[0], new String[]{""}, xAxisLabel, true);
        this.displayed = displayed;
    }

    public static BlockRatioCyclesBuilder initializeBlockCycles(
            int blockID, boolean processed, double dalyFaradayGain, double[] cycleLogRatioMeansData, double[] cycleLogRatioOneSigmaData, boolean[] cyclesIncluded,
            String[] title, String xAxisLabel, String yAxisLabel, boolean displayed, boolean blockIncluded) {
        BlockRatioCyclesBuilder blockRatioCyclesBuilder = new BlockRatioCyclesBuilder(blockID, processed, dalyFaradayGain, title, xAxisLabel, yAxisLabel, displayed);
        blockRatioCyclesBuilder.blockRatioCyclesRecord = blockRatioCyclesBuilder.generateBlockCyclesPlot(blockID, processed, dalyFaradayGain, cycleLogRatioMeansData, cycleLogRatioOneSigmaData, cyclesIncluded, title, xAxisLabel, blockIncluded);
        return blockRatioCyclesBuilder;
    }

    protected BlockRatioCyclesRecord generateBlockCyclesPlot(int blockID, boolean processed, double dalyFaradayGain, double[] cycleLogRatioMeansdata, double[] cycleLogRatioOneSigmaData, boolean[] cyclesIncluded, String[] title, String xAxisLabel, boolean blockIncluded) {
        DescriptiveStatistics descriptiveStatisticsRatios = new DescriptiveStatistics();
        for (int index = 0; index < cycleLogRatioMeansdata.length; index++) {
            if (cyclesIncluded[index]) {
                descriptiveStatisticsRatios.addValue(cycleLogRatioMeansdata[index]);
            }
        }

        return new BlockRatioCyclesRecord(
                blockID,
                processed,
                dalyFaradayGain,
                blockIncluded,
                cyclesIncluded,
                cycleLogRatioMeansdata,
                cycleLogRatioOneSigmaData,
                descriptiveStatisticsRatios.getMean(),
                descriptiveStatisticsRatios.getStandardDeviation(),
                title,
                xAxisLabel,
                yAxisLabel
        );
    }

    public BlockRatioCyclesRecord getBlockCyclesRecord() {
        return blockRatioCyclesRecord;
    }
}