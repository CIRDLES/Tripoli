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

package org.cirdles.tripoli.dataProcessors.dataSources.synthetic;

import jama.Matrix;
import jama.MatrixIO;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataOutputModels.MassSpecOutputDataRecord;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.DataSourceProcessor_OPPhoenix;
import org.cirdles.tripoli.sessions.analysis.analysisMethods.AnalysisMethodBuiltinFactory;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DataSourceProcessorOPPhoenixTest {

    private static final ResourceExtractor RESOURCE_EXTRACTOR
            = new ResourceExtractor(Tripoli.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Order(1)
    void prepareInputDataModelFromFileTwoIsotopes() throws IOException {
        System.err.println("Testing Synthetic Data 2 isotopes.");
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("BurdickBlSyntheticData"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFile);

        double[] testArray = new double[]{1, 2, 3, 4, 5};
        Matrix test = new Matrix(testArray, testArray.length);
        MatrixIO.print(2, 2, test);

        assert (massSpecOutputDataRecord.rawDataColumn().getRowDimension() == 3600);
    }

    @Test
    @Order(2)
    void prepareInputDataModelFromFileFiveIsotopes() throws IOException {
        System.err.println("Testing Synthetic Data 5 isotopes.");
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_01R.txt").toPath();
        DataSourceProcessor_OPPhoenix dataSourceProcessorOPPhoenix
                = DataSourceProcessor_OPPhoenix.initializeWithAnalysisMethod(AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get("KU_204_5_6_7_8_Daly_AllFaradayPb"));
        MassSpecOutputDataRecord massSpecOutputDataRecord = dataSourceProcessorOPPhoenix.prepareInputDataModelFromFile(dataFile);

        assert (massSpecOutputDataRecord.rawDataColumn().getRowDimension() == 162000);
        assertEquals(-531920.15291, massSpecOutputDataRecord.rawDataColumn().get(26669, 0));
    }
}