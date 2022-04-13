package org.cirdles.tripoli.dataProcessors.dataSources.synthetic;

import jama.Matrix;
import jama.MatrixIO;
import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.massSpectrometers.dataModels.DataSourceProcessorTypeA;
import org.cirdles.tripoli.massSpectrometers.dataModels.MassSpecDataModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;


class SyntheticDataSourceProcessorTypeATest {

    private static final ResourceExtractor RESOURCE_EXTRACTOR
            = new ResourceExtractor(Tripoli.class);

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void prepareInputDataModelFromFile() throws IOException {
        Path dataFile = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataProcessors/dataSources/synthetic/SyntheticDataset_05.txt").toPath();
        DataSourceProcessorTypeA syntheticDataSourceProcessorTypeA = new DataSourceProcessorTypeA();
        MassSpecDataModel massSpecDataModel = syntheticDataSourceProcessorTypeA.prepareInputDataModelFromFile(dataFile);

        double[] testArray = new double[]{1, 2, 3, 4, 5};
        Matrix test = new Matrix(testArray, testArray.length);
        MatrixIO.print(2, 2, test);
    }
}