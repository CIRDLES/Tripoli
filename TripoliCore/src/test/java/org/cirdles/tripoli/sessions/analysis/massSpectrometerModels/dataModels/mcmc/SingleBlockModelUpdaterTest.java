package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SingleBlockModelUpdaterTest {

    SingleBlockModelRecord singleBlockModelRecord_Test;
    double [] baseLineMeansArray = {-399826.3822187037, -299757.32392412674, -199690.25908550684, -99956.0760771712,
        -103.29832749570951, 100073.76977221946, 200116.9173358956, 299425.4414813997};
    double [] baseLineStdArray = {4982.67807983952, 4780.0623321199555, 5193.360350459159, 4907.063915023453,
        4714.765639738263, 5160.46695647297, 5046.694666480015, 4697.626440989729};
    double[] logRatios = {-6.105156257140191, -3.004719311598562, -1.0005880585294717, -1.0037282990171001E-4};
    int m = 2;
    int sizeOfModel = 24;
    double[][] dataCov = new double[sizeOfModel][sizeOfModel];
    double[] dataMean = new double[sizeOfModel];

    @BeforeEach
    void setUp() {

        // Create Sudo ModelRecord
        singleBlockModelRecord_Test = new SingleBlockModelRecord(
                1,
                8,
                10,
                5,
                null,
                baseLineMeansArray,
                baseLineStdArray,
                0.8000798414508663,
                null,
                logRatios,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void randomOperMS() {
        SingleBlockModelUpdater singleBlockModelUpdater = new SingleBlockModelUpdater();
        String oper = singleBlockModelUpdater.randomOperMS(true);

        assertTrue(singleBlockModelUpdater.getOperations().contains(oper));
    }

    @Test
    void testUpdateMeanCovMS2() {

        SingleBlockModelUpdater modelUpdater = new SingleBlockModelUpdater();

        SingleBlockModelUpdater.UpdatedCovariancesRecord result = modelUpdater.updateMeanCovMS2(
                singleBlockModelRecord_Test, dataCov, dataMean, m);

        //System.out.println(result.toString());
    }
}