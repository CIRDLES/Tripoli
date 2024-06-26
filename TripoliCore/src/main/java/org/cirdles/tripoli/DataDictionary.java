/****************************************************************************
 * Copyright 2004-2024 James F. Bowring and www.Earth-Time.org
 * Ported from OGTripoli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ****************************************************************************/

package org.cirdles.tripoli;

public enum DataDictionary {
    ;
    public static String[] etReduxLeadMeasuredRatioNames = {
            "206_204",
            "207_204",
            "208_204",
            "206_207",
            "206_208",
            "204_205",
            "206_205",
            "207_205",
            "208_205",
            "202_205"};

    public static String[] etReduxUraniumMeasuredRatioNames = {
            "238_236",
            "233_236",
            "238_235",
            "233_235",
            "238_233"};

    public static boolean isLegalETReduxName(String name) {
        boolean retVal = false;
        if (name.contains("23")) {
            for (int i = 0; i < etReduxUraniumMeasuredRatioNames.length; i++) {
                retVal = retVal || etReduxUraniumMeasuredRatioNames[i].compareTo(name) == 0;
            }
        } else if (name.contains("20")) {
            for (int i = 0; i < etReduxLeadMeasuredRatioNames.length; i++) {
                retVal = retVal || etReduxLeadMeasuredRatioNames[i].compareTo(name) == 0;
            }
        }
        return retVal;
    }

}


