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

package org.cirdles.tripoli.elements;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author James F. Bowring
 */
public final class ElementsFactory {

    public static List<ElementRecord> periodicTableElementsList = new ArrayList<>();

    static {
        final ResourceExtractor RESOURCE_EXTRACTOR
                = new ResourceExtractor(Tripoli.class);
        Path periodicTableData;
        List<String> contentsByLine = new ArrayList<>();
        try {
            periodicTableData = RESOURCE_EXTRACTOR
                    .extractResourceAsFile("/org/cirdles/tripoli/elements/PeriodicTableOfElements.csv").toPath();
            contentsByLine.addAll(Files.readAllLines(periodicTableData, Charset.defaultCharset()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // remove header
        contentsByLine.remove(0);
        for (String line : contentsByLine) {
            String[] lineContents = line.split(",");
            String elementSymbol = lineContents[2].trim();
            String elementName = lineContents[1].trim();
            int atomicNumber = Integer.parseInt(lineContents[0]);
            double atomicMass = Double.parseDouble(lineContents[3]);
            int tripoliRow = Integer.parseInt(lineContents[4]);
            int tripoliCol = Integer.parseInt(lineContents[5]);

            ElementRecord element = new ElementRecord(
                    elementSymbol,
                    elementName,
                    atomicNumber,
                    atomicMass,
                    tripoliRow,
                    tripoliCol
            );
            periodicTableElementsList.add(element);
        }

    }
}