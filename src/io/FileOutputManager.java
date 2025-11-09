package io;

import core.Event;
import core.StatisticsCollector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles all file output operations for the simulator.
 * <p>
 * Responsibilities:
 *  - Manage per-run output directories
 *  - Write detailed event logs
 *  - Save summary statistics
 * <p>
 * Centralizing I/O ensures clean separation between simulation logic
 * and file management, improving maintainability.
 */
public class FileOutputManager {

    /** Root output directory. */
    private static final String OUTPUT_ROOT = "output/";

    /** Directory specific to this simulation run. */
    private final String runDir;

    /** Path to the event log file. */
    private final String eventLogPath;

    /** Writer for appending event logs efficiently. */
    private PrintWriter eventWriter;

    /**
     * Constructs a FileOutputManager for a unique simulation run.
     * Automatically creates a timestamped folder to contain all
     * generated files for this run.
     */
    public FileOutputManager() {
        // Create timestamped run folder
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        this.runDir = OUTPUT_ROOT + "run_" + timestamp + "/";
        this.eventLogPath = runDir + "event_log.txt";

        try {
            Files.createDirectories(Path.of(runDir));
            eventWriter = new PrintWriter(new FileWriter(eventLogPath, true));
            eventWriter.printf("# Telecom Network Traffic Simulator Event Log%n# Created: %s%n%n", timestamp);
        } catch (IOException e) {
            System.err.println("Error initializing event log file: " + e.getMessage());
        }
    }

    /** Returns this run's directory path. */
    public String getRunDirectory() {
        return runDir;
    }

    /** Writes a single event record to the event log. */
    public void logEvent(Event event) {
        if (eventWriter == null) return;
        eventWriter.printf("t=%.3f, source=%d, type=%s%n",
                event.getTime(), event.getSourceId(), event.getType());
    }

    /**
     * Saves a simulation summary file inside the run folder.
     *
     * @param stats StatisticsCollector with summary data
     */
    public void saveSummary(StatisticsCollector stats) {
        String summaryPath = runDir + "summary.txt";
        try (FileWriter writer = new FileWriter(summaryPath)) {
            writer.write("=== Telecom Network Traffic Simulator Summary ===\n");
            writer.write(String.format("Samples Recorded: %d%n", stats.getSampleCount()));
            writer.write(String.format("Average Rate: %.4f%n", stats.getAverageRate()));
            writer.write(String.format("Peak Rate: %.4f%n", stats.getPeakRate()));
            writer.write(String.format("Std Dev: %.4f%n", stats.getStdDevRate()));
            writer.write(String.format("Estimated Hurst Exponent: %.4f%n", stats.getHurstExponent()));
            writer.write("=================================================\n");
            System.out.println("Summary saved to: " + summaryPath);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    /** Closes the event writer and flushes remaining data to disk. */
    public void close() {
        if (eventWriter != null) {
            eventWriter.flush();
            eventWriter.close();
            System.out.println("Event log saved to: " + eventLogPath);
        }
    }
}
