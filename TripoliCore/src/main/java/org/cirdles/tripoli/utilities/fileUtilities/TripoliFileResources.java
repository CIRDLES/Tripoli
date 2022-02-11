package org.cirdles.tripoli.utilities.fileUtilities;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;

import java.io.IOException;

import static org.cirdles.tripoli.TripoliConstants.NAME_OF_TRIPOLI_RESOURCES_FOLDER;
import static org.cirdles.tripoli.TripoliConstants.SCHEMA_FOLDER;

public class TripoliFileResources {
    static {

    }

    public static void init() throws IOException {
        try {
            NAME_OF_TRIPOLI_RESOURCES_FOLDER.mkdir();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadTripoliSchema() throws IOException {
        ResourceExtractor prawnFileResourceExtractor = new ResourceExtractor(Tripoli.class);
        if (SCHEMA_FOLDER.exists()) {
            FileUtilities.recursiveDelete(SCHEMA_FOLDER.toPath());
        }
        if (SCHEMA_FOLDER.mkdir()) {

        }
    }
}