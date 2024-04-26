package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ETReduxFractionTest {
    String fileNameForXML = "testETReduxFraction.xml";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateSerializationXML() {
        ETReduxFraction etReduxFraction = ETReduxFraction.buildExportFraction("Sample1", "Fraction1", "U", 1.025);
        etReduxFraction.serializeXMLObject(fileNameForXML);
        ETReduxFraction etReduxFraction2 = (ETReduxFraction) etReduxFraction.readXMLObject(fileNameForXML, false);
        assertEquals(etReduxFraction, etReduxFraction2);

        etReduxFraction = ETReduxFraction.buildExportFraction("Sample1", "Fraction1", "Pb", 1.025);
        etReduxFraction.serializeXMLObject(fileNameForXML);
        etReduxFraction2 = (ETReduxFraction) etReduxFraction.readXMLObject(fileNameForXML, false);
        assertEquals(etReduxFraction, etReduxFraction2);
    }
}