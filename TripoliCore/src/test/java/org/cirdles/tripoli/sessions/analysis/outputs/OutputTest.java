package org.cirdles.tripoli.sessions.analysis.outputs;

import jakarta.annotation.Resource;
import jakarta.xml.bind.JAXBException;
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class OutputTest {

    AnalysisInterface analysis;
    Path outputPath;

    @BeforeEach
    void setUp(){

    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(outputPath);
    }

    public void initializeAnalysis(Path dataFilePathPath) throws JAXBException, TripoliException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(dataFilePathPath));
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        analysis.getUserFunctions().sort(null);

    }
    public List<Path> generateListOfPaths(String directoryString, Class directoryClass) throws URISyntaxException, IOException {

        File directoryFile = new File(Objects.requireNonNull(Tripoli.class.getResource(directoryString)).toURI());
        List<Path> filePathsList;
        try (Stream<Path> pathStream = Files.walk(directoryFile.toPath())) {
            filePathsList = pathStream.filter(Files::isRegularFile)
                    .toList();
        }
        return filePathsList;
    }

    @Test
    public void massSpecOutputTest() throws URISyntaxException, IOException {
        String dataDirectoryString = "/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/";
        String oracleDirectoryString = "/org/cirdles/tripoli/core/outputs/";

        List<Path> dataFilePaths = generateListOfPaths(dataDirectoryString, Tripoli.class);
        List<Path> oracleFilePaths = generateListOfPaths(oracleDirectoryString, OutputTest.class);

        String outputDirectory = String.valueOf(oracleFilePaths.get(0));
        outputDirectory = outputDirectory.replaceAll("outputs.*", "");
        outputPath = Paths.get(outputDirectory, "outputs/output.txt");

        assertAll(
                () -> {
                    // Compare Files with Default UserFunctions
                    for (Path path : dataFilePaths) {
                        int index = dataFilePaths.indexOf(path);
                        initializeAnalysis(path);

                        String clipBoardString = analysis.prepareFractionForClipboardExport();
                        Files.write(outputPath, clipBoardString.getBytes());

                        assertEquals(-1L, Files.mismatch(outputPath, oracleFilePaths.get(index)));
                    }
                }
        );

    }

}
