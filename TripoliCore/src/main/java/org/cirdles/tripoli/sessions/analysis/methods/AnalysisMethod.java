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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.cirdles.tripoli.constants.MassSpectrometerContextEnum;
import org.cirdles.tripoli.expressions.species.IsotopicRatio;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;
import org.cirdles.tripoli.expressions.species.nuclides.NuclidesFactory;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.MassSpecExtractedData;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.Detector;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.detectorSetups.DetectorSetup;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineCell;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec.PhoenixAnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceCell;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.cirdles.tripoli.constants.TripoliConstants.SPACES_100;

/**
 * @author James F. Bowring
 */
public class AnalysisMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = -642166785514147638L;
    private final MassSpectrometerContextEnum massSpectrometerContext;
    public Map<String, Boolean> mapOfRatioNamesToInvertedFlag;
    private String methodName;
    private BaselineTable baselineTable;
    private SequenceTable sequenceTable;
    private List<SpeciesRecordInterface> speciesList;
    private List<IsotopicRatio> isotopicRatiosList;
    private List<IsotopicRatio> derivedIsotopicRatiosList;
    private BiMap<IsotopicRatio, IsotopicRatio> biMapOfRatiosAndInverses = HashBiMap.create();
    private List<UserFunction> userFunctions;
    private boolean useLinearKnots;

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
        derivedIsotopicRatiosList = new ArrayList<>();
        mapOfRatioNamesToInvertedFlag = new TreeMap<>();
        userFunctions = new ArrayList<>();
        this.useLinearKnots = true;
    }

    public static AnalysisMethod initializeAnalysisMethod(String methodName, MassSpectrometerContextEnum massSpectrometerContext) {
        return new AnalysisMethod(methodName, massSpectrometerContext);
    }

    public static AnalysisMethod createAnalysisMethodFromCase1(
            MassSpecExtractedData massSpecExtractedData) {
        AnalysisMethod analysisMethod = new AnalysisMethod("Derived for Case1", massSpecExtractedData.getMassSpectrometerContext());
        String[] columnHeaders = massSpecExtractedData.getColumnHeaders();
        // ignore first two columns: Cycle, Time
        String regex = "[^alpha].*\\d?:?\\(?\\d{2,3}.{0,2}\\/\\d?:?\\d{2,3}.{0,2}.*";
        for (int i = 2; i < columnHeaders.length; i++) {
//            System.out.println(columnHeaders[i] + "   " + columnHeaders[i].matches(regex));
            UserFunction userFunction = new UserFunction(columnHeaders[i].trim(), i - 2, columnHeaders[i].matches(regex), true);//columnHeaders[i].matches(regex));
            analysisMethod.getUserFunctions().add(userFunction);
        }

        return analysisMethod;
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

        analysisMethod.sortSpeciesListByAbundance();
        analysisMethod.createListsOfIsotopicRatios();

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

        Set<String> baselineNames = new TreeSet<>(List.of(massSpecExtractedData.getBlocksDataFull().get(1).baselineIDs()));
        if (analysisMethod.baselineTable.getSequenceCount() != baselineNames.size()) {
            retVal += "Baseline table has " + analysisMethod.baselineTable.getSequenceCount() + " sequences. \n";
        }

        Set<String> onPeakNames = new TreeSet<>(List.of(massSpecExtractedData.getBlocksDataFull().get(1).onPeakIDs()));
        if (analysisMethod.sequenceTable.getSequenceCount() != onPeakNames.size()) {
            retVal += "Sequence table has " + analysisMethod.sequenceTable.getSequenceCount() + " sequences. \n";
        }

        return retVal;
    }

    public boolean isUseLinearKnots() {
        return useLinearKnots;
    }

    public void toggleKnotsMethod() {
        this.useLinearKnots = !this.useLinearKnots;
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

    public String prettyPrintMethodSummary(boolean verbose) {
        StringBuilder retVal = new StringBuilder();
        retVal.append("Method: ").append(methodName).append(SPACES_100, 0, 55 - methodName.length()).append(verbose ? "\nSpecies: " : "  Species: ");
        List<SpeciesRecordInterface> speciesAlphabetic = new ArrayList<>(speciesList);
        Collections.sort(speciesAlphabetic, Comparator.comparing(s -> s.getAtomicMass()));
        for (SpeciesRecordInterface species : speciesAlphabetic) {
            retVal.append(species.prettyPrintShortForm() + " ");
        }
        if (verbose) {
            retVal.append("\nIsotopicRatios: ");
            for (IsotopicRatio ratio : isotopicRatiosList) {
                retVal.append("\n\t\t" + ratio.prettyPrint());
            }
            for (UserFunction userFunction : userFunctions) {
                if (userFunction.isTreatAsIsotopicRatio()) {
                    retVal.append("\n\t\t" + userFunction.getName());
                }
            }
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
        Collections.sort(speciesList, Comparator.comparing(s -> s.getMassNumber()));
    }

    public List<SpeciesRecordInterface> getSpeciesListSortedByMass() {
        List<SpeciesRecordInterface> speciesListCopy = new ArrayList<>(speciesList);
        Collections.sort(speciesListCopy, Comparator.comparing(s -> s.getMassNumber()));
        return speciesListCopy;
    }

    public SpeciesRecordInterface retrieveHighestAbundanceSpecies() {
        return speciesList.get(speciesList.size() - 1);
    }

    public BaselineTable getBaselineTable() {
        return baselineTable;
    }

    public SequenceTable getSequenceTable() {
        return sequenceTable;
    }

    public List<IsotopicRatio> getIsotopicRatiosList() {
        return isotopicRatiosList;
    }

    public List<IsotopicRatio> getCloneOfIsotopicRatiosList() {
        List<IsotopicRatio> cloneOfIsotopicRatios = new ArrayList<>();
        for (IsotopicRatio ir : isotopicRatiosList) {
            cloneOfIsotopicRatios.add(new IsotopicRatio(ir.getNumerator(), ir.getDenominator(), ir.isDisplayed()));
        }
        return cloneOfIsotopicRatios;
    }

    public List<IsotopicRatio> getDerivedIsotopicRatiosList() {
        return derivedIsotopicRatiosList;
    }

    public List<IsotopicRatio> getCloneOfDerivedIsotopicRatiosList() {
        List<IsotopicRatio> cloneOfDerivedIsotopicRatios = new ArrayList<>();
        for (IsotopicRatio ir : derivedIsotopicRatiosList) {
            cloneOfDerivedIsotopicRatios.add(new IsotopicRatio(ir.getNumerator(), ir.getDenominator(), ir.isDisplayed()));
        }
        return cloneOfDerivedIsotopicRatios;
    }

    public BiMap<IsotopicRatio, IsotopicRatio> getBiMapOfRatiosAndInverses() {
        return biMapOfRatiosAndInverses;
    }

    public Map<String, Boolean> getMapOfRatioNamesToInvertedFlag() {
        return mapOfRatioNamesToInvertedFlag;
    }

    public List<UserFunction> getUserFunctions() {
        return userFunctions;
    }

    public void addRatioToIsotopicRatiosList(IsotopicRatio isotopicRatio) {
        if (null == isotopicRatiosList) {
            isotopicRatiosList = new ArrayList<>();
        }
        if (!isotopicRatiosList.contains(isotopicRatio)) {
            isotopicRatiosList.add(isotopicRatio);
        }
    }

    public void addRatioToDerivedIsotopicRatiosList(IsotopicRatio isotopicRatio) {
        if (null == derivedIsotopicRatiosList) {
            derivedIsotopicRatiosList = new ArrayList<>();
        }
        if (!derivedIsotopicRatiosList.contains(isotopicRatio)) {
            derivedIsotopicRatiosList.add(isotopicRatio);
        }
    }


    /**
     * Creates a ratio of each species except the last one divided by the last one in specieslist
     */
    public void createListsOfIsotopicRatios() {
        sortSpeciesListByAbundance();
        isotopicRatiosList = new ArrayList<>();
        SpeciesRecordInterface highestAbundanceSpecies = speciesList.get(speciesList.size() - 1);
        for (int speciesIndex = 0; speciesIndex < speciesList.size() - 1; speciesIndex++) {
            addRatioToIsotopicRatiosList(new IsotopicRatio(speciesList.get(speciesIndex), highestAbundanceSpecies, true));
        }
        for (int i = 0; i < isotopicRatiosList.size() - 1; i++) {
            IsotopicRatio ratioOne = isotopicRatiosList.get(i);
            for (int j = i + 1; j < isotopicRatiosList.size(); j++) {
                IsotopicRatio ratioTwo = isotopicRatiosList.get(j);
                IsotopicRatio derivedRatio = new IsotopicRatio(ratioOne.getNumerator(), ratioTwo.getNumerator(), false);
                addRatioToDerivedIsotopicRatiosList(derivedRatio);
                IsotopicRatio inverseDerivedRatio = new IsotopicRatio(ratioTwo.getNumerator(), ratioOne.getNumerator(), false);
                addRatioToDerivedIsotopicRatiosList(inverseDerivedRatio);
                biMapOfRatiosAndInverses.put(derivedRatio, inverseDerivedRatio);
            }
        }
        // remaining inverses
        for (int i = 0; i < isotopicRatiosList.size(); i++) {
            IsotopicRatio ratio = isotopicRatiosList.get(i);
            IsotopicRatio invertedRatio = new IsotopicRatio(ratio.getDenominator(), ratio.getNumerator(), false);
            addRatioToDerivedIsotopicRatiosList(invertedRatio);
            biMapOfRatiosAndInverses.put(ratio, invertedRatio);
        }

        Collections.sort(derivedIsotopicRatiosList, (ratio1, ratio2) -> ratio1.getNumerator().compareTo(ratio2.getNumerator()));
    }
}