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

package org.cirdles.tripoli.reports;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public class ReportCategory implements Serializable, Comparable<ReportCategory> {

    private String categoryName;
    private int positionIndex;
    private ReportDetails columnDetails;
    private boolean visible;

    private final String FIXED_CATEGORY_NAME = "Analysis";

    public ReportCategory() {
    }

    public ReportCategory(String categoryName, ReportDetails columnDetails, boolean visible) {
        this.categoryName = categoryName;
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
    public String getCategoryName() {return categoryName;}

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportCategory that = (ReportCategory) o;
        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(categoryName, that.categoryName) && Objects.equals(columnDetails, that.columnDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, positionIndex, columnDetails, visible, FIXED_CATEGORY_NAME);
    }

    @Override
    public int compareTo(@NotNull ReportCategory category) {
        // Holds the fixed category name in its assigned index
        if (this.categoryName.equals(FIXED_CATEGORY_NAME)){
            return -1;
        } else if (category.getCategoryName().equals(FIXED_CATEGORY_NAME)){
            return 1;
        }
        return Integer.compare(this.positionIndex, category.getPositionIndex());
    }
}
