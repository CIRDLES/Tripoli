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

package org.cirdles.tripoli.utilities.comparators;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public enum LiveDataEntryComparator {
    ;
    private static final Pattern FILE_PATTERN = Pattern.compile(".*-B(\\d+)-C(\\d+)\\.TXT", Pattern.CASE_INSENSITIVE);

    public static final Comparator<Path> blockCycleComparator = Comparator.comparingInt((Path path) -> {
        Matcher matcher = FILE_PATTERN.matcher(path.getFileName().toString());
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE; // block
    }).thenComparingInt(path -> {
        Matcher matcher = FILE_PATTERN.matcher(path.getFileName().toString());
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) : Integer.MAX_VALUE; // cycle
    });
}
