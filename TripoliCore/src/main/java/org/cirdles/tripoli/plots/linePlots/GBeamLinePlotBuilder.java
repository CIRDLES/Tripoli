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

package org.cirdles.tripoli.plots.linePlots;

/**
 * @author James F. Bowring
 */
public class GBeamLinePlotBuilder extends LinePlotBuilder {
    private final double[] intensityData;

    private GBeamLinePlotBuilder(double[] xData, double[] yData, double[] intensityData) {
        super(xData, yData, new String[]{"Line Plot of G-Beam"}, "Mass (amu)", "Peak Intensity", 0, 0);
        this.intensityData = intensityData;
    }

    public static GBeamLinePlotBuilder initializeGBeamLinePlot(double[] xData, double[] yData, double[] intensityData) {
        return new GBeamLinePlotBuilder(xData, yData, intensityData);
    }

    public double[] getIntensityData() {
        return intensityData;
    }
}