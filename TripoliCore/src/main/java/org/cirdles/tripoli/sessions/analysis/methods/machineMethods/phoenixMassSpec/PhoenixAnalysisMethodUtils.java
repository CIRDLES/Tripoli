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

package org.cirdles.tripoli.sessions.analysis.methods.machineMethods.phoenixMassSpec;

import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethod;
import org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;

import static org.cirdles.tripoli.sessions.analysis.methods.AnalysisMethodBuiltinFactory.BURDICK_BL_SYNTHETIC_DATA;

/**
 * @author James F. Bowring
 */
public enum PhoenixAnalysisMethodUtils {
    ;

    public static void test() throws IOException {
        AnalysisMethod analysisMethod = AnalysisMethodBuiltinFactory.analysisMethodsBuiltinMap.get(BURDICK_BL_SYNTHETIC_DATA);
        try {
            writeRawDataFileAsXML(analysisMethod, "TESTY.xml");
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeRawDataFileAsXML(AnalysisMethod analysisMethod, String fileName)
            throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(AnalysisMethod.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        File outputAnalysisMethodFileFile = new File(fileName);
        jaxbMarshaller.marshal(analysisMethod, outputAnalysisMethodFileFile);

//        // jan 2021 per issue #574, remove leading white space and add a space after last attribute value plus add 2 comment lines
//        // to make files ingestible by Squid25
//        Path originalPrawnOutput = outputPrawnFile.toPath();
//        List<String> lines = Files.readAllLines(originalPrawnOutput, Charset.defaultCharset());
//        for (int i = 0; i < lines.size(); i++) {
//            lines.set(i, lines.get(i).replaceAll("\"/>", "\" />").trim());
//        }
//        lines.add(1, "<!-- SHRIMP SW PRAWN Data File -->");
//        lines.add(2, "<!-- SQUID3-generated PRAWN Data File copy -->");
//
//        File updatedPrawnFile = writeTextFileFromListOfStringsWithUnixLineEnd(lines, "updatedPrawnFile", ".xml");
//        Path updatedPrawnOutput = updatedPrawnFile.toPath();
//        Files.copy(updatedPrawnOutput, originalPrawnOutput, StandardCopyOption.REPLACE_EXISTING);
    }


}