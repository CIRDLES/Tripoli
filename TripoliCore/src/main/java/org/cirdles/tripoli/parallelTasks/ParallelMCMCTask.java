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

package org.cirdles.tripoli.parallelTasks;

import org.cirdles.tripoli.plots.PlotBuilder;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

/**
 * @author James F. Bowring
 */

public class ParallelMCMCTask implements Runnable {

    public static AnalysisInterface analysis;
    private final int blockID;
    //  plotBuilders
    private PlotBuilder[] ratiosHistogramBuilder;

    private PlotBuilder[] convergeRatioLineBuilder;

    private PlotBuilder[] convergeBLFaradayLineBuilder;

    private PlotBuilder[] convergeErrWeightedMisfitLineBuilder;
    private PlotBuilder[] convergeErrRawMisfitLineBuilder;

    private PlotBuilder[] convergeIntensityLinesBuilder;

    private PlotBuilder[] peakShapesBuilder;

    // TODO: refactor to all plotBuilders
    private PlotBuilder[][] plotBuilders;

    public ParallelMCMCTask(int blockID) {
        this.blockID = blockID;
    }

    public PlotBuilder[][] getPlotBuilders() {
        return plotBuilders;
    }

    public boolean healthyPlotbuilder() {
        return (ratiosHistogramBuilder != null);
    }

    public PlotBuilder[] getPeakShapesBuilder() {
        return peakShapesBuilder.clone();
    }

    public PlotBuilder[] getConvergeRatioLineBuilder() {
        return convergeRatioLineBuilder.clone();
    }

    public PlotBuilder[] getConvergeBLFaradayLineBuilder() {
        return convergeBLFaradayLineBuilder.clone();
    }

    public PlotBuilder[] getConvergeErrWeightedMisfitLineBuilder() {
        return convergeErrWeightedMisfitLineBuilder.clone();
    }

    public PlotBuilder[] getConvergeErrRawMisfitLineBuilder() {
        return convergeErrRawMisfitLineBuilder.clone();
    }

    public PlotBuilder[] getConvergeIntensityLinesBuilder() {
        return convergeIntensityLinesBuilder;
    }

    public  void run() {
                System.out.println(Thread.currentThread().getName()
                + " is executing this code");
        try {
            plotBuilders = analysis.updatePlotsByBlock2(blockID);
        } catch (TripoliException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBlockID() {
        return blockID;
    }

}