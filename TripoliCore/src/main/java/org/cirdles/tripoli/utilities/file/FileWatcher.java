package org.cirdles.tripoli.utilities.file;

import org.cirdles.tripoli.utilities.callbacks.FileWatcherCallbackInterface;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path pathToWatch;
    private final FileWatcherCallbackInterface callback;
    private volatile boolean running = true;
    private long timeoutSeconds;
    private long lastEventTime;
    private boolean eventOccurred = false;

    public FileWatcher(Path pathToWatch, FileWatcherCallbackInterface callback) {
        this.pathToWatch = pathToWatch;
        this.callback = callback;
        this.timeoutSeconds = 0;
        lastEventTime = System.currentTimeMillis();
    }

    /**
     * If set, the watcher will signal the callback if no events occur for the specified number of seconds. The signal
     * returned event will have a null path and kind. This represents an idle state.
     * @param seconds Number of seconds to wait before signaling the callback.
     */
    public void setTimeoutSeconds(long seconds){
        timeoutSeconds = seconds*1000;
    }

    public Path getPath(){
        return pathToWatch;
    }

    public void processExistingFiles() {
        try{
            List<Path> existingFiles = FileUtilities.listRegularFiles(pathToWatch);
            for (Path entry : existingFiles) {
                callback.onFileEvent(entry, ENTRY_CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void processExistingFiles(Comparator<Path> comparator) {
        try {
            List<Path> existingFiles = FileUtilities.listRegularFiles(pathToWatch);

            if (comparator != null) {
                existingFiles.sort(comparator);
            }

            for (Path entry : existingFiles) {
                callback.onFileEvent(entry, ENTRY_CREATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            pathToWatch.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            System.out.println("Started watching: " + pathToWatch);

            while (running) {

                // Idle check
                if (eventOccurred && timeoutSeconds > 0) {
                    long now = System.currentTimeMillis();
                    if (now - lastEventTime >= timeoutSeconds && callback != null) {
                        callback.onFileEvent(null, null); // Idle signal
                    }
                }

                WatchKey key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    Path fullPath = pathToWatch.resolve(fileName);

                    if (callback != null) {
                        callback.onFileEvent(fullPath, event.kind());
                        eventOccurred = true;
                        lastEventTime = System.currentTimeMillis();
                    }
                }

                if (!key.reset()) break;
            }

            System.out.println("Stopped watching.");

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}