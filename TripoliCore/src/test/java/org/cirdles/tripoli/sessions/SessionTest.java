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

package org.cirdles.tripoli.sessions;

import jakarta.xml.bind.JAXBException;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
    void initializeDefaultSessionTest() throws TripoliException, JAXBException {
        Session testSession = Session.initializeDefaultSession();

//        testSession.getMapOfAnalyses().put("testAnalysis",
//                AnalysisInterface.initializeAnalysis("testAnalysis", analysisMethodsBuiltinMap.get(AnalysisMethodBuiltinFactory.BURDICK_BL_SYNTHETIC_DATA), new Sample("test sample 1")));
//        testSession.getMapOfAnalyses().put("testAnalysis2",
//                AnalysisInterface.initializeAnalysis("testAnalysis", analysisMethodsBuiltinMap.get(AnalysisMethodBuiltinFactory.BURDICK_BL_SYNTHETIC_DATA), new Sample("test sample 2")));
//
//        TripoliSerializer.serializeObjectToFile(testSession, String.valueOf(testFilePath.getFileName()));
//        Session testSession2 = (Session) TripoliSerializer.getSerializedObjectFromFile(String.valueOf(testFilePath.getFileName()), true);
//
//        assertEquals(testSession2.getSessionName(), testSession.getSessionName());
//        assertEquals(testSession2.getMapOfAnalyses().keySet(), testSession.getMapOfAnalyses().keySet());
//        AnalysisMethod a = testSession.getMapOfAnalyses().entrySet().stream().findFirst().get().getValue().getMethod();
//        AnalysisMethod b = testSession2.getMapOfAnalyses().entrySet().stream().findFirst().get().getValue().getMethod();
//        assertEquals(a, b);
//        assertTrue(true);
    }
}