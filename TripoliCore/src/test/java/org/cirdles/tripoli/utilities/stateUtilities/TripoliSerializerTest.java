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
            Object deserializedObject = objectInputStream.readObject();
            objectInputStream.close();

            // Compare Serialized and Deserialized Object
            assertEquals(testObject, deserializedObject);

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

        // Override equals and hashCode in Session.java
        assertTrue(session1.equals(session2));
        assertEquals(session1.hashCode(), session2.hashCode());
    }
}