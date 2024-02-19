package org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.ogtripoli;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.ogtripoli.IsotopXPhoeniX_ImportedTIMSDPDataFile.initializeIsotopXPhoeniX_ImportedTIMSDPDataFile;

class IsotopXPhoeniX_ImportedTIMSDPDataFileTest {

    ResourceExtractor RESOURCE_EXTRACTOR;

    @BeforeEach
    void setUp() {
        RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void readDataFileExample() throws IOException {
        Path timsdpFilePath = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/dataSourceProcessors/dataSources/ogTripoli/isotopxPhoenix/timsDP/SmKU1A-A2-427.TIMSDP").toPath();
        IsotopXPhoeniX_ImportedTIMSDPDataFile isotopXPhoeniX_ImportedTIMSDPDataFile = initializeIsotopXPhoeniX_ImportedTIMSDPDataFile(timsdpFilePath);

        isotopXPhoeniX_ImportedTIMSDPDataFile.readDataFile();
        assert (isotopXPhoeniX_ImportedTIMSDPDataFile.testFileValidity());


    }
}