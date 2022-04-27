package org.cirdles.tripoli.sessions;

import org.cirdles.tripoli.sessions.analysis.Analysis;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethod;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionTest {

    Path testFilePath = (new File("TestSession.tripoli")).toPath();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void initializeDefaultSession() throws TripoliException {
        Session testSession = Session.initializeDefaultSession();
        testSession.getMapOfAnalyses().put("testAnalysis", Analysis.initializeAnalysis("testAnalysis", analysisMethodsBuiltinMap.get("BurdickBlSyntheticData")));
        testSession.getMapOfAnalyses().put("testAnalysis2", Analysis.initializeAnalysis("testAnalysis", analysisMethodsBuiltinMap.get("BurdickBlSyntheticData")));

//        testSession.getMapOfAnalyses().get("testAnalysis").

        TripoliSerializer.serializeObjectToFile(testSession, String.valueOf(testFilePath.getFileName()));
        Session testSession2 = (Session) TripoliSerializer.getSerializedObjectFromFile(String.valueOf(testFilePath.getFileName()), true);

        assertEquals(testSession2.getSessionName(), testSession.getSessionName());
        assertEquals(testSession2.getMapOfAnalyses().keySet(), testSession.getMapOfAnalyses().keySet());
        AnalysisMethod a = testSession.getMapOfAnalyses().entrySet().stream().findFirst().get().getValue().getMethod();
        AnalysisMethod b = testSession2.getMapOfAnalyses().entrySet().stream().findFirst().get().getValue().getMethod();
        assertEquals(a, b);
    }
}