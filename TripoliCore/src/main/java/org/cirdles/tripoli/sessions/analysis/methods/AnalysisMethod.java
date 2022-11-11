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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerBuiltinModelFactory;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.MassSpectrometerModel;
import org.cirdles.tripoli.sessions.analysis.methods.baseline.BaselineTable;
import org.cirdles.tripoli.sessions.analysis.methods.machineMethods.PhoenixAnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.sequence.SequenceTable;
import org.cirdles.tripoli.species.IsotopicRatio;
import org.cirdles.tripoli.species.SpeciesRecordInterface;
import org.cirdles.tripoli.species.nuclides.NuclidesFactory;

import java.io.Serial;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James F. Bowring
 */
public class AnalysisMethod implements Serializable {
    @Serial
    private static final long serialVersionUID = -642166785514147638L;

    protected String methodName;
    protected MassSpectrometerModel massSpectrometer;
    protected BaselineTable baselineTable;
    protected SequenceTable sequenceTable;
    private List<SpeciesRecordInterface> speciesList;
    private List<IsotopicRatio> isotopicRatiosList;


    private AnalysisMethod(String methodName, MassSpectrometerModel massSpectrometer) {
        this(methodName, massSpectrometer, BaselineTable.createEmptyBaselineTable(), SequenceTable.createEmptySequenceTable());
    }

    private AnalysisMethod(String methodName, MassSpectrometerModel massSpectrometer, BaselineTable baselineTable, SequenceTable sequenceTable) {
        this.methodName = methodName;
        this.massSpectrometer = massSpectrometer;
        this.speciesList = new ArrayList<>();
        this.baselineTable = baselineTable;
        this.sequenceTable = sequenceTable;
        this.isotopicRatiosList = new ArrayList<>();
    }

    public static AnalysisMethod initializeAnalysisMethod(String methodName, MassSpectrometerModel massSpectrometer) {
        return new AnalysisMethod(methodName, massSpectrometer);
    }

    public static AnalysisMethod createAnalysisMethodFromPhoenixAnalysisMethod(PhoenixAnalysisMethod phoenixAnalysisMethod){
        AnalysisMethod analysisMethod = new AnalysisMethod(
                phoenixAnalysisMethod.getHEADER().getFilename(),
                MassSpectrometerBuiltinModelFactory.massSpectrometersBuiltinMap.get("Phoenix"));

        List<PhoenixAnalysisMethod.ONPEAK> onPeakSequences = phoenixAnalysisMethod.getONPEAK();
        for (PhoenixAnalysisMethod.ONPEAK onpeakSequence : onPeakSequences) {
            String sequenceNumber = onpeakSequence.getSequence();
            // <CollectorArray>147Sm:H1S1,148Sm:H2S1,149Sm:H3S1,150Sm:H4S1</CollectorArray>
            String[] collectorArray = onpeakSequence.getCollectorArray()
                    .trim()
                    .replace("<CollectorArray>", "")
                    .replace("</CollectorArray>", "")
                    .split(",");
            for (String cellSpec : collectorArray){
                String[] cellSpecs = cellSpec.split(":");
                int indexOfElementNameStart = cellSpecs[0].split("\\d\\D\\D")[0].length() + 1;
                int massNumber = Integer.parseInt(cellSpecs[0].substring(0, indexOfElementNameStart));
                String elementName = cellSpecs[0].substring(indexOfElementNameStart);
                SpeciesRecordInterface species = NuclidesFactory.retrieveSpecies(elementName, massNumber);
                analysisMethod.addSpeciesToSpeciesList(species);
            }

        }


        return analysisMethod;
    }

    public static void TEST() throws JAXBException {

        Path phoenixAnalysisMethodDataFilePath = Paths.get("Sm147to150_S6_v2.TIMSAM");//"Pb 4-5-6-7-8 Daly 10-5-5-5-2 sec.TIMSAM.xml");

        JAXBContext jaxbContext = JAXBContext.newInstance(PhoenixAnalysisMethod.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        PhoenixAnalysisMethod phoenixAnalysisMethod = (PhoenixAnalysisMethod) jaxbUnmarshaller.unmarshal(phoenixAnalysisMethodDataFilePath.toFile());

        AnalysisMethod am = AnalysisMethod.createAnalysisMethodFromPhoenixAnalysisMethod(phoenixAnalysisMethod);
        System.out.println(phoenixAnalysisMethod.getHEADER().getFilename());
    }
    @Override
    public boolean equals(Object otherObject) {
        boolean retVal = true;
        if (otherObject != this) {
            if (otherObject instanceof AnalysisMethod otherAnalysisMethod) {
                retVal = this.getMethodName().compareToIgnoreCase(otherAnalysisMethod.getMethodName()) == 0;
            } else {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (methodName == null ? 0 : methodName.hashCode());
        hash = 31 * hash + (massSpectrometer == null ? 0 : massSpectrometer.hashCode());
        return hash;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public MassSpectrometerModel getMassSpectrometer() {
        return massSpectrometer;
    }

    public void setMassSpectrometer(MassSpectrometerModel massSpectrometer) {
        this.massSpectrometer = massSpectrometer;
    }

    public List<SpeciesRecordInterface> getSpeciesList() {
        return speciesList;
    }

    public void setSpeciesList(List<SpeciesRecordInterface> speciesList) {
        this.speciesList = speciesList;
    }

    public void addSpeciesToSpeciesList(SpeciesRecordInterface species){
        if (speciesList == null){
            speciesList = new ArrayList<>();
        }
        if (!speciesList.contains(species)) {
            speciesList.add(species);
        }
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

}