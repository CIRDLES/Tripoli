package org.cirdles.tripoli.valueModels;

import org.jetbrains.annotations.NotNull;

public interface ValueModelInterface{
    int compareTo(@NotNull ValueModel valueModel) throws ClassCastException;
}