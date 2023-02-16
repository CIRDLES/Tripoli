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

package org.cirdles.tripoli.sessions.analysis.methods;

import org.apache.commons.lang3.math.NumberUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec.PhoenixAnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.ConstantsTripoliCore.SPACES_100;

/**
 * @author James F. Bowring
 */
public class AnalysisMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = -642166785514147638L;

    private String methodName;
    private BaselineTable baselineTable;
    private SequenceTable sequenceTable;
    private List<SpeciesRecordInterface> speciesList;
    private List<IsotopicRatio> isotopicRatiosList;
    private MassSpectrometerContextEnum massSpectrometerContext;


    private AnalysisMethod(String methodName, MassSpectrometerContextEnum massSpectrometerContext) {
        this(methodName, massSpectrometerContext, BaselineTable.createEmptyBaselineTable(), SequenceTable.createEmptySequenceTable());
    }

    private AnalysisMethod(String methodName, MassSpectrometerContextEnum massSpectrometerContext, BaselineTable baselineTable, SequenceTable sequenceTable) {
        this.methodName = methodName;
        this.massSpectrometerContext = massSpectrometerContext;
        speciesList = new ArrayList<>();
        this.baselineTable = baselineTable;
        this.sequenceTable = sequenceTable;
        isotopicRatiosList = new ArrayList<>();
    }

    public static AnalysisMethod initializeAnalysisMethod(String methodName, MassSpectrometerContextEnum massSpectrometerContext) {
        return new AnalysisMethod(methodName, massSpectrometerContext);
    }

    public static AnalysisMethod createAnalysisMethodFromPhoenixAnalysisMethod(
            PhoenixAnalysisMethod phoenixAnalysisMethod,
            DetectorSetup detectorSetup,
            MassSpectrometerContextEnum massSpectrometerContext) {
        AnalysisMethod analysisMethod = new AnalysisMethod(
                phoenixAnalysisMethod.getHEADER().getFilename(), massSpectrometerContext);

        // first build baseline table
        List<PhoenixAnalysisMethod.BASELINE> baselineSequences = phoenixAnalysisMethod.getBASELINE();
        analysisMethod.baselineTable.setSequenceCount(baselineSequences.size());

        // determine whether AxialCollector is Ax or PM in order to determine mass entries in baselineTable
        Detector axialDetector = null;
        String axialCollectorName = phoenixAnalysisMethod.getSETTINGS().getAxialColl();
        if (axialCollectorName.startsWith("A")) {
            axialDetector = detectorSetup.getMapOfDetectors().get("Ax");
        } else {
            axialDetector = detectorSetup.getMapOfDetectors().get("PM");
        }

        List<PhoenixAnalysisMethod.ONPEAK> onPeakSequences = phoenixAnalysisMethod.getONPEAK();
        analysisMethod.sequenceTable.setSequenceCount(onPeakSequences.size());

        for (PhoenixAnalysisMethod.ONPEAK onpeakSequence : onPeakSequences) {
            String sequenceNumber = onpeakSequence.getSequence();
            // <CollectorArray>147Sm:H1S1,148Sm:H2S1,149Sm:H3S1,150Sm:H4S1</CollectorArray>
            String[] collectorArray = onpeakSequence.getCollectorArray().trim().split(",");
            for (String cellSpec : collectorArray) {
                String[] cellSpecs = cellSpec.split(":");
                int massNumber;
                String elementName;
                // determine whether isotopes are written Pb206 instead of the preferred 206Pb
                if (NumberUtils.isCreatable(cellSpecs[0].substring(0, 1))) {
                    int indexOfElementNameStart = cellSpecs[0].split("\\d\\D")[0].length() + 1;
                    massNumber = Integer.parseInt(cellSpecs[0].substring(0, indexOfElementNameStart));
                    elementName = cellSpecs[0].substring(indexOfElementNameStart);
                } else {
                    String[] cellSpecsSub = cellSpecs[0].split("\\D");
                    massNumber = Integer.parseInt(cellSpecsSub[cellSpecsSub.length - 1]);
                    elementName = cellSpecs[0].split("\\d")[0];
                }

                SpeciesRecordInterface species = NuclidesFactory.retrieveSpecies(elementName, massNumber);
                analysisMethod.addSpeciesToSpeciesList(species);
                analysisMethod.sortSpeciesListByAbundance();
                analysisMethod.createBaseListOfRatios();

                String detectorName = cellSpecs[1].split("S")[0];
                Detector detector = detectorSetup.getMapOfDetectors().get(detectorName);
                String[] baselineReferencesArray = onpeakSequence.getBLReferences().trim().split(",");
                List<String> baselineRefsList = new ArrayList<>();
                for (String baselineRef : baselineReferencesArray) {
                    if (!baselineRef.isBlank()) {
                        baselineRefsList.add(baselineRef.trim());
                        int baselineSequenceNumber = Integer.parseInt(baselineRef.trim().split(".\\D")[1]);
                        BaselineCell baselineCell = analysisMethod.baselineTable.accessBaselineCellForDetector(
                                detector, baselineRef.trim(), baselineSequenceNumber);
                        // determine mass
                        // TODO: upgrade from using massOffsetFromStationary = [-4 -3 -2 -1 0 1 2 3 4];
                        if (detector.equals(axialDetector)) {
                            // rule per Noah - if <BLReference> empty or == "MASS", use <AxMass>, else mass from <BLReference>
                            double axMassOffset = Double.parseDouble(baselineSequences.get(baselineSequenceNumber - 1).getAxMassOffset());
                            String baselineRefs = baselineSequences.get(baselineSequenceNumber - 1).getBLReferences();
                            if (baselineRefs.isBlank() || 0 == baselineRefs.compareToIgnoreCase("MASS")) {
                                double axMass = Double.parseDouble(baselineSequences.get(baselineSequenceNumber - 1).getAxMass());
                                baselineCell.setCellMass(axMass + axMassOffset);
                            } else {
                                double axMass = Double.parseDouble(baselineRefs.split("(?<=\\d)(?=\\D)|(?=\\d)(?<=\\D)")[0]);
                                baselineCell.setCellMass(axMass + axMassOffset);
                            }
                        }
                    }
                }
                SequenceCell sequenceCell = analysisMethod.sequenceTable.accessSequenceCellForDetector(
                        detector, "OP" + sequenceNumber, Integer.parseInt(sequenceNumber), baselineRefsList);
                sequenceCell.addTargetSpecies(species);

            }
        }

        // post-process baselineTable to populate with masses
        // TODO: make deltas more robust - Noah will have matlab code
        Map<Detector, List<BaselineCell>> mapOfDetectorsToBaselineCells = analysisMethod.baselineTable.getMapOfDetectorsToBaselineCells();
        List<BaselineCell> axialBaselineCells = mapOfDetectorsToBaselineCells.get(axialDetector);
        // this index is used for either Ax or PM when calculating masses
        int ordinalIndexOfAxial = detectorSetup.getMapOfDetectors().get("Ax").getOrdinalIndex();
        for (Detector detector : mapOfDetectorsToBaselineCells.keySet()) {
            if (!detector.equals(axialDetector)) {
                int ordinalIndex = detector.getOrdinalIndex();
                List<BaselineCell> baselineCells = mapOfDetectorsToBaselineCells.get(detector);
                for (BaselineCell baselineCell : baselineCells) {
                    int baselineCellIndex = baselineCell.getBaselineSequence();
                    List<BaselineCell> axialBaseLineCellListOfOne = axialBaselineCells
                            .stream()
                            .filter(c -> c.getBaselineSequence() == baselineCellIndex)
                            .collect(Collectors.toList());
                    baselineCell.setCellMass((ordinalIndex - ordinalIndexOfAxial) + axialBaseLineCellListOfOne.get(0).getCellMass());
                }
            }

        }

        return analysisMethod;
    }

    public static String compareAnalysisMethodToDataFileSpecs(AnalysisMethod analysisMethod, MassSpecExtractedData massSpecExtractedData) {
        String retVal = "";
        if (0 != analysisMethod.methodName.compareToIgnoreCase(massSpecExtractedData.getHeader().methodName())) {
            retVal += "Method name: " + analysisMethod.methodName + " differs from data file's method name. \n";
        }

        Set<String> baselineNames = new TreeSet<>(List.of(massSpecExtractedData.getBlocksData().get(1).baselineIDs()));
        if (analysisMethod.baselineTable.getSequenceCount() != baselineNames.size()) {
            retVal += "Baseline table has " + analysisMethod.baselineTable.getSequenceCount() + " sequences. \n";
        }

        Set<String> onPeakNames = new TreeSet<>(List.of(massSpecExtractedData.getBlocksData().get(1).onPeakIDs()));
        if (analysisMethod.sequenceTable.getSequenceCount() != onPeakNames.size()) {
            retVal += "Sequence table has " + analysisMethod.sequenceTable.getSequenceCount() + " sequences. \n";
        }

        return retVal;
    }

    private String prettyPrintSequenceTable() {
        StringBuilder retVal = new StringBuilder();
        Map<Detector, List<SequenceCell>> detectorToSequenceCell = sequenceTable.getMapOfDetectorsToSequenceCells();
        detectorToSequenceCell.entrySet().stream()
                .forEach(e -> {
                    retVal.append(e.getKey().getDetectorName()).append(" ");
                    boolean offset = false;
                    for (SequenceCell sequenceCell : e.getValue()) {
                        int sequenceNumber = sequenceCell.getOnPeakSequence();
                        if (!offset) {
                            retVal.append(SPACES_100, 0, (sequenceNumber - 1) * 10);
                            offset = true;
                        }
                        retVal.append(sequenceCell.getSequenceId()).append(":").append(sequenceCell.getTargetSpecies().prettyPrintShortForm()).append(" ");
                    }

                    retVal.append("\n");
                });

        return retVal.toString();
    }

    public String prettyPrintMethodSummary(boolean onTwoLines) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("Method: ").append(methodName).append(SPACES_100, 0, 65 - methodName.length()).append(onTwoLines ? "\nSpecies: " : "  Species: ");
        List<SpeciesRecordInterface> speciesAlphabetic = new ArrayList<>(speciesList);
        Collections.sort(speciesAlphabetic, Comparator.comparing(s -> s.getAtomicMass()));
        for (SpeciesRecordInterface species : speciesAlphabetic) {
            retVal.append(species.prettyPrintShortForm() + " ");
        }

        return retVal.toString();
    }

    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof AnalysisMethod otherAnalysisMethod) {
                retVal = 0 == methodName.compareToIgnoreCase(otherAnalysisMethod.methodName);
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (null == methodName ? 0 : methodName.hashCode());
        return hash;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public MassSpectrometerContextEnum getMassSpectrometerContext() {
        return massSpectrometerContext;
    }

    public List<SpeciesRecordInterface> getSpeciesList() {
        return speciesList;
    }

    public void setSpeciesList(List<SpeciesRecordInterface> speciesList) {
        this.speciesList = speciesList;
    }

    public void addSpeciesToSpeciesList(SpeciesRecordInterface species) {
        if (null == speciesList) {
            speciesList = new ArrayList<>();
        }
        if (!speciesList.contains(species)) {
            speciesList.add(species);
        }
    }

    public void sortSpeciesListByAbundance() {
        Collections.sort(speciesList, Comparator.comparing(s -> s.getNaturalAbundancePercent()));
    }

    public BaselineTable getBaselineTable() {
        return baselineTable;
    }

    public void setBaselineTable(BaselineTable baselineTable) {
        this.baselineTable = baselineTable;
    }

    public SequenceTable getSequenceTable() {
        return sequenceTable;
    }

    public void setSequenceTable(SequenceTable sequenceTable) {
        this.sequenceTable = sequenceTable;
    }

    public List<IsotopicRatio> getTripoliRatiosList() {
        return isotopicRatiosList;
    }

    public void setTripoliRatiosList(List<IsotopicRatio> isotopicRatiosList) {
        this.isotopicRatiosList = isotopicRatiosList;
    }

    public void addRatioToIsotopicRatiosList(IsotopicRatio isotopicRatio) {
        if (null == isotopicRatiosList) {
            isotopicRatiosList = new ArrayList<>();
        }
        if (!isotopicRatiosList.contains(isotopicRatio)) {
            isotopicRatiosList.add(isotopicRatio);
        }
    }

    /**
     * Creates a ratio of each species except the last one divided by the last one in specieslist
     */
    public void createBaseListOfRatios() {
        isotopicRatiosList = new ArrayList<>();
        SpeciesRecordInterface highestAbundanceSpecies = speciesList.get(speciesList.size() - 1);
        for (int speciesIndex = 0; speciesIndex < speciesList.size() - 1; speciesIndex++) {
            addRatioToIsotopicRatiosList(new IsotopicRatio(speciesList.get(speciesIndex), highestAbundanceSpecies));
        }
    }

}