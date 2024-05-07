package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeasuredRatioModelTest {

    MeasuredRatioModel measuredRatioModel = new MeasuredRatioModel("TestRatioModel", 12345.6789, 98765.4321, true, false);
    String fileNameForXML = "testMeasuredRatioModel.xml";

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(new File(fileNameForXML).toPath());
    }

    @Test
    void validateSerializationXML() {
        measuredRatioModel.serializeXMLObject(fileNameForXML);
        MeasuredRatioModel measuredRatioModel2 = (MeasuredRatioModel) measuredRatioModel.readXMLObject(fileNameForXML, false);
        assertEquals(measuredRatioModel, measuredRatioModel2);
    }
}