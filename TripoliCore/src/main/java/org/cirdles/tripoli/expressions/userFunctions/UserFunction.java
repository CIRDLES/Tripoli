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

package org.cirdles.tripoli.expressions.userFunctions;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author James F. Bowring
 */
public class UserFunction implements Serializable {
    @Serial
    private static final long serialVersionUID = -5408855769497340457L;
    private String name;
    private int columnIndex;
    private boolean treatAsIsotopicRatio;
    private boolean displayed;
    private boolean inverted;

    public UserFunction(String name, int columnIndex, boolean treatAsIsotopicRatio, boolean displayed) {
        this.name = name;
        this.columnIndex = columnIndex;
        this.treatAsIsotopicRatio = treatAsIsotopicRatio;
        this.displayed = displayed;
        this.inverted = false;
    }

    public String getName() {
        return name;
    }

    public String showInvertedRatioName() {
        String retVal = name;
        if (treatAsIsotopicRatio) {
            String[] nameSplit = name.split("/");
            retVal = nameSplit[1] + "/" + nameSplit[0];
        }
        return retVal;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public boolean isTreatAsIsotopicRatio() {
        return treatAsIsotopicRatio;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}