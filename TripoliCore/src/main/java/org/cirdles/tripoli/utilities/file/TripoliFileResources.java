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

package org.cirdles.tripoli.utilities.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.cirdles.tripoli.Tripoli.TRIPOLI_RESOURCE_EXTRACTOR;
import static org.cirdles.tripoli.TripoliConstants.*;
import static org.cirdles.tripoli.utilities.file.GithubFileExtractor.extractGithubFile;

public class TripoliFileResources {
    public static void initLocalResources() throws IOException {
        if (TRIPOLI_RESOURCES_FOLDER.exists()) {
            FileUtilities.recursiveDelete(TRIPOLI_RESOURCES_FOLDER.toPath());
        }
        if (!TRIPOLI_RESOURCES_FOLDER.mkdir()) {
            throw new IOException();
        }

        if (PARAMETER_MODELS_FOLDER.exists()) {
            FileUtilities.recursiveDelete(PARAMETER_MODELS_FOLDER.toPath());
        }
        if (!PARAMETER_MODELS_FOLDER.mkdir()) {
            throw new IOException();
        }

        retrieveResourceFiles(SCHEMA_FOLDER, "schema");
        retrieveResourceFiles(TRACER_MODELS_FOLDER, "parameterModels/tracerModels");
        retrieveResourceFiles(PHYSCONST_MODELS_FOLDER, "parameterModels/physicalConstantsModels");
        retrieveResourceFiles(REFMAT_MODELS_FOLDER, "parameterModels/referenceMaterialsModels");

        retrieveResourceFiles(SYNTHETIC_DATA_FOLDER, "dataProcessors/dataSources/synthetic");
        retrieveResourceFiles(NUCLIDESCHART_DATA_FOLDER, "species/nuclides");
        retrieveResourceFiles(PERIODICTABLE_DATA_FOLDER, "elements");
        retrieveResourceFiles(PEAK_CENTRES_FOLDER, "dataProcessors/dataSources/peakShapes");

        System.out.println("Tripoli Resources loaded");
    }

    /**
     * Provides a clean copy of resource files every time Tripoli runs
     */
    // https://www.atlassian.com/blog/developer/2006/12/how_to_use_file_separator_when
    public static void retrieveResourceFiles(File resourceTargetFolder, String resourceFolderName)
            throws IOException {

        Path listOfResourceFiles = TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath(resourceFolderName + "/" + "listOfResourceFiles.txt");
        if (resourceTargetFolder.exists()) {
            FileUtilities.recursiveDelete(resourceTargetFolder.toPath());
        }
        if (resourceTargetFolder.mkdir() && (listOfResourceFiles != null)) {
            List<String> fileNames = Files.readAllLines(listOfResourceFiles, ISO_8859_1);
            for (String name : fileNames) {
                if (name.trim().length() > 0) {
                    if (name.startsWith("https")) {
                        int fileNameIndex = name.lastIndexOf("/");
                        String fileName = name.substring(fileNameIndex + 1);
                        try {
                            extractGithubFile(
                                    name.trim(),
                                    resourceTargetFolder + "/" + fileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        File resourceFileName = TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsFile(resourceFolderName + "/" + name);
                        File resourceLocalFileName = new File(resourceTargetFolder.getCanonicalPath() + "/" + name);
                        if (resourceFileName != null) {
                            boolean renameTo = resourceFileName.renameTo(resourceLocalFileName);
                            if (!renameTo) {
                                throw new IOException();
                            }
                        }
                    }
                }
            }
        }
    }
}