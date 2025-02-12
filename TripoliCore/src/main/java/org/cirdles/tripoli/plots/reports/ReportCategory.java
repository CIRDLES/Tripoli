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

package org.cirdles.tripoli.plots.reports;

import java.io.Serializable;

public class ReportCategory implements Serializable {

    private String displayName;
    private int positionIndex;
    private ReportDetails columnDetails;
    private boolean visible;

    public ReportCategory() {
    }

    public ReportCategory(String displayName, ReportDetails columnDetails, boolean visible) {
        this.displayName = displayName;
        this.positionIndex = 0;
        this.columnDetails = columnDetails;
        this.visible = visible;
    }
    public int getPositionIndex() {
        return positionIndex;
    }
    public void setPositionIndex(int positionIndex) {
        this.positionIndex = positionIndex;
    }
    public void swapColumnPositions( ReportCategory coulmn1, ReportCategory coulmn2 ) {
        int temp = coulmn1.getPositionIndex();
        coulmn1.setPositionIndex(coulmn2.getPositionIndex());
        coulmn2.setPositionIndex(temp);
    }

}
