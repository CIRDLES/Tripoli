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

package org.cirdles.tripoli.sessions.analysis.outputs.etRedux;

import org.cirdles.tripoli.constants.TripoliConstants;
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
        ETReduxFraction etReduxFraction = ETReduxFraction.buildExportFraction("Sample1", "Fraction1", TripoliConstants.ETReduxExportTypeEnum.U, 1.025);
        etReduxFraction.serializeXMLObject(fileNameForXML);
        ETReduxFraction etReduxFraction2 = (ETReduxFraction) etReduxFraction.readXMLObject(fileNameForXML, false);
        assertEquals(etReduxFraction, etReduxFraction2);

        etReduxFraction = ETReduxFraction.buildExportFraction("Sample1", "Fraction1", TripoliConstants.ETReduxExportTypeEnum.Pb, 1.025);
        etReduxFraction.serializeXMLObject(fileNameForXML);
        etReduxFraction2 = (ETReduxFraction) etReduxFraction.readXMLObject(fileNameForXML, false);
        assertEquals(etReduxFraction, etReduxFraction2);
    }
}