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

package org.cirdles.tripoli.nuclidesChart;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.Tripoli;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author James F. Bowring
 */
public final class SpeciesFactory implements Serializable {

    public static Map<Integer, List<Species>> speciesByProtonList = new LinkedHashMap<>();

    static {
        final ResourceExtractor RESOURCE_EXTRACTOR
                = new ResourceExtractor(Tripoli.class);
        Path nuclidesChartData = RESOURCE_EXTRACTOR
                .extractResourceAsFile("/org/cirdles/tripoli/nuclidesChart/NuclidesChartData.csv").toPath();
        List<String> contentsByLine = new ArrayList<>();
        try {
            contentsByLine.addAll(Files.readAllLines(nuclidesChartData, Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // remove header
        contentsByLine.remove(0);
        int saveProtonsZ = -1;
        int protonsZ;
        for (String line : contentsByLine) {
            String[] lineContents = line.split(",");
            String elementSymbol = lineContents[2].trim();
            protonsZ = Integer.parseInt(lineContents[0]);
            int neutronsN = Integer.parseInt(lineContents[1]);

            double atomicMass;
            try {
                atomicMass = Double.parseDouble(lineContents[5]);
            } catch (NumberFormatException e) {
                atomicMass = 0.;
            }

            double halfLifeAnnum;
            try {
                halfLifeAnnum = Double.parseDouble(lineContents[4]);
            } catch (NumberFormatException e) {
                if (lineContents[4].toUpperCase(Locale.ROOT).contains("STABLE")){
                    halfLifeAnnum = -1.;
                } else {
                    halfLifeAnnum = 0.;
                }
            }

            double naturalAbundancePercent;
            try {
                naturalAbundancePercent = Double.parseDouble(lineContents[3]);
            } catch (NumberFormatException e) {
                naturalAbundancePercent = 0.;
            }

            Species species = new Species(
                    elementSymbol,
                    protonsZ,
                    neutronsN,
                    atomicMass,
                    halfLifeAnnum,
                    naturalAbundancePercent
            );

            if (protonsZ == saveProtonsZ){
                speciesByProtonList.get(protonsZ).add(species);
            } else {
                List<Species> speciesListForProtonCount = new ArrayList<>();
                speciesListForProtonCount.add(species);
                speciesByProtonList.put(protonsZ, speciesListForProtonCount);
                saveProtonsZ = protonsZ;
            }
        }

    }
}