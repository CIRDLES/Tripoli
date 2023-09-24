package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataModels.mcmc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleBlockModelUpdaterTest {

    @BeforeEach
    void setUp() {
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
}