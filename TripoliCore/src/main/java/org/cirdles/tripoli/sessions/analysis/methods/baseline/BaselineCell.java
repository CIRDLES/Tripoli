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

package org.cirdles.tripoli.sessions.analysis.methods.baseline;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author James F. Bowring
 * <p>
 * BaselineCell holds a mass only.
 */
public class BaselineCell implements Serializable {

    @Serial
    private static final long serialVersionUID = 6371757007810524402L;

    //    private void readObject ( ObjectInputStream stream ) throws IOException,
//            ClassNotFoundException {
//        stream.defaultReadObject();
//
//        ObjectStreamClass myObject = ObjectStreamClass.lookup(
//                Class.forName( BaselineCell.class.getCanonicalName()) );
//        long theSUID = myObject.getSerialVersionUID();
//
//        System.err.println( "Customized De-serialization of BaselineCell "
//                + theSUID );
//    }
    private String baselineID;
    private int baselineSequence;
    private double cellMass;

    public BaselineCell(String baselineID, int baselineSequence) {
        this.baselineID = baselineID;
        this.baselineSequence = baselineSequence;
        cellMass = 0.0;
    }

    public static BaselineCell initializeBaselineCell(String baselineID, int baselineSequence) {
        return new BaselineCell(baselineID, baselineSequence);
    }

    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof BaselineCell otherBaselineCell) {
                retVal = 0 == this.getBaselineID().compareToIgnoreCase(otherBaselineCell.getBaselineID());
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == baselineID ? 0 : baselineID.hashCode());
        return hash;
    }

    public String getBaselineID() {
        return baselineID;
    }

    public void setBaselineID(String baselineID) {
        this.baselineID = baselineID;
    }

    public int getBaselineSequence() {
        return baselineSequence;
    }

    public void setBaselineSequence(int baselineSequence) {
        this.baselineSequence = baselineSequence;
    }

    public double getCellMass() {
        return cellMass;
    }

    public void setCellMass(double cellMass) {
        this.cellMass = cellMass;
    }
}