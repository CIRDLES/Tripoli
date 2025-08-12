package org.cirdles.tripoli.utilities.callbacks;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public interface FileWatcherCallbackInterface {
    void onFileEvent(Path fullPath, WatchEvent.Kind<?> kind);
}
