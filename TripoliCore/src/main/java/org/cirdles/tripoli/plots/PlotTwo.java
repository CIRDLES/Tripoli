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

package org.cirdles.tripoli.plots;

import java.io.Serializable;

/**
 * Stores plot2 selection configuration (xAxis, yAxis, intensity UserFunctions)
 * for persistence in the analysis method.
 */
public class PlotTwo implements Serializable {
    private static final long serialVersionUID = -2288545158821633069L;

    private String xAxisUserFunctionName;
    private String yAxisUserFunctionName;
    private String intensityUserFunctionName;
    private String name;
    private boolean displayed;

    public PlotTwo() {
        this.displayed = true;
    }

    public PlotTwo(String xAxisUserFunctionName, String yAxisUserFunctionName, String intensityUserFunctionName, String name) {
        this.xAxisUserFunctionName = xAxisUserFunctionName;
        this.yAxisUserFunctionName = yAxisUserFunctionName;
        this.intensityUserFunctionName = intensityUserFunctionName;
        this.name = name;
        this.displayed = true;
    }

    public String getXAxisUserFunctionName() {
        return xAxisUserFunctionName;
    }

    public void setXAxisUserFunctionName(String xAxisUserFunctionName) {
        this.xAxisUserFunctionName = xAxisUserFunctionName;
    }

    public String getYAxisUserFunctionName() {
        return yAxisUserFunctionName;
    }

    public void setYAxisUserFunctionName(String yAxisUserFunctionName) {
        this.yAxisUserFunctionName = yAxisUserFunctionName;
    }

    public String getIntensityUserFunctionName() {
        return intensityUserFunctionName;
    }

    public void setIntensityUserFunctionName(String intensityUserFunctionName) {
        this.intensityUserFunctionName = intensityUserFunctionName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("X: ").append(xAxisUserFunctionName != null ? xAxisUserFunctionName : "None");
        sb.append(" vs Y: ").append(yAxisUserFunctionName != null ? yAxisUserFunctionName : "None");
        if (intensityUserFunctionName != null && !intensityUserFunctionName.isEmpty()) {
            sb.append(" (Intensity: ").append(intensityUserFunctionName).append(")");
        }
        return sb.toString();
    }
}

