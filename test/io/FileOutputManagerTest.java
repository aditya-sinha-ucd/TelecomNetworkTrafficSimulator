package io;

import core.StatisticsCollector;
import extensions.NetworkQueue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests focused on the {@link FileOutputManager}'s metadata persistence
 * features.
 */
public class FileOutputManagerTest {

    @Test
    void metadataIsPersistedToBothEventLogAndJson() throws IOException {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("mode", "FGN");
        metadata.put("sources", "0");
        metadata.put("note", "unit-test");

        String runDir = null;
        try (FileOutputManager manager = new FileOutputManager(metadata)) {
            runDir = manager.getRunDirectory();
        }

        assertNotNull(runDir, "Output manager should expose a run directory");
        Path runPath = Path.of(runDir);
        Path metadataFile = runPath.resolve("metadata.json");
        Path eventLog = runPath.resolve("event_log.txt");

        assertTrue(Files.exists(metadataFile), "metadata.json should be created");
        String json = Files.readString(metadataFile);
        assertTrue(json.contains("\"mode\": \"FGN\""), "JSON should include run metadata");
        assertTrue(json.contains("\"note\": \"unit-test\""), "JSON should include custom fields");

        assertTrue(Files.exists(eventLog), "Event log should exist");
        String logContent = Files.readString(eventLog);
        assertTrue(logContent.contains("# mode: FGN"), "Event log should list metadata header");
        assertTrue(logContent.contains("# note: unit-test"), "Event log should echo metadata values");

        deleteRecursively(runPath);
    }

    @Test
    void summaryIncludesMetadataAndClearSections() throws IOException {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("mode", "PARETO");
        metadata.put("sources", "3");

        StatisticsCollector stats = new StatisticsCollector(1.0);
        stats.recordSample(0.0, 0.5);
        stats.recordSample(1.0, 0.75);

        NetworkQueue queue = new NetworkQueue(10.0);
        queue.enqueueBulk(0.0, 2);
        queue.processUntil(1.0);

        Path runPath;
        try (FileOutputManager manager = new FileOutputManager(metadata)) {
            runPath = Path.of(manager.getRunDirectory());
            manager.saveSummary(stats, queue);
        }

        Path summaryPath = runPath.resolve("summary.txt");
        assertTrue(Files.exists(summaryPath), "summary.txt should be written");
        String summary = Files.readString(summaryPath);
        assertTrue(summary.contains("Run Metadata"));
        assertTrue(summary.contains("mode: PARETO"));
        assertTrue(summary.contains("Traffic Statistics"));
        assertTrue(summary.contains("Queue Metrics"));

        deleteRecursively(runPath);
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(dir)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to clean test directory: " + path, e);
                }
            });
        }
    }
}
