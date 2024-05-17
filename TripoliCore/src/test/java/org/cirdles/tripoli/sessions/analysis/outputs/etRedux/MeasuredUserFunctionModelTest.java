package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeasuredUserFunctionModelTest {

    MeasuredUserFunctionModel measuredUserFunctionModel = new MeasuredUserFunctionModel("TestRatioModel", 12345.6789, 98765.4321, true, false);
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
        measuredUserFunctionModel.serializeXMLObject(fileNameForXML);
        MeasuredUserFunctionModel measuredUserFunctionModel2 = (MeasuredUserFunctionModel) measuredUserFunctionModel.readXMLObject(fileNameForXML, false);
        assertEquals(measuredUserFunctionModel, measuredUserFunctionModel2);
    }
}