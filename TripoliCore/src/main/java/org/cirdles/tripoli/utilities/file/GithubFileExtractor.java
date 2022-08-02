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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GithubFileExtractor {
    public static void extractGithubFile(String fileRawURI, String fileName) {
        java.net.URL url;

        try {
            url = new java.net.URL(fileRawURI);
            java.net.URLConnection uc;
            uc = url.openConnection();

            uc.setRequestProperty("X-Requested-With", "Curl");

            BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            StringBuilder fileContents = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                fileContents.append(line).append("\n");

            Path path = Paths.get(fileName);
            byte[] strToBytes = fileContents.toString().getBytes();

            Files.write(path, strToBytes);

        } catch (IOException e) {
            System.out.println("Could not read " + fileRawURI);
        }
    }
}