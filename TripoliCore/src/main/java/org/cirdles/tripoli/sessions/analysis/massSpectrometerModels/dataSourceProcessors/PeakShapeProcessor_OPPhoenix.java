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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors;



import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerModel;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.peakShapes.PeakShapeOutputDataRecord;
import org.ojalgo.matrix.Primitive32Matrix;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ian Robinson
 * @author James F. Bowring
 */
public class PeakShapeProcessor_OPPhoenix {
    private final AnalysisMethod analysisMethod;

    private PeakShapeProcessor_OPPhoenix(AnalysisMethod analysisMethod) {
        this.analysisMethod = analysisMethod;
    }

    public static PeakShapeProcessor_OPPhoenix initializeWithAnalysisMethod(AnalysisMethod analysisMethod) {
        return new PeakShapeProcessor_OPPhoenix(analysisMethod);
    }

    public PeakShapeOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

        // Matrix Factory/Store Factory
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
        Primitive32Matrix.Factory matrixFactory = Primitive32Matrix.FACTORY;
        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));

        List<String[]> headerLine = new ArrayList<>();
        List<String[]> columnNames = new ArrayList<>();
        List<Double> masses = new ArrayList<>();
        List<Double> intensity = new ArrayList<>();

        int phase = 0;
        for (String line : contentsByLine) {
            if (!line.isEmpty()) {
                switch (phase) {
                    case 0 -> headerLine.add(line.split("\\s*,\\s*"));
                    case 1 -> columnNames.add(line.split("\\s*,\\s*"));
                    case 2 -> {
                        String[] cols = line.split("\\s*,\\s*");
                        masses.add(Double.parseDouble(cols[0]));
                        intensity.add(Double.parseDouble(cols[1]));
                    }
                }

                if (line.startsWith("#START")) {
                    phase = 1;
                } else if (phase == 1) {
                    phase = 2;
                }
            }
        }

        String detectorName = headerLine.get(1)[1];
        String massID = headerLine.get(2)[1];
        double peakCenterMass = Double.parseDouble(headerLine.get(4)[1]);
        double integrationPeriodMS = Double.parseDouble(headerLine.get(10)[1].replaceFirst("ms", ""));

        double[] magMasses = masses.stream().mapToDouble(d -> d).toArray();
        // Matrix magnetMasses = new Matrix(magMasses, magMasses.length);
        // Ojalgo matrix Test
        Primitive64Store magnetMassesOJ = storeFactory.columns(magMasses);

        
        double[] mPeakIntensity = intensity.stream().mapToDouble(d -> d).toArray();
        // Matrix measuredPeakIntensities = new Matrix(mPeakIntensity, mPeakIntensity.length);
        // Ojalgo matrix Test
        Primitive64Store measuredPeakIntensitiesOJ = storeFactory.columns(mPeakIntensity);

        MassSpectrometerModel massSpec = analysisMethod.getMassSpectrometer();
        double collectorWidthAMU = peakCenterMass / massSpec.getEffectiveRadiusMagnetMM() * massSpec.getCollectorWidthMM();
        double theoreticalBeamWidthAMU = peakCenterMass / massSpec.getEffectiveRadiusMagnetMM() * massSpec.getTheoreticalBeamWidthMM();

        // collectorLimits is a matrix with two columns and the same
        // number of rows as magnet masses.  Each row contains the mass
        // range of the beam that is entering the collector (defined by
        // collectorWidthAMU)
        double[][] collector = new double[magnetMassesOJ.getRowDim()][2];
        for (int i = 0; i < collector.length; i++) {
            collector[i][0] = magnetMassesOJ.get(i, 0) - collectorWidthAMU / 2;
            collector[i][1] = magnetMassesOJ.get(i, 0) + collectorWidthAMU / 2;
        }
        // Matrix collectorLimits = new Matrix(collector);
        Primitive64Store collectorLimitsOJ = storeFactory.rows(collector);

        double deltaMagnetMass = magnetMassesOJ.get(1, 0) - magnetMassesOJ.get(0, 0);
        double beamWindow = theoreticalBeamWidthAMU * 2.0;

        return new PeakShapeOutputDataRecord(
                magnetMassesOJ,
                measuredPeakIntensitiesOJ,
                peakCenterMass,
                integrationPeriodMS,
                massID,
                detectorName,
                collectorWidthAMU,
                theoreticalBeamWidthAMU,
                collectorLimitsOJ,
                deltaMagnetMass,
                beamWindow
        );
    }
}