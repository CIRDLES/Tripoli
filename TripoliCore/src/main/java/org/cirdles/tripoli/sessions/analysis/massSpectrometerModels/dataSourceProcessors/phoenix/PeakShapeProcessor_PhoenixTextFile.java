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

package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix;


import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerModel;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.peakShapes.PeakShapeOutputDataRecord;
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
public class PeakShapeProcessor_PhoenixTextFile {
    private final MassSpectrometerModel massSpectrometerModel;

    private PeakShapeProcessor_PhoenixTextFile(MassSpectrometerModel massSpectrometerModel) {
        this.massSpectrometerModel = massSpectrometerModel;
    }

    public static PeakShapeProcessor_PhoenixTextFile initializeWithMassSpectrometer(MassSpectrometerModel massSpectrometerModel) {
        return new PeakShapeProcessor_PhoenixTextFile(massSpectrometerModel);
    }

    public PeakShapeOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

        // Store Factory
        PhysicalStore.Factory<Double, Primitive64Store> storeFactory = Primitive64Store.FACTORY;
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
                    default -> {
                        masses = null;
                        intensity = null;
                    }
                }

                if (1 == phase) {
                    phase = 2;
                }
            } else {
                phase = 1;
            }
        }

        String detectorName = headerLine.get(1)[1];
        String massID = headerLine.get(2)[1];
        double peakCenterMass = Double.parseDouble(headerLine.get(4)[1]);
        double integrationPeriodMS = Double.parseDouble(headerLine.get(10)[1].replaceFirst("ms", ""));

        double[] magMasses = masses.stream().mapToDouble(d -> d).toArray();
        Primitive64Store magnetMasses = storeFactory.columns(magMasses);

        double[] mPeakIntensity = intensity.stream().mapToDouble(d -> d).toArray();
        Primitive64Store measuredPeakIntensities = storeFactory.columns(mPeakIntensity);

        double collectorWidthAMU = peakCenterMass / massSpectrometerModel.getEffectiveRadiusMagnetMM() * massSpectrometerModel.getCollectorWidthMM();
        double theoreticalBeamWidthAMU = peakCenterMass / massSpectrometerModel.getEffectiveRadiusMagnetMM() * massSpectrometerModel.getTheoreticalBeamWidthMM();

        // collectorLimits is a matrix with two columns and the same
        // number of rows as magnet masses.  Each row contains the mass
        // range of the beam that is entering the collector (defined by
        // collectorWidthAMU)
        double[][] collector = new double[magnetMasses.getRowDim()][2];
        for (int i = 0; i < collector.length; i++) {
            collector[i][0] = magnetMasses.get(i, 0) - collectorWidthAMU / 2;
            collector[i][1] = magnetMasses.get(i, 0) + collectorWidthAMU / 2;
        }

        Primitive64Store collectorLimits = storeFactory.rows(collector);

        double deltaMagnetMass = magnetMasses.get(1, 0) - magnetMasses.get(0, 0);
        double beamWindow = theoreticalBeamWidthAMU * 2.0;

        return new PeakShapeOutputDataRecord(
                magnetMasses,
                measuredPeakIntensities,
                peakCenterMass,
                integrationPeriodMS,
                massID,
                detectorName,
                collectorWidthAMU,
                theoreticalBeamWidthAMU,
                collectorLimits,
                deltaMagnetMass,
                beamWindow
        );
    }
}