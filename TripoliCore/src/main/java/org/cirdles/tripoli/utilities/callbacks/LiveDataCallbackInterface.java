package org.cirdles.tripoli.utilities.callbacks;

import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;

import java.nio.file.Path;

public interface LiveDataCallbackInterface {
    void onLiveDataUpdated(Path filePath, AnalysisInterface liveDataAnalysis);
}
