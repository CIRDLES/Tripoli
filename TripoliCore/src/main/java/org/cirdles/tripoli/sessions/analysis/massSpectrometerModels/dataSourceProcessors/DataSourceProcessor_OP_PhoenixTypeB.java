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
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerModel;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.methods.baselineTables.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baselineTables.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.sequenceTables.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequenceTables.SequenceTable;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataSourceProcessor_OP_PhoenixTypeB implements DataSourceProcessorInterface {

    private final MassSpectrometerModel op_Phoenix;
    private final List<SpeciesRecordInterface> speciesList;

    public DataSourceProcessor_OP_PhoenixTypeB(MassSpectrometerModel op_Phoenix) {
        this.op_Phoenix = op_Phoenix;
        this.speciesList = new ArrayList<>();
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 204));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 205));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 206));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 207));
        speciesList.add(NuclidesFactory.retrieveSpecies("Pb", 208));
    }

    @Override
    public MassSpecOutputDataRecord prepareInputDataModelFromFile(Path inputDataFile) throws IOException {

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
            if (!line.isEmpty()) {
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
        DetectorSetup detectorSetup = op_Phoenix.getDetectorSetup();

        BaselineTable baselineTable = BaselineTable.createEmptyBaselineTable();

        BaselineCell baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "Bl1");
        baselineCell.setCellMass(203.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "Bl1");
        baselineCell.setCellMass(204.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "Bl1");
        baselineCell.setCellMass(205.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "Bl1");
        baselineCell.setCellMass(206.5);

        baselineCell = baselineTable.accessBaselineCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "Bl1");
        baselineCell.setCellMass(207.5);

        SequenceTable sequenceTable = SequenceTable.createEmptySequenceTable();

        SequenceCell sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L4"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 204));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 204));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L3"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 205));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 204));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 205));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("L2"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 204));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 205));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Ax_Fara"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 204));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 205));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("Axial"), "S5");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 205));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H1"), "S4");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 206));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H2"), "S3");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 207));
        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H3"), "S2");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));

        sequenceCell = sequenceTable.accessSequenceCellForDetector(detectorSetup.getMapOfDetectors().get("H4"), "S1");
        sequenceCell.addTargetSpecies(NuclidesFactory.retrieveSpecies("Pb", 208));


        // start with Baseline table
        DataSourceProcessorInterface.AccumulatedData baselineFaradayAccumulator = accumulateBaselineDataPerSequenceTableSpecs(sequenceID, detectorData, sequenceTable, true);
        // now sequence table Faraday
        DataSourceProcessorInterface.AccumulatedData sequenceFaradayAccumulator = accumulateDataPerSequenceTableSpecs(sequenceID, detectorData, sequenceTable, true);
        // now sequence table NOT Faraday (ion counter)
        DataSourceProcessorInterface.AccumulatedData sequenceIonCounterAccumulator = accumulateDataPerSequenceTableSpecs(sequenceID, detectorData, sequenceTable, false);

        List<Double> dataAccumulatorList = new ArrayList<>();
        dataAccumulatorList.addAll(baselineFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceFaradayAccumulator.dataAccumulatorList());
        dataAccumulatorList.addAll(sequenceIonCounterAccumulator.dataAccumulatorList());

        List<Double> isotopeIndicesForDataAccumulatorList = new ArrayList<>();
        isotopeIndicesForDataAccumulatorList.addAll(baselineFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceFaradayAccumulator.isotopeIndicesForDataAccumulatorList());
        isotopeIndicesForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.isotopeIndicesForDataAccumulatorList());

        List<Double> baseLineFlagsForDataAccumulatorList = new ArrayList<>();
        baseLineFlagsForDataAccumulatorList.addAll(baselineFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceFaradayAccumulator.baseLineFlagsForDataAccumulatorList());
        baseLineFlagsForDataAccumulatorList.addAll(sequenceIonCounterAccumulator.baseLineFlagsForDataAccumulatorList());

        // convert to arrays to  build parameters for MassSpecOutputDataRecord record
        double[] dataAccumulatorArray = dataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix rawDataColumn = new Matrix(dataAccumulatorArray, dataAccumulatorArray.length);

        double[] isotopeIndicesForDataAccumulatorArray = isotopeIndicesForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix isotopeIndicesForRawDataColumn = new Matrix(isotopeIndicesForDataAccumulatorArray, isotopeIndicesForDataAccumulatorArray.length);

        double[] baseLineFlagsForDataAccumulatorArray = baseLineFlagsForDataAccumulatorList.stream().mapToDouble(d -> d).toArray();
        Matrix baseLineFlagsForRawDataColumn = new Matrix(baseLineFlagsForDataAccumulatorArray, baseLineFlagsForDataAccumulatorArray.length);

        return new MassSpecOutputDataRecord(
                rawDataColumn,
                isotopeIndicesForRawDataColumn,
                baseLineFlagsForRawDataColumn);
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

    @Override
    public List<SpeciesRecordInterface> getSpeciesList() {
        return speciesList;
    }

}