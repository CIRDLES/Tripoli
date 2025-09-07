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
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.expressions.userFunctions.UserFunction;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.dataLiteOne.initializers.AllBlockInitForDataLiteOne;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Compares clipboard output for MassSpec data against known formatting
 */

public class OutputTest {

    AnalysisInterface analysis;
    Path outputPath;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(outputPath);
    }

    public void initializeAnalysis(Path dataFilePathPath) throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(dataFilePathPath));
        analysis.getUserFunctions().sort(null);
        try {
            AllBlockInitForDataLiteOne.initBlockModels(analysis);
        }
        catch (ArrayIndexOutOfBoundsException ignored) {} // Throws ArrayIndexOutOfBoundsException when dataFile's result is empty

    }

    public List<Path> generateListOfPaths(String directoryString) throws URISyntaxException, IOException {

        File directoryFile = new File(Objects.requireNonNull(Tripoli.class.getResource(directoryString)).toURI());
        List<Path> filePathsList;
        try (Stream<Path> pathStream = Files.walk(directoryFile.toPath())) {
            filePathsList = pathStream.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith("New Session-"))
                    .toList();
        }
        return filePathsList;
    }

    @Test
    public void massSpecOutputTest() throws URISyntaxException, IOException, JAXBException, TripoliException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String dataDirectoryString = "/org/cirdles/tripoli/core/reporting/dataFiles/";
        String oracleDirectoryString = "/org/cirdles/tripoli/core/reporting/shortReports/";

        List<Path> dataFilePaths = generateListOfPaths(dataDirectoryString);
        List<Path> oracleFilePaths = generateListOfPaths(oracleDirectoryString);

        String outputDirectory = String.valueOf(oracleFilePaths.get(0));
        outputDirectory = outputDirectory.replaceAll("reporting.shortReports.*", "");
        outputPath = Paths.get(outputDirectory, "reporting/shortReports/output.txt");

        boolean mismatchFound = false;

        for (Path path : dataFilePaths) {
            int index = dataFilePaths.indexOf(path);
            initializeAnalysis(path); // TODO: Fix ArrayIndexOutOfBoundsException

            for (UserFunction uf : analysis.getUserFunctions()) {
                if (uf.isTreatAsCustomExpression()) {
                    uf.setDisplayed(false);
                }
            }

            String clipBoardString = analysis.prepareFractionForClipboardExport();
            Files.write(outputPath, clipBoardString.getBytes());

            long byteIndex = Files.mismatch(outputPath, oracleFilePaths.get(index));
            if (byteIndex != -1L) {
                System.out.println("Mismatch found on file: " + path.toString().split("dataFiles")[1] + " on position " + byteIndex);
                mismatchFound = true;
            }
        }

        assertFalse(mismatchFound);

    }

}
