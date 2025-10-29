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
 * This includes:
 *  - Writing detailed event logs
 *  - Saving summary statistics
 *  - Managing file naming and directories
 * <p>
 * By isolating file operations, we avoid cluttering the core
 * simulation code with I/O logic.
 */
public class FileOutputManager {

    /** Default output directory. */
    private static final String OUTPUT_DIR = "output/";

    /** Path to the event log file. */
    private final String eventLogPath;

    /** Path to the summary file. */
    private final String summaryPath;

    /** Writer for appending event logs efficiently. */
    private PrintWriter eventWriter;

    /**
     * Constructs a FileOutputManager and ensures the output
     * directory exists before writing any files.
     */
    public FileOutputManager() {
        try {
            Files.createDirectories(Path.of(OUTPUT_DIR));
        } catch (IOException e) {
            System.err.println("Warning: Unable to create output directory: " + e.getMessage());
        }

        // Generate time-stamped filenames to avoid overwriting old runs
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        this.eventLogPath = OUTPUT_DIR + "event_log_" + timestamp + ".txt";
        this.summaryPath = OUTPUT_DIR + "summary_" + timestamp + ".txt";

        try {
            eventWriter = new PrintWriter(new FileWriter(eventLogPath, true));
            eventWriter.printf("# Telecom Network Traffic Simulator Event Log%n# Created: %s%n%n", timestamp);
        } catch (IOException e) {
            System.err.println("Error opening event log file: " + e.getMessage());
        }
    }

    /**
     * Logs a simulation event to the event log file.
     *
     * @param event the event to log
     */
    public void logEvent(Event event) {
        if (eventWriter == null) return;
        eventWriter.printf("t=%.3f, source=%d, type=%s%n",
                event.getTime(), event.getSourceId(), event.getType());
    }

    /**
     * Saves a summary of the simulation statistics to a text file.
     *
     * @param stats the StatisticsCollector containing summary data
     */
    public void saveSummary(StatisticsCollector stats) {
        try (FileWriter writer = new FileWriter(summaryPath)) {
            writer.write("=== Telecom Network Traffic Simulator Summary ===\n");
            writer.write(String.format("Average Rate: %.4f%n", stats.getAverageRate()));
            writer.write(String.format("Peak Rate: %.4f%n", stats.getPeakRate()));
            writer.write(String.format("Std Dev: %.4f%n", stats.getStdDevRate()));
            writer.write(String.format("Samples Recorded: %d%n", stats.getSampleCount()));
            writer.write("=================================================\n");
            System.out.println("Summary saved to: " + summaryPath);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    /**
     * Closes the log writer and flushes all buffered data to disk.
     * <p>
     * This should be called at the end of the simulation.
     */
    public void close() {
        if (eventWriter != null) {
            eventWriter.flush();
            eventWriter.close();
            System.out.println("Event log saved to: " + eventLogPath);
        }
    }
}
