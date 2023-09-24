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
public class BeamShapeLinePlotBuilder extends LinePlotBuilder {
    private final int leftBoundary;
    private final int rightBoundary;

    private BeamShapeLinePlotBuilder(double[] xData, double[] yData, int leftBoundary, int rightBoundary) {
        super(xData, yData, new String[]{"Line Plot of Beam Shape"}, "Mass (amu)", "Beam Intensity", 0, 0);
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
    }

    public static BeamShapeLinePlotBuilder initializeBeamShapeLinePlot(double[] xData, double[] yData, int leftBoundary, int rightBoundary) {
        return new BeamShapeLinePlotBuilder(xData, yData, leftBoundary, rightBoundary);
    }

    public int getLeftBoundary() {
        return leftBoundary;
    }

    public int getRightBoundary() {
        return rightBoundary;
    }
}