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

import java.io.Serial;
import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class PlotBuilder implements Serializable {
    @Serial
    private static final long serialVersionUID = -7383206169661986594L;
    protected String[] title;
    protected String xAxisLabel;
    protected String yAxisLabel;
    protected boolean displayed;

    protected double shadeWidthForModelConvergence;

    public PlotBuilder() {
        this(new String[]{"NONE"}, "NONE", "NONE", false);
    }

    public PlotBuilder(String[] title, String xAxisLabel, String yAxisLabel, boolean displayed) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.displayed = displayed;
    }

    public double getShadeWidthForModelConvergence() {
        return shadeWidthForModelConvergence;
    }

    public void setShadeWidthForModelConvergence(double shadeWidthForModelConvergence) {
        this.shadeWidthForModelConvergence = Math.max(0.0, shadeWidthForModelConvergence);
    }

    public String[] getTitle() {
        return title;
    }

    public String getxAxisLabel() {
        return xAxisLabel;
    }

    public String getyAxisLabel() {
        return yAxisLabel;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }
}