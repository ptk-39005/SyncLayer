package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

public class WALService {
    private static final String LOG_FILE = "swiftstock_wal.log";
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private final ExecutorService writerExecutor = Executors.newSingleThreadExecutor();

    public WALService() {
        writerExecutor.submit(this::processLogs);
    }

    public void log(String operation, String productId, int qty) {
        String entry = String.format("%d|%s|%s|%d",
                System.currentTimeMillis(), operation, productId, qty);
        logQueue.add(entry);
    }

    public void shutDown() {
        try {
            writerExecutor.shutdown();

            // Wait up to 5 seconds for the bg thread to finish the queue
            if (!writerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            writerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void processLogs() {
        Path path = Paths.get("swiftstock_wal.log");
        try (BufferedWriter writer = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            while (!writerExecutor.isShutdown() || !logQueue.isEmpty()) {

                String entry = logQueue.poll(100, TimeUnit.MILLISECONDS);

                if (entry != null) {
                    writer.write(entry);
                    writer.newLine();

                    List<String> extra = new ArrayList<>();
                    logQueue.drainTo(extra);
                    for (String line : extra) {
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.flush();
                }
            }
        } catch (IOException | InterruptedException e) {
        }
    }
}