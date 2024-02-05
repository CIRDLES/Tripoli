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

package org.cirdles.tripoli.species.nuclides;

import org.cirdles.tripoli.expressions.species.nuclides.NuclideRecord;
import org.cirdles.tripoli.expressions.species.nuclides.NuclidesFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NuclideRecordFactoryTest {

    @BeforeEach
    void setUp() {
        System.err.println("Testing NuclidesFactory  " + NuclidesFactory.nuclidesListByElementMap.size());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void validateSpeciesChart() {
        int actualValue = ((NuclideRecord) ((List<?>) NuclidesFactory.nuclidesListByElementMap.get("Nd")).get(0)).neutronsN();
        int expectedValue = 65;

        assertEquals(actualValue, expectedValue);

    }
}