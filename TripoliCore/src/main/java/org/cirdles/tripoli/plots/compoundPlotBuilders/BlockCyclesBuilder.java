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

import org.cirdles.tripoli.plots.PlotBuilder;

/**
 * @author James F. Bowring
 */
public class BlockCyclesBuilder extends PlotBuilder {

    //    @Serial
//    private static final long serialVersionUID = 9180059676626735662L;
    protected BlockCyclesRecord blockCyclesRecord;

    protected BlockCyclesBuilder(int blockID, boolean processed, String[] title, boolean displayed) {
        super(title, "", "", displayed);
        blockCyclesRecord = generateBlockCyclesPlot(blockID, processed, new double[0], new double[0], new boolean[0], new String[]{""}, true);
        this.displayed = displayed;
    }

    public static BlockCyclesBuilder initializeBlockCycles(
            int blockID, boolean blockIncluded, boolean processed, boolean[] cyclesIncluded, double[] cycleMeansData, double[] cycleOneSigmaData,
            String[] title, boolean displayed, boolean isRatio) {
        BlockCyclesBuilder blockCyclesBuilder = new BlockCyclesBuilder(blockID, processed, title, displayed);
        blockCyclesBuilder.blockCyclesRecord = blockCyclesBuilder.generateBlockCyclesPlot(blockID, processed, cycleMeansData, cycleOneSigmaData, cyclesIncluded, title, blockIncluded);
        return blockCyclesBuilder;
    }

    protected BlockCyclesRecord generateBlockCyclesPlot(
            int blockID, boolean processed, double[] cycleMeansdata, double[] cycleOneSigmaData,
            boolean[] cyclesIncluded, String[] title, boolean blockIncluded) {
//        DescriptiveStatistics descriptiveStatisticsBlockCycles = new DescriptiveStatistics();
//        for (int index = 0; index < cycleMeansdata.length; index++) {
//            if (cyclesIncluded[index]) {
//                descriptiveStatisticsBlockCycles.addValue(cycleMeansdata[index]);
//            }
//        }

        return new BlockCyclesRecord(
                blockID,
                processed,
                blockIncluded,
                cyclesIncluded,
                cycleMeansdata,
                cycleOneSigmaData,
                title
        );
    }

    public BlockCyclesRecord getBlockCyclesRecord() {
        return blockCyclesRecord;
    }
}