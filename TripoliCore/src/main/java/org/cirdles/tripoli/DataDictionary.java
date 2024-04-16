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
    public static String[] EarthTimeBariumPhosphateICIsotopeNames = new String[]{
            "pct130Ba_BaPO2",
            "pct132Ba_BaPO2",
            "pct134Ba_BaPO2",
            "pct135Ba_BaPO2",
            "pct136Ba_BaPO2",
            "pct137Ba_BaPO2",
            "pct138Ba_BaPO2",
            "pct16O_BaPO2",
            "pct17O_BaPO2",
            "pct18O_BaPO2"
    };

    public static String[] EarthTimeThalliumICIsotopeNames = new String[]{
            "pct203Tl_Tl",
            "pct205Tl_Tl"
    };

    public static String[] EarthTimeTracerRatioNames = new String[]{
            "206_204",
            "207_206",
            "206_208",
            "206_205",
            "207_205",
            "208_205",
            "202_205",
            "238_235",
            "233_235",
            "233_236",
            "235_205"};

    public static String[] UPbReduxMeasuredRatioNames = new String[]{
            "206_204",
            "207_204",
            "208_204",
            "206_207",
            "206_208",
            "204_205",
            "206_205",
            "207_205",
            "208_205",
            "202_205",
            "238_236",     //jan 2011
            "233_236",
            "238_235",
            "233_235",
            "238_233"};
    public static String[] isotopeNames = new String[]
            {"Pb205",
                    "U235"};
    public static String[] TracerTypes = new String[]
            {"mixed 205-235",
                    "mixed 205-233-235",
                    "mixed 208-235",
                    "mixed 205-233-236",
                    "mixed 202-205-233-235",
                    "mixed 202-205-233-236",
                    "mixed 205-233-235-230Th"};

    public static String getEarthTimeBariumPhosphateICIsotopeNames(int index) {
        return EarthTimeBariumPhosphateICIsotopeNames[index];
    }

    public static String getEarthTimeThalliumICIsotopeNames(int index) {
        return EarthTimeThalliumICIsotopeNames[index];
    }

    public static String getTracerRatioName(int index) {
        return "r" + EarthTimeTracerRatioNames[index] + "t";
    }

    public static String getTracerIsotopeConcName(int index) {
        return "conc" + isotopeNames[index] + "t";
    }

    public static String getMeasuredRatioName(int index) {
        return "r" + UPbReduxMeasuredRatioNames[index] + "m";
    }

    public static String getElementNameOfRatio(String name) {
        if (name.substring(1, 1).equals("0"))
            return "Pb";
        else
            return "U";
    }

}

