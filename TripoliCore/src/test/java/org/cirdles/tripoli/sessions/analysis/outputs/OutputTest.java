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

package org.cirdles.tripoli.sessions.analysis.outputs;

import jakarta.xml.bind.JAXBException;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OutputTest {

    AnalysisInterface analysis;
    Path outputPath;
    Path oraclePath;

    @BeforeEach
    void setUp() throws TripoliException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
    }
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(outputPath);
        Files.deleteIfExists(oraclePath);
        analysis.resetAnalysis();
    }

    public void intializeAnalysis(String oracleFilename, String dataFilePathString) throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String dataFilePrefix = "/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/";
        String oraclePrefix = "/org/cirdles/tripoli/core/outputs/";

        ResourceExtractor testResourceExtractor = new ResourceExtractor(OutputTest.class);
        ResourceExtractor coreResourceExtractor = new ResourceExtractor(Tripoli.class);

        oraclePath = testResourceExtractor.extractResourceAsPath(oraclePrefix + oracleFilename);
        File dataFile = coreResourceExtractor.extractResourceAsFile(dataFilePrefix + dataFilePathString);

        String dataFileName = dataFilePathString.split("/")[dataFilePathString.split("/").length-1];
        File newDataFileName = new File(dataFile.getParent() + File.separator + dataFileName);
        dataFile.renameTo(newDataFileName);
        dataFile = newDataFileName;
        outputPath = oraclePath.getParent().resolve("output.txt");

        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(dataFile.toPath()));
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        analysis.getUserFunctions().sort(null);
        Files.deleteIfExists(dataFile.toPath());
    }
    @Test
    public void boiseStateTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("B998_F11_13223M02_iz1_Pb1-14973.txt",
                "isotopxPhoenixTIMS/boiseState/B998_F11_13223M02_iz1_Pb1-14973.xls");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void ionVantageTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("NBS981-500_20191208-5563.txt",
                "isotopxPhoenixTIMS/kU_IGL/ionVantage/NBS981-500_20191208-5563.xls");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void isolinxV1test() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        intializeAnalysis("NBS981_210325b-392.txt",
                "isotopxPhoenixTIMS/kU_IGL/isolinxVersion1/NBS981_210325b-392.TIMSDP");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));

    }
    @Test
    public void isolinxV2test() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        intializeAnalysis("NBS981_230024a-145.txt",
                "isotopxPhoenixTIMS/kU_IGL/isolinxVersion2/NBS981_230024a-145.TIMSDP");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void purdueTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("WH205_z4_Pb-654.txt",
                "isotopxPhoenixTIMS/purdue/WH205_z4_Pb-654.TIMSDP");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void neptuneMCICPMSTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("MK-113z1.txt",
                "neptuneMCICPMS/MK-113z1.exp");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void nuTIMSTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("21AG-0815_2-8_Pb_(Individual_Answers_1719).txt",
                "nuTIMS/21AG-0815_2-8_Pb_(Individual_Answers_1719).txt");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void ethTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("15700_Nd_Gain_10_13_301115.txt",
                "tritonTIMS/ETH/15700_Nd_Gain_10_13_301115.exp");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }
    @Test
    public void ucDavisTest() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        intializeAnalysis("z11_1.txt",
                "tritonTIMS/UCDavis/z11_1.exp");

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(oraclePath, outputPath));
    }

}
