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
