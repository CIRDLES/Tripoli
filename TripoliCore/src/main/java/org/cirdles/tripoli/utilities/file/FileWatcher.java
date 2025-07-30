package org.cirdles.tripoli.utilities.file;

import org.cirdles.tripoli.utilities.callbacks.FileWatcherCallbackInterface;
import org.cirdles.tripoli.utilities.comparators.LiveDataEntryComparator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path pathToWatch;
    private final FileWatcherCallbackInterface callback;
    private volatile boolean running = true;

    public FileWatcher(Path pathToWatch, FileWatcherCallbackInterface callback) {
        this.pathToWatch = pathToWatch;
        this.callback = callback;
    }

    public void processExistingFiles() {

        try{
            List<Path> existingFiles = FileUtilities.listRegularFiles(pathToWatch);
            for (Path entry : existingFiles) {
                callback.onFileEvent(entry, StandardWatchEventKinds.ENTRY_CREATE);
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
                callback.onFileEvent(entry, StandardWatchEventKinds.ENTRY_CREATE);
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
                WatchKey key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (key == null) continue;

                for (WatchEvent<?> event : key.pollEvents()) {
                    Path fileName = (Path) event.context();
                    Path fullPath = pathToWatch.resolve(fileName);

                    if (callback != null) {
                        callback.onFileEvent(fullPath, event.kind());
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