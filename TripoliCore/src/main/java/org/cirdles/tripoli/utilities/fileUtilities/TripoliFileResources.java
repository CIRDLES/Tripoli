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

package org.cirdles.tripoli.utilities.fileUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.cirdles.tripoli.Tripoli.TRIPOLI_RESOURCE_EXTRACTOR;
import static org.cirdles.tripoli.TripoliConstants.*;
import static org.cirdles.tripoli.utilities.fileUtilities.GithubFileExtractor.extractGithubFile;

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

        System.out.println("Tripoli Resources loaded");
    }

    /**
     * Provides a clean copy of resource files every time Tripoli runs
     */
    public static void retrieveResourceFiles(File resourceTargetFolder, String resourceFolderName)
            throws IOException {
        Path listOfResourceFiles = TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath(resourceFolderName + File.separator + "listOfResourceFiles.txt");
        if (resourceTargetFolder.exists()) {
            FileUtilities.recursiveDelete(resourceTargetFolder.toPath());
        }
        if (resourceTargetFolder.mkdir()) {
            if (listOfResourceFiles != null) {
                List<String> fileNames = Files.readAllLines(listOfResourceFiles, ISO_8859_1);
                for (int i = 0; i < fileNames.size(); i++) {
                    if (fileNames.get(i).trim().length() > 0) {
                        if (fileNames.get(i).startsWith("https")) {
                            int fileNameIndex = fileNames.get(i).lastIndexOf("/");
                            String fileName = fileNames.get(i).substring(fileNameIndex + 1);
                            try {
                                extractGithubFile(
                                        fileNames.get(i).trim(),
                                        resourceTargetFolder + File.separator + fileName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            File resourceFileName = TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsFile(resourceFolderName + File.separator + fileNames.get(i));
                            File resourceLocalFileName = new File(resourceTargetFolder.getCanonicalPath() + File.separator + fileNames.get(i));
                            if (resourceFileName != null) {
                                try {
                                    resourceFileName.renameTo(resourceLocalFileName);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}