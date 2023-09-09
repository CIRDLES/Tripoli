package org.cirdles.tripoli.utilities.stateUtilities;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static org.junit.jupiter.api.Assertions.*;

class TripoliSerializerTest {

    private String fileName;

    @BeforeEach
    void setUp() {
        fileName = "testSerializationFile.ser";
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

        String testObject = "Hello, Serialization!";

        try {
            TripoliSerializer.serializeObjectToFile(testObject, fileName);

            File serializedFile = new File(fileName);
            assertTrue(serializedFile.exists());

            FileInputStream fileInputStream = new FileInputStream(serializedFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object deserializedObject = objectInputStream.readObject();
            objectInputStream.close();

            // Check that the deserialized object matches the original test object
            assertEquals(testObject, deserializedObject);

        } catch (IOException ex) {
            fail("Exception occurred during test: " + ex.getMessage());
        }
    }
}