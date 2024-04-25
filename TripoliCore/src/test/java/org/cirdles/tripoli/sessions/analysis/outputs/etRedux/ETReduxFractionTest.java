package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ETReduxFractionTest {

    String fileNameForXML = "testETReduxFraction.xml";
    MeasuredRatioModel[] measuredRatioModels = new MeasuredRatioModel[3];

    @BeforeEach
    void setUp() {
        measuredRatioModels[0] = new MeasuredRatioModel("TestRatioModelA3", 12345.6789, 98765.4321, true, false);
        measuredRatioModels[1] = new MeasuredRatioModel("TestRatioModelB3", -12345.6789, 98765.4321, false, true);
        measuredRatioModels[2] = new MeasuredRatioModel("TestRatioModelC3", 12345.6789, -98765.4321, false, false);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateSerializationXML() {
        ETReduxFraction etReduxFraction = new ETReduxFraction();//"Sample1", "Fraction1", "U", 1.025);
//        etReduxFraction.setMeasuredRatios(measuredRatioModels);
        etReduxFraction.serializeXMLObject(fileNameForXML);
        ETReduxFraction etReduxFraction2 = (ETReduxFraction) etReduxFraction.readXMLObject(fileNameForXML, false);
        assertEquals(etReduxFraction, etReduxFraction2);
    }
}