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

package org.cirdles.tripoli.valueModels;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValueModelTest {

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(new File("TestValueModel.ser").getPath()));
    }

    @Test
    void createFullNamedValueModel() {
        ValueModel vm = ValueModel.createFullNamedValueModel("Test", new BigDecimal("1.00000000000000000000000000022"), BigDecimal.ONE, BigDecimal.ONE);
        try {
            TripoliSerializer.serializeObjectToFile(vm, "TestValueModel.ser");
            ValueModel vm2 = (ValueModel) TripoliSerializer.getSerializedObjectFromFile("TestValueModel.ser", true);
            assertEquals(vm.name, vm2.name);
            assertEquals(vm.value, vm2.value);
            assertEquals(vm.oneSigma, vm2.oneSigma);
            assertEquals(vm.oneSigmaSys, vm2.oneSigmaSys);
        } catch (TripoliException e) {
            e.printStackTrace();
        }
    }
}