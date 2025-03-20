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

public class ReportColumn implements Serializable, Comparable<ReportColumn>{
    private static final long serialVersionUID = 3378567673921898881L;
    public final String FIXED_COLUMN_NAME = "Analysis Name";

    private final String columnName;
    private int positionIndex;
    private boolean visible;

    public ReportColumn(String title, int positionIndex) {
        columnName = title;
        this.positionIndex = positionIndex;
        visible = true;
    }
    public ReportColumn(ReportColumn otherColumn) {
        columnName = otherColumn.columnName;
        positionIndex = otherColumn.positionIndex;
        visible = otherColumn.visible;
    }

    public String getColumnName() { return columnName; }

    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return visible; }

    public void setPositionIndex(int i) {
        positionIndex = i;
    }
    public int getPositionIndex() { return positionIndex; }

    @Override
    public int compareTo(@NotNull ReportColumn column) {
        if (this.columnName.equals(FIXED_COLUMN_NAME)){
            return -1;
        } else if (column.getColumnName().equals(FIXED_COLUMN_NAME)){
            return 1;
        }
        return Integer.compare(this.positionIndex, column.getPositionIndex());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ReportColumn that = (ReportColumn) o;

        return positionIndex == that.positionIndex && visible == that.visible && Objects.equals(columnName, that.columnName);
    }

    @Override
    public int hashCode() {
        return Objects.hash( positionIndex, visible, columnName);
    }
}
