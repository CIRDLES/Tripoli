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




import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

/**
 * @author James F. Bowring
 */
public enum FileUtilities {
    ;

    public static void recursiveDelete(Path pathToBeDeleted)
            throws IOException {
        Files.walk(pathToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static boolean isFileClosedWindows(File file) {
        return file.renameTo(file);
    }

    public static boolean isFileClosedUnix(File file) {
        try {
            Process plsof = new ProcessBuilder("lsof", "|", "grep", file.getAbsolutePath()).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(plsof.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while (null != (line = reader.readLine())) {
                if (line.contains(file.getAbsolutePath())) {
                    reader.close();
                    plsof.destroy();
                    return false;
                }
            }
            reader.close();
            plsof.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /*
    public static void unpackZipFile(File archive, File targetDirectory)
            throws IOException {
        ZipFile zipFile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        // Dec 2021 this fix comes from https://cwe.mitre.org/data/definitions/23.html and SNYK CODE
        // via https://app.snyk.io/org/bowring/project/7dd848fc-362b-4514-a91c-3c04628633ac
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            Path entryPath = targetDirectory.toPath().resolve(entry.getName());
            if (!entryPath.normalize().startsWith(targetDirectory.toPath()))
                throw new IOException("Zip entry contained path traversal");
            if (entry.isDirectory()) {
                Files.createDirectories(entryPath);
            } else {
                Files.createDirectories(entryPath.getParent());
                try (InputStream in = zipFile.getInputStream(entry)) {
                    try (OutputStream out = new FileOutputStream(entryPath.toFile())) {
                        IOUtils.copy(in, out);
                    }
                }
            }
        }
    }
     */

    public static List<Path> listRegularFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    files.add(entry);
                }
            }
        }
        return files;
    }



}