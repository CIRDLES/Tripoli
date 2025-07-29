package org.cirdles.tripoli;
import org.cirdles.tripoli.sessions.analysis.AnalysisInterface;
import org.cirdles.tripoli.sessions.analysis.massSpectrometerModels.dataSourceProcessors.phoenix.PhoenixLiveData;
import org.cirdles.tripoli.utilities.callbacks.LiveDataCallbackInterface;
import org.cirdles.tripoli.utilities.exceptions.TripoliException;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher implements Runnable {

    private final Path pathToWatch;
    private volatile boolean running = true;
    private LiveDataCallbackInterface listener;

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

            PhoenixLiveData phoenixLiveData = new PhoenixLiveData();

            while (running) {
                WatchKey key;
                try {
                    key = watchService.poll(500, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (key == null) {
                        continue; // Check running flag again
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path fileName = (Path) event.context();
                    System.out.printf("Event kind: %s, File: %s%n", kind.name(), fileName);
                    if (kind == ENTRY_CREATE) {
                        Path fullPath = pathToWatch.resolve(fileName);
                        AnalysisInterface liveDataAnalysis = phoenixLiveData.readLiveDataFile(fullPath);
                        if (listener != null && liveDataAnalysis != null) {
                            javafx.application.Platform.runLater(() ->
                                    listener.onLiveDataUpdated(fullPath, liveDataAnalysis)
                            );
                        }
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


}