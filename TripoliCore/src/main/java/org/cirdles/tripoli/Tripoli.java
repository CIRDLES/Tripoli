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

package org.cirdles.tripoli;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.tripoli.utilities.file.TripoliFileResources;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author James F. Bowring
 */
public class Tripoli {
    public static final String VERSION;
    public static final String RELEASE_DATE;

    public static final StringBuilder ABOUT_WINDOW_CONTENT = new StringBuilder();
    public static final StringBuilder CONTRIBUTORS_CONTENT = new StringBuilder();
    public static final StringBuilder SUPPORTERS_CONTENT = new StringBuilder();

    public static final ResourceExtractor TRIPOLI_RESOURCE_EXTRACTOR = new ResourceExtractor(Tripoli.class);

    static {
        String version = "version";
        String releaseDate = "date";

        Path resourcePath = TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath("version.txt");

        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(resourcePath, charset)) {
            String line = reader.readLine();
            if (line != null) {
                String[] versionText = line.split("=");
                version = versionText[1];
            }

            line = reader.readLine();
            if (line != null) {
                String[] versionDate = line.split("=");
                releaseDate = versionDate[1];
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        // format is "x.y.z"
        VERSION = version;
        RELEASE_DATE = releaseDate;

        // get content for about window
        resourcePath = Tripoli.TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath("docs/aboutContent.txt");
        try (BufferedReader reader = Files.newBufferedReader(resourcePath, charset)) {
            String thisLine;
            while ((thisLine = reader.readLine()) != null) {
                Tripoli.ABOUT_WINDOW_CONTENT.append(thisLine);
            }
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        resourcePath = Tripoli.TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath("docs/contributorsContent.txt");
        try (BufferedReader reader = Files.newBufferedReader(resourcePath, charset)) {
            String thisLine;

            while ((thisLine = reader.readLine()) != null) {
                Tripoli.CONTRIBUTORS_CONTENT.append(thisLine);
            }

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        resourcePath = Tripoli.TRIPOLI_RESOURCE_EXTRACTOR.extractResourceAsPath("docs/supportersContent.txt");
        try (BufferedReader reader = Files.newBufferedReader(resourcePath, charset)) {
            String thisLine;

            while ((thisLine = reader.readLine()) != null) {
                Tripoli.SUPPORTERS_CONTENT.append(thisLine);
            }

        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }

        try {
            TripoliFileResources.initLocalResources();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}