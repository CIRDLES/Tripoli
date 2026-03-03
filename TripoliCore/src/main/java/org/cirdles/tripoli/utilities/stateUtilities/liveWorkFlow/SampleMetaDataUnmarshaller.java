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

package org.cirdles.tripoli.utilities.stateUtilities.liveWorkFlow;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import jakarta.xml.bind.JAXBException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author James F. Bowring
 */
public class SampleMetaDataUnmarshaller {

    public static SampleMetaData unmarshall(String sampleMetaDataFilePath) throws JAXBException, FileNotFoundException {
        String networkSource = sampleMetaDataFilePath;
        String localDestination = "copiedSampleMetaDataFile.xml";
        Path sourcePath = Paths.get(networkSource);
        Path destinationPath = Paths.get(localDestination);
        try {
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("SampleMetaDataFile XML file copied successfully!");
        } catch (IOException e) {
            System.err.println("Failed to copy the XML file: " + e.getMessage());
            e.printStackTrace();
        }

        XStream xstream = new XStream();
        xstream.addPermission(AnyTypePermission.ANY);
        xstream.alias("SampleMetaData", SampleMetaData.class);
        xstream.alias("FractionMetaData", FractionMetaData.class);
        Reader fileReader = new FileReader(localDestination);
        return (SampleMetaData) xstream.fromXML(fileReader);
    }

    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        SampleMetaData sampleMetaData =
                unmarshall("/Users/bowring/Downloads/LiveWorkflowReproduction/SampleMetaData/23-PP-8.xml");
        System.out.println(sampleMetaData.getSampleName());
    }

}
