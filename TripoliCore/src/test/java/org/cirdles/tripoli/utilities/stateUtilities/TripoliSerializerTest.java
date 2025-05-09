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

package org.cirdles.tripoli.utilities.stateUtilities;

import jakarta.xml.bind.JAXBException;
import org.cirdles.tripoli.sessions.Session;
import org.cirdles.tripoli.sessions.SessionBuiltinFactory;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;

import static org.cirdles.tripoli.sessions.SessionBuiltinFactory.TRIPOLI_DEMONSTRATION_SESSION;
import static org.junit.jupiter.api.Assertions.*;

class TripoliSerializerTest {

    private String fileName;
    private Session testSession;

    @BeforeEach
    void setUp() {
        fileName = "testSerializationFile.ser";

        Map<String, Session> sessionsBuiltinMap = new TreeMap<>();

        testSession = SessionBuiltinFactory.sessionsBuiltinMap.get(TRIPOLI_DEMONSTRATION_SESSION);
        testSession.setSessionName("Test 2023");
        testSession.setAnalystName("My Nguyen");
        sessionsBuiltinMap.put(testSession.getSessionName(), testSession);
    }

    @AfterEach
    void tearDown() {
        File serializedFile = new File(fileName);
        if (serializedFile.exists() && !serializedFile.delete()) {
            System.err.println("Failed to delete test file: " + fileName);
        }
    }

    @Test
    void serializeObjectToFile() throws TripoliException, ClassNotFoundException {

        String testObject = "Testing Serialization...";

        try {
            TripoliSerializer.serializeObjectToFile(testObject, fileName);

            File serializedFile = new File(fileName);
            assertTrue(serializedFile.exists());

            FileInputStream fileInputStream = new FileInputStream(serializedFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object deserializedWithJava = objectInputStream.readObject();
            objectInputStream.close();

            Object deserializedWithTripoli = TripoliSerializer.getSerializedObjectFromFile(fileName, true);

            // Compare Serialized and Deserialized Object
            assertEquals(testObject, deserializedWithJava);
            assertEquals(testObject, deserializedWithTripoli);

        } catch (IOException ex) {
            fail("Exception occurred during test: " + ex.getMessage());
        }
    }

    @Test
    void getSerializedObjectFromFile() throws TripoliException {

        TripoliSerializer.serializeObjectToFile(testSession, fileName);

        @Nullable Session deserializedSession;
        try {
            deserializedSession = (Session) TripoliSerializer.getSerializedObjectFromFile(fileName, true);
        } catch (TripoliException ex) {
            fail("Exception occurred during deserialization: " + ex.getMessage());
            return;
        }
        // Compare Serialized and Deserialized Object
        assertEquals(testSession, deserializedSession);
    }

    @Test
    void testSessionEquality() throws JAXBException {
        Session session1 = Session.initializeDefaultSession();
        Session session2 = Session.initializeDefaultSession();

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }
}