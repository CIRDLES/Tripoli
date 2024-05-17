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

package org.cirdles.tripoli.constants;

import org.cirdles.tripoli.utilities.collections.CircularArray;

import java.io.File;
import java.nio.CharBuffer;

public enum TripoliConstants {
    ;
    public static final File TRIPOLI_RESOURCES_FOLDER = new File("TripoliResources");
    public static final File SCHEMA_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "Schema");

    public static final File PARAMETER_MODELS_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "ParameterModels");
    public static final File REFMAT_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "ReferenceMaterialModels");
    public static final File PHYSCONST_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "PhysicalConstantsModels");
    public static final File TRACER_MODELS_FOLDER = new File(PARAMETER_MODELS_FOLDER.getAbsolutePath() + File.separator + "TracerModels");

    public static final File SYNTHETIC_DATA_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "syntheticData");
    public static final File SYNTHETIC_DATA_FOLDER_2ISOTOPE
            = new File(SYNTHETIC_DATA_FOLDER.getAbsolutePath() + File.separator + "TwoIsotopeSyntheticData");
    public static final File SYNTHETIC_DATA_FOLDER_DATA
            = new File(SYNTHETIC_DATA_FOLDER.getAbsolutePath() + File.separator + "data");
    public static final File SYNTHETIC_DATA_FOLDER_METHODS
            = new File(SYNTHETIC_DATA_FOLDER.getAbsolutePath() + File.separator + "methods");
    public static final File NUCLIDESCHART_DATA_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "NuclidesChartData");
    public static final File PERIODICTABLE_DATA_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "PeriodicTableData");

    public static final File PEAK_CENTRES_FOLDER = new File(TRIPOLI_RESOURCES_FOLDER.getAbsolutePath() + File.separator + "PeakCentres");

    public static final String DEFAULT_OBJECT_NAME = "NO_NAME";

    public static final String TRIPOLI_USERS_DATA_FOLDER_NAME = "TripoliUserData";


    public static final String SPACES_100 = CharBuffer.allocate(100).toString().replace('\0', ' ');
    public static final String SPACES_150 = CharBuffer.allocate(150).toString().replace('\0', ' ');

    public static final String MISSING_STRING_FIELD = "MISSING";

    /**
     * elementary charge e is exactly 1.602176634×10−19 coulomb (C).
     * see: https://en.wikipedia.org/wiki/2019_redefinition_of_the_SI_base_units
     */
    public static final double ELEMENTARY_CHARGE_E = 1.602176634e-19;
    public static final double ONE_COULOMB = 1.0 / ELEMENTARY_CHARGE_E; // s.b. 6.2415091e18 == 6.2415090744607631E18
    public static final int PLOT_INDEX_RATIOS = 0;
    public static final int PLOT_INDEX_BASELINES = 1;
    public static final int PLOT_INDEX_DFGAINS = 2;
    public static final int PLOT_INDEX_MEANINTENSITIES = 4;
    public static final String PLOT_TAB_ENSEMBLES = "Ensembles";
    public static final String PLOT_TAB_CONVERGE = "Converge";
    public static final String PLOT_TAB_CONVERGE_INTENSITY = "Converge Intensity";

    public static final CircularArray<String> TRIPOLI_DEFAULT_HEX_COLORS = new CircularArray<>(new String[]{
            "#12bceb", "#095c73", "#ff0000", "#7fffd4", "#ffcf62", "#ac8c42", "#ff0000",
            "#7fffd4","#9e6fb1","#4d3656","#ff0000","#7fffd4","#baff78","#6e9747","#ff0000",
            "#7fffd4","#ffa056","#b2703c","#ff0000","#7fffd4"});

    public enum DetectorPlotFlavor {

        FARADAY_DATA("Faraday Data"),
        PM_DATA("PM Data"),
        FARADAY_MODEL("Faraday Model"),
        PM_MODEL("PM Model");

        private final String name;

        DetectorPlotFlavor(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

    }

    public enum IntensityUnits {
        COUNTS(),
        VOLTS(),
        AMPS();

        public static double[] convertFromCountsToVolts(double[] counts, double[] amplifierResistance) {
            double[] volts = new double[counts.length];
            for (int i = 0; i < volts.length; i++) {
                volts[i] = counts[i] / (ONE_COULOMB / amplifierResistance[i]);
            }
            return volts;
        }

        public static double convertFromVoltsToCount(double count, double amplifierResistance) {
            return count * (ONE_COULOMB / amplifierResistance);
        }

        public static double[] convertFromCountsToAmps(double[] counts) {
            double[] amps = new double[counts.length];
            for (int i = 0; i < amps.length; i++) {
                amps[i] = counts[i] / ONE_COULOMB;
            }
            return amps;
        }
    }

    public static enum ETReduxExportTypeEnum {
        Pb(),
        U(),
        NONE();
    }

    public static enum ReductionModeEnum {
        BLOCK(),
        CYCLE();
    }
}