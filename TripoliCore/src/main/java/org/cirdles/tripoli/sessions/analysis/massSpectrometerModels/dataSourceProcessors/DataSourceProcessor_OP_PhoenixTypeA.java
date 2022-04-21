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

import jama.Matrix;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataModel;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorEnumTypeA;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DataSourceProcessor_OP_PhoenixTypeA implements DataSourceProcessorInterface {

    private static final List<SpeciesRecordInterface> speciesList = new ArrayList<>();

    static {
        // build NuclideRecord list with dummy first entry as placeholder for index compatibility with matlab
        speciesList.add(NuclidesFactory.retrieveSpecies("n", 1));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 206));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 208));
    }

    @Override
    public MassSpecOutputDataModel prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

        List<String> contentsByLine = new ArrayList<>(Files.readAllLines(inputDataFile, Charset.defaultCharset()));

        List<String[]> headerByLineSplit = new ArrayList<>();
        List<String[]> columnNamesSplit = new ArrayList<>();
        List<String> sequenceIDByLineSplit = new ArrayList<>();
        List<String> blockNumberByLineSplit = new ArrayList<>();
        List<String> cycleNumberByLineSplit = new ArrayList<>();
        List<String> integrationNumberByLineSplit = new ArrayList<>();
        List<String> timeStampByLineSplit = new ArrayList<>();
        List<String> massByLineSplit = new ArrayList<>();
        List<String[]> detectorDataByLineSplit = new ArrayList<>();

        int phase = 0;
        for (String line : contentsByLine) {
            switch (phase) {
                case 0 -> headerByLineSplit.add(line.split(","));
                case 1 -> columnNamesSplit.add(line.split(","));
                case 2 -> {
                    String[] lineSplit = line.split(",");
                    sequenceIDByLineSplit.add(lineSplit[0]);
                    blockNumberByLineSplit.add(lineSplit[1]);
                    cycleNumberByLineSplit.add(lineSplit[2]);
                    integrationNumberByLineSplit.add(lineSplit[3]);
                    timeStampByLineSplit.add(lineSplit[4]);
                    massByLineSplit.add(lineSplit[5]);

                    detectorDataByLineSplit.add(Arrays.copyOfRange(lineSplit, 6, lineSplit.length));
                }
            }
            if (line.startsWith("#START")) {
                phase = 1;
            } else if (phase == 1) {
                phase = 2;
            }
        }

        String[] sequenceID = sequenceIDByLineSplit.toArray(new String[0]);
        double[] blockNumber = convertListOfNumbersAsStringsToDoubleArray(blockNumberByLineSplit);
        double[] cycleNumber = convertListOfNumbersAsStringsToDoubleArray(cycleNumberByLineSplit);
        double[] integrationNumber = convertListOfNumbersAsStringsToDoubleArray(integrationNumberByLineSplit);
        double[] timeStamp = convertListOfNumbersAsStringsToDoubleArray(timeStampByLineSplit);
        double[] mass = convertListOfNumbersAsStringsToDoubleArray(massByLineSplit);


        // convert detectorDataByLineSplit to doubles array
        int totalCountOfIntegrations = sequenceIDByLineSplit.size();
        double[][] detectorData = new double[totalCountOfIntegrations][];
        int index = 0;
        for (String[] numbersAsStrings : detectorDataByLineSplit) {
            String[] detectorValues = Arrays.copyOfRange(numbersAsStrings, 0, numbersAsStrings.length);
            detectorData[index] = Arrays.stream(detectorValues)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
            index++;
        }


        /* TODO: provide a model of the data acquisition including a map of
                the sequences to the detectors;  here we are temporarily hard-coding
                BL1 to Ax_Fara, Axial, and High1;
                S1 to Axial and High1;
                S2 to Ax_Fara and Axial

                Baseline Table
                Amplifier Number	1	 2	 3	 4	   5	 NA	     6	7	8	9
                Detector name	    L5	L4	L3	L2	Ax Fara	Axial	H1	H2	H3	H4
                BL1					                 203.5	205.5  207.5

                Sequence Table
                Amplifier Num	1	2	3	4	   5	 NA	     6	7	8	9
                Detector name	L5	L4	L3	L2	Ax Fara	Axial	H1	H2	H3	H4
                S1						                206Pb	208Pb
                S2					             206Pb	208Pb

            so the algorithm for building the data vector basically works the faradays from left to right
            recording meaningful entries for each sequence grouped together and ignoring the other
            sequences per the table;  then at the end adds the axial column grouped by sequence
            that may or may not (in this case not) start with BL1 axial ...

            each sequence is made of integrations, each cycle is made of sequences,
            each block is made of cycles, and each measurement is made of blocks.
        */


        // build Baseline and sequence tables experiment
        Map<DetectorEnumTypeA, Map<String, SpeciesRecordInterface>> baselineTable = new LinkedHashMap<>();

        Map<String, SpeciesRecordInterface> AX_FARA_Map = new LinkedHashMap<>();
        AX_FARA_Map.put("BL1", speciesList.get(0));

        Map<String, SpeciesRecordInterface> AXIAL_Map = new LinkedHashMap<>();
        AXIAL_Map.put("BL1", speciesList.get(0));

        Map<String, SpeciesRecordInterface> H1_Map = new LinkedHashMap<>();
        H1_Map.put("BL1", speciesList.get(0));

        baselineTable.put(DetectorEnumTypeA.AX_FARA, AX_FARA_Map);
        baselineTable.put(DetectorEnumTypeA.AXIAL, AXIAL_Map);
        baselineTable.put(DetectorEnumTypeA.H1, H1_Map);

        // sequence table
        Map<DetectorEnumTypeA, Map<String, SpeciesRecordInterface>> sequenceTable = new LinkedHashMap<>();

        AX_FARA_Map = new LinkedHashMap<>();
        AX_FARA_Map.put("S2", speciesList.get(1));

        AXIAL_Map = new LinkedHashMap<>();
        AXIAL_Map.put("S1", speciesList.get(1));
        AXIAL_Map.put("S2", speciesList.get(2));

        H1_Map = new LinkedHashMap<>();
        H1_Map.put("S1", speciesList.get(2));

        sequenceTable.put(DetectorEnumTypeA.AX_FARA, AX_FARA_Map);
        sequenceTable.put(DetectorEnumTypeA.AXIAL, AXIAL_Map);
        sequenceTable.put(DetectorEnumTypeA.H1, H1_Map);


        // start with Baseline table
        AccumulatedData baselineFaradayAccumulator = accumulateDataPerTableSpecs(sequenceID, detectorData, baselineTable, true);
        // now sequence table Faraday
        AccumulatedData sequenceFaradayAccumulator = accumulateDataPerTableSpecs(sequenceID, detectorData, sequenceTable, true);
        // now sequence table NOT Faraday (ion counter)
        AccumulatedData sequenceIonCounterAccumulator = accumulateDataPerTableSpecs(sequenceID, detectorData, sequenceTable, false);

        List<Double> dataAccumulatorList = new ArrayList<>();
        dataAccumulatorList.addAll(baselineFaradayAccumulator.dataAccumulatorList);
        dataAccumulatorList.addAll(sequenceFaradayAccumulator.dataAccumulatorList);
        dataAccumulatorList.addAll(sequenceIonCounterAccumulator.dataAccumulatorList);

        List<Double> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        isotopeIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.isotopeIndicesForDataAccumulatorList);
        isotopeIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.isotopeIndicesForDataAccumulatorList);
        isotopeIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.isotopeIndicesForDataAccumulatorList);

        List<Double> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        baseLineFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.baseLineFlagsForDataAccumulatorList);
        baseLineFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.baseLineFlagsForDataAccumulatorList);
        baseLineFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.baseLineFlagsForDataAccumulatorList);

        // convert to arrays to  build parameters for MassSpecOutputDataModel record
        double[] dataAccumulatorArray = dataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix rawDataColumn = new Matrix(dataAccumulatorArray, dataAccumulatorArray.length);

        double[] isotopeIndicesForDataAccumulatorArray = isotopeIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix isotopeIndicesForRawDataColumn = new Matrix(isotopeIndicesForDataAccumulatorArray, isotopeIndicesForDataAccumulatorArray.length);

        double[] baseLineFlagsForDataAccumulatorArray = baseLineFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix baseLineFlagsForRawDataColumn = new Matrix(baseLineFlagsForDataAccumulatorArray, baseLineFlagsForDataAccumulatorArray.length);

        return new MassSpecOutputDataModel(
                rawDataColumn,
                isotopeIndicesForRawDataColumn,
                baseLineFlagsForRawDataColumn);
    }

    /**
     * detectorData has a column for each of the enumerated detectors
     * and the ordinal of the enumerated detector is the column index into detectorData
     * sequenceID column index = the row index for sequence integrations
     *
     * @param sequenceID
     * @param detectorData
     * @param tableSpecs
     * @param faraday
     * @return
     */
    private AccumulatedData accumulateDataPerTableSpecs(
            String[] sequenceID, double[][] detectorData, Map<DetectorEnumTypeA, Map<String, SpeciesRecordInterface>> tableSpecs, boolean faraday) {
        List<Double> dataAccumulatorList = new ArrayList<>();
        List<Double> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        List<Double> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        for (DetectorEnumTypeA detector : tableSpecs.keySet()) {
            if (detector.isFaraday() == faraday) {
                for (String sequenceName : tableSpecs.get(detector).keySet()) {
                    int detectorDataColumnIndex = detector.ordinal();
                    for (int detectorDataRowIndex = 0; detectorDataRowIndex < sequenceID.length; detectorDataRowIndex++) {
                        if (sequenceID[detectorDataRowIndex].toUpperCase(Locale.ROOT).compareTo(sequenceName.toUpperCase(Locale.ROOT)) == 0) {
                            dataAccumulatorList.add(detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                            isotopeIndicesForDataAccumulatorList.add((double) speciesList.indexOf(tableSpecs.get(detector).get(sequenceName)));
                            if (tableSpecs.get(detector).get(sequenceName).equals(speciesList.get(0))) {
                                baseLineFlagsForDataAccumulatorList.add(1.0);
                            } else {
                                baseLineFlagsForDataAccumulatorList.add(0.0);
                            }
//                            System.err.println(sequenceName + "  " + detectorDataRowIndex + "  " + detectorDataColumnIndex + "  " + detectorData[detectorDataRowIndex][detectorDataColumnIndex]);
                        }
                    }
                }
            }
        }

        return new AccumulatedData(dataAccumulatorList, isotopeIndicesForDataAccumulatorList, baseLineFlagsForDataAccumulatorList);
    }

    private double[] convertListOfNumbersAsStringsToDoubleArray(List<String> listToConvert) {
        double[] retVal = new double[listToConvert.size()];
        int index = 0;
        for (String blockNumberAsString : listToConvert) {
            retVal[index] = Double.parseDouble(blockNumberAsString);
            index++;
        }

        return retVal;
    }

    private record AccumulatedData(
            List<Double> dataAccumulatorList,
            List<Double> isotopeIndicesForDataAccumulatorList,
            List<Double> baseLineFlagsForDataAccumulatorList
    ) {
    }
}