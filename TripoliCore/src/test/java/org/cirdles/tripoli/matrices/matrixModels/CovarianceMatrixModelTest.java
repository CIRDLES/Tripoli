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

package org.cirdles.tripoli.matrices.matrixModels;

import org.cirdles.tripoli.utilities.exceptions.TripoliException;
import org.cirdles.tripoli.utilities.stateUtilities.TripoliSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CovarianceMatrixModelTest {

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(new File("myMatrix.ser").getPath()));
    }

    @Test
    void initializeMatrixModelWithVariances() {
        AbstractMatrixModel myMatrix = new CovarianceMatrixModel();

        String[] rowNames = new String[]{"first", "second", "third", "fourth", "fifth"};
        myMatrix.setRows(rowNames);
        myMatrix.setCols(myMatrix.getRows());

        ConcurrentMap<String, BigDecimal> varianceTerms = new ConcurrentHashMap<>();
        varianceTerms.put("third", new BigDecimal(1));
        varianceTerms.put("fourth", new BigDecimal(2));
        varianceTerms.put("fifth", new BigDecimal(3));

        Map<String, BigDecimal> coVariances = new HashMap<>();
        coVariances.put("covThird__fourth", new BigDecimal(9));

        if (((CovarianceMatrixModel) myMatrix).initializeMatrixModelWithVariances(varianceTerms)) {
            ((CovarianceMatrixModel) myMatrix).initializeCoVariances(coVariances);
            System.err.println(myMatrix.ToStringWithLabels());
        }

        try {
            TripoliSerializer.serializeObjectToFile(myMatrix, "myMatrix.ser");
            AbstractMatrixModel myMatrix2 = (AbstractMatrixModel) TripoliSerializer.getSerializedObjectFromFile("myMatrix.ser", true);
            assertArrayEquals(myMatrix2.getMatrix().getArray(), myMatrix.getMatrix().getArray());

        } catch (TripoliException e) {
            e.printStackTrace();
        }
    }
}