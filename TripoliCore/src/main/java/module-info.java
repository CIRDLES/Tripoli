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

module Tripoli.TripoliCore {
    requires commons.bc38781605;
    requires org.apache.poi.poi;
    requires org.jetbrains.annotations;
    requires jama;
    requires ojalgo;
    requires com.google.common;
    requires commons.math3;
    requires commons.lang3;
    requires jakarta.xml.bind;
    requires java.xml.bind;
    requires jblas;


    exports org.cirdles.tripoli;
    exports org.cirdles.tripoli.valueModels;
    exports org.cirdles.tripoli.utilities.stateUtilities;
    exports org.cirdles.tripoli.utilities.exceptions;
    exports org.cirdles.tripoli.expressions.species.nuclides;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;
    exports org.cirdles.tripoli.sessions.analysis.methods;
    exports org.cirdles.tripoli.sessions.analysis.methods.baseline;
    exports org.cirdles.tripoli.sessions.analysis.methods.sequence;
    exports org.cirdles.tripoli.expressions.species;
    exports org.cirdles.tripoli.elements;
    exports org.cirdles.tripoli.utilities.callbacks;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes;
    exports org.cirdles.tripoli.plots.histograms;
    exports org.cirdles.tripoli.plots.linePlots;
    exports org.cirdles.tripoli.plots;
    exports org.cirdles.tripoli.utilities;

    opens org.cirdles.tripoli.sessions.analysis.methods.machineMethods to jakarta.xml.bind;
    exports org.cirdles.tripoli.sessions;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels;
    exports org.cirdles.tripoli.utilities.file;
    exports org.cirdles.tripoli.sessions.analysis;
    exports org.cirdles.tripoli.constants;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups;
    opens org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec to jakarta.xml.bind;
    exports org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec;
    exports org.cirdles.tripoli.plots.analysisPlotBuilders;
    exports org.cirdles.tripoli.plots.compoundPlotBuilders;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc.initializers;
    exports org.cirdles.tripoli.utilities.mathUtilities.weightedMeans;
    exports org.cirdles.tripoli.expressions.userFunctions;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne;
    exports org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix;
    exports org.cirdles.tripoli.utilities.mathUtilities;
}