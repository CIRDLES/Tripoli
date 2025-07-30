package org.cirdles.tripoli;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PhoenixLiveData;
import org.cirdles.tripoli.utilities.callbacks.LiveDataCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path pathToWatch;
    private volatile boolean running = true;
    private LiveDataCallbackInterface listener;
    private PhoenixLiveData phoenixLiveData;

    public FileWatcher(Path pathToWatch) {
        this.pathToWatch = pathToWatch;
    }
    public void setLiveDataUpdateListener(LiveDataCallbackInterface listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            pathToWatch.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            System.out.println("Started watching: " + pathToWatch);

            phoenixLiveData = new PhoenixLiveData();
            recordInitialFiles();

            while (running) {
                WatchKey key;
                try {
                    key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (key == null) {
                        continue;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                // Handle event
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == ENTRY_CREATE) {
                        recordCreateEvent(event);
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }

            System.out.println("Stopped watching.");

        } catch (IOException | TripoliException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }

    private void recordCreateEvent(WatchEvent<?> event){
        Path fileName = (Path) event.context();

        Path fullPath = pathToWatch.resolve(fileName);
        AnalysisInterface liveDataAnalysis = phoenixLiveData.readLiveDataFile(fullPath);
        if (listener != null && liveDataAnalysis != null) {
            javafx.application.Platform.runLater(() ->
                    listener.onLiveDataUpdated(fullPath, liveDataAnalysis)
            );
        }

    }

    private void recordInitialFiles() {
        try {
            // Get existing files
            List<Path> existingLiveData = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToWatch)) {
                for (Path entry : stream) {
                    if (Files.isRegularFile(entry)) {
                        existingLiveData.add(entry);
                    }
                }
            }

            // Analyze the files
            for (Path path : existingLiveData) {
                AnalysisInterface liveDataAnalysis = phoenixLiveData.readLiveDataFile(path);
                if (listener != null && liveDataAnalysis != null) {
                    javafx.application.Platform.runLater(() ->
                            listener.onLiveDataUpdated(path, liveDataAnalysis)
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}