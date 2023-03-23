/*
 * Copyright 2018 James F. Bowring and CIRDLES.org.
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

/**
 * @author ryanb
 */
public enum FileNameFixer {
    ;

    public static String fixFileName(String name) {
        String nameFixed = name.replaceAll(" ", "_");
        nameFixed = nameFixed.replaceAll("\\^", "");
        nameFixed = nameFixed.replaceAll("\\n", "");
        nameFixed = nameFixed.replaceAll("\\*", "");
        nameFixed = nameFixed.replaceAll("\\[", "");
        nameFixed = nameFixed.replaceAll("]", "");
        nameFixed = nameFixed.replaceAll("\\(", "");
        nameFixed = nameFixed.replaceAll("\\)", "");
        nameFixed = nameFixed.replaceAll("!", "");
        nameFixed = nameFixed.replaceAll("#", "");
        nameFixed = nameFixed.replaceAll("&", "");
        nameFixed = nameFixed.replaceAll("@", "");
        nameFixed = nameFixed.replaceAll("\\+", "");
        nameFixed = nameFixed.replaceAll("/", "");
        nameFixed = nameFixed.replaceAll("\\{", "");
        nameFixed = nameFixed.replaceAll("}", "");
        nameFixed = nameFixed.replaceAll(":", "");
        nameFixed = nameFixed.replaceAll("\\?", "");
        nameFixed = nameFixed.replaceAll(">", "");
        nameFixed = nameFixed.replaceAll("<", "");
        nameFixed = nameFixed.replaceAll("%", "");
        nameFixed = nameFixed.replaceAll("'", "");
        nameFixed = nameFixed.replaceAll("\"", "");
        nameFixed = nameFixed.replaceAll("~", "");
        nameFixed = nameFixed.replaceAll("`", "");
        nameFixed = nameFixed.replaceAll("=", "");
        while (nameFixed.contains("__")) {
            nameFixed = nameFixed.replaceAll("__", "_");
        }

        return nameFixed;
    }

    public static String fixFileName(File file) {
        return fixFileName(file.toString());
    }
}