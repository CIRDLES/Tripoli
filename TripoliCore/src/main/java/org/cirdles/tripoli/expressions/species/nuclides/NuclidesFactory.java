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

package org.cirdles.tripoli.expressions.species.nuclides;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;
import org.cirdles.tripoli.expressions.species.SpeciesRecordInterface;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author James F. Bowring
 */
public final class NuclidesFactory implements Serializable {

    public static Map<String, List<SpeciesRecordInterface>> nuclidesListByElementMap = new TreeMap<>();

    static {
        ResourceExtractor RESOURCE_EXTRACTOR
                = new ResourceExtractor(Tripoli.class);
        Path nuclidesChartData;
        List<String> contentsByLine = new ArrayList<>();
        try {
            nuclidesChartData = RESOURCE_EXTRACTOR
                    .extractResourceAsFile("/org/cirdles/tripoli/species/nuclides/NuclidesChartData.csv").toPath();
            contentsByLine.addAll(Files.readAllLines(nuclidesChartData, Charset.defaultCharset()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // remove header
        contentsByLine.remove(0);

        for (String line : contentsByLine) {
            String[] lineContents = line.split(",");
            String elementSymbol = lineContents[2].trim();
            int protonsZ = Integer.parseInt(lineContents[0]);
            int neutronsN = Integer.parseInt(lineContents[1]);

            double atomicMass;
            try {
                atomicMass = Double.parseDouble(lineContents[5]);
            } catch (NumberFormatException e) {
                atomicMass = 0.0;
            }

            double halfLifeAnnum;
            try {
                halfLifeAnnum = Double.parseDouble(lineContents[4]);
            } catch (NumberFormatException e) {
                if (lineContents[4].toUpperCase(Locale.ROOT).contains("STABLE")) {
                    halfLifeAnnum = -1.0;
                } else {
                    halfLifeAnnum = 0.0;
                }
            }

            double naturalAbundancePercent;
            try {
                naturalAbundancePercent = Double.parseDouble(lineContents[3]);
            } catch (NumberFormatException e) {
                naturalAbundancePercent = 0.0;
            }

            SpeciesRecordInterface nuclide = new NuclideRecord(
                    elementSymbol,
                    protonsZ,
                    neutronsN,
                    atomicMass,
                    halfLifeAnnum,
                    naturalAbundancePercent
            );

            if (null != nuclidesListByElementMap.get(elementSymbol)) {
                nuclidesListByElementMap.get(elementSymbol).add(nuclide);
            } else {
                List<SpeciesRecordInterface> speciesListForElement = new ArrayList<>();
                speciesListForElement.add(nuclide);
                nuclidesListByElementMap.put(elementSymbol, speciesListForElement);
            }
        }
    }

    public static SpeciesRecordInterface retrieveSpecies(String elementName, int massNumber) {
        List<SpeciesRecordInterface> nuclides = nuclidesListByElementMap.get(elementName);
        List<SpeciesRecordInterface> targetNuclideList = nuclides
                .stream()
                .filter(nuclide -> ((nuclide instanceof NuclideRecord) && nuclide.getMassNumber() == massNumber)).toList();
        return targetNuclideList.get(0);
    }
}