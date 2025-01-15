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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.assertEquals;


public class OutputTest {

    AnalysisInterface analysis;
    Path outputPath;

    @BeforeEach
    void setUp() throws TripoliException {
        analysis = AnalysisInterface.initializeNewAnalysis(0);
    }
    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(outputPath)) {
            Files.delete(outputPath);
        }
    }

    @Test
    public void nbs981test() throws TripoliException, JAXBException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        ResourceExtractor testResourceExtractor = new ResourceExtractor(OutputTest.class);
        ResourceExtractor coreResourceExtractor = new ResourceExtractor(Tripoli.class);
        Path expectedPath = testResourceExtractor.extractResourceAsPath("/org/cirdles/tripoli/core/NBS981_210325b-392_output.txt");
        outputPath = expectedPath.getParent().resolve("output.txt");
        Path timsdpPath = coreResourceExtractor.extractResourceAsPath("/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/isotopxPhoenix/timsDP/NBS981_210325b-392.TIMSDP");

        analysis.setAnalysisName(analysis.extractMassSpecDataFromPath(timsdpPath));
        AllBlockInitForDataLiteOne.initBlockModels(analysis);
        analysis.getUserFunctions().sort(null);

        String clipBoardString = analysis.prepareFractionForClipboardExport();
        Files.write(outputPath, clipBoardString.getBytes());

        assertEquals(-1L, Files.mismatch(expectedPath, outputPath));


    }
}
