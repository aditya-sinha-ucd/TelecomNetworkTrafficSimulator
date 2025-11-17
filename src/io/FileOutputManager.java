/**
 * @file src/io/FileOutputManager.java
 * @brief Concrete {@link OutputSink} that persists artifacts to disk.
 * @details Owns per-run directories, handles event logs, summaries, CSVs, and
 *          metadata files for both simulator and FGN workflows. Centralizing
 *          IO keeps {@link core.Simulator} and console handlers focused on
 *          orchestration while this class manages formatting, file creation,
 *          and metadata serialization.
 * @date 2024-05-30
 */
package io;

import core.Event;
import core.StatisticsCollector;
import extensions.NetworkQueue;
import util.HurstEstimator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @class FileOutputManager
 * @brief Implements {@link OutputSink} using timestamped run directories.
 * @details Responsible for file lifecycle management, metadata capture, and
 *          statistical summaries. Collaborates with {@link util.HurstEstimator}
 *          to validate generated sequences and with {@link extensions.NetworkQueue}
 *          to report congestion metrics.
 */
public class FileOutputManager implements OutputSink {

    /** Minimum samples needed for a reliable Hurst estimate in FGN mode. */
    public static final int MIN_FGN_HURST_SAMPLES = 512;

    /** Base folder where all simulation results will be saved. */
    private static final String OUTPUT_ROOT = "output/";

    /** Directory for this specific simulation run. */
    private final String runDir;

    /** Path to the text file where all simulation events are logged. */
    private final String eventLogPath;

    /** Optional descriptive metadata about the run. */
    private final Map<String, String> metadata;

    /** Writer used to record events as they happen during the simulation. */
    private PrintWriter eventWriter;

    /**
     * @brief Creates a new output folder for the current run.
     * @details The folder name includes a timestamp so that multiple runs can
     *          be stored without overwriting previous results.
     */
    public FileOutputManager() {
        this(Collections.emptyMap());
    }

    /**
     * @brief Creates a new output manager that also stores run metadata.
     * @param metadata Descriptive context (mode, parameters, etc.) supplied by
     *                 simulation handlers; may be {@code null} to skip metadata.
     */
    public FileOutputManager(Map<String, String> metadata) {
        this.metadata = metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        this.runDir = OUTPUT_ROOT + "run_" + timestamp + "/";
        this.eventLogPath = runDir + "event_log.txt";

        try {
            Files.createDirectories(Path.of(runDir));
            eventWriter = new PrintWriter(new FileWriter(eventLogPath, true));
            eventWriter.printf("# Telecom Network Traffic Simulator Event Log%n# Created: %s%n", timestamp);
            if (!this.metadata.isEmpty()) {
                eventWriter.println("# --- Run Metadata ---");
                this.metadata.forEach((key, value) ->
                        eventWriter.printf("# %s: %s%n", key, value));
            }
            eventWriter.println();
            writeMetadataFile();
        } catch (IOException e) {
            System.err.println("Error creating output folder or log file: " + e.getMessage());
        }
    }

    /**
     * @brief Returns the path of this run's output folder.
     * @return Path string pointing to the run directory.
     */
    @Override
    public String getRunDirectory() {
        return runDir;
    }

    /**
     * @brief Writes a single simulation event to the log file.
     * @param event Event emitted by {@link core.Simulator}.
     */
    @Override
    public void logEvent(Event event) {
        if (eventWriter == null) return;
        eventWriter.printf("t=%.3f, source=%d, type=%s%n",
                event.getTime(), event.getSourceId(), event.getType());
    }

    /**
     * @brief Writes a friendly, sectioned summary pairing metrics with metadata.
     * @param stats Traffic statistics gathered during the run.
     * @param queue Queue metrics collected alongside traffic stats.
     */
    @Override
    public void saveSummary(StatisticsCollector stats, NetworkQueue queue) {
        String summaryPath = runDir + "summary.txt";
        try (FileWriter writer = new FileWriter(summaryPath)) {
            writer.write("=== Telecom Network Traffic Simulator Report ===\n");
            writer.write(String.format("Run Directory: %s%n%n", runDir));

            appendMetadataSection(writer);
            writer.write('\n');

            writer.write("Traffic Statistics\n");
            writer.write("------------------\n");
            writer.write(String.format("Samples Recorded : %d%n", stats.getSampleCount()));
            writer.write(String.format("Average Rate     : %.4f%n", stats.getAverageRate()));
            writer.write(String.format("Peak Rate        : %.4f%n", stats.getPeakRate()));
            writer.write(String.format("Std Dev          : %.4f%n", stats.getStdDevRate()));
            writer.write(String.format("Hurst Exponent   : %.4f%n", stats.getHurstExponent()));

            writer.write("\nQueue Metrics\n");
            writer.write("-------------\n");
            writer.write(String.format("Arrivals           : %d%n", queue.getTotalArrived()));
            writer.write(String.format("Served             : %d%n", queue.getTotalServed()));
            writer.write(String.format("Dropped            : %d%n", queue.getTotalDropped()));
            writer.write(String.format("Average Waiting (s): %.4f%n", queue.getAvgWaitingTime()));
            writer.write(String.format("Average System (s) : %.4f%n", queue.getAvgSystemTime()));
            writer.write("=================================================\n");
            System.out.println("Summary saved to: " + summaryPath);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    /**
     * @brief Saves data generated in FGN mode, including CSV and summary.
     * @param series Generated time-series samples.
     * @param H Target Hurst exponent.
     * @param sigma Standard deviation used for generation.
     * @param samplingInterval Time spacing between samples.
     * @param threshold Threshold for ON/OFF labeling in the event log.
     */
    @Override
    public void saveFGNResults(double[] series, double H, double sigma,
                               double samplingInterval, double threshold) {
        try {
            String csvPath = runDir + "traffic_data.csv";
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvPath))) {
                writer.println("Index,Value");
                for (int i = 0; i < series.length; i++) {
                    writer.printf("%d,%.10f%n", i, series[i]);
                }
            }

            writeFGNEventLog(series, samplingInterval, threshold);

            final double MIN_VAR = 1e-12;

            double meanVal = Arrays.stream(series).average().orElse(0.0);
            double variance = Arrays.stream(series)
                    .map(v -> (v - meanVal) * (v - meanVal))
                    .average().orElse(0.0);

            Double estH = Double.NaN;
            String hurstNote = null;
            if (series.length < MIN_FGN_HURST_SAMPLES) {
                hurstNote = String.format("requires >= %d samples (generated %d)",
                        MIN_FGN_HURST_SAMPLES, series.length);
            } else if (variance <= MIN_VAR) {
                hurstNote = "variance too low (increase σ or threshold spread)";
            } else {
                List<Double> data = Arrays.stream(series).boxed().collect(Collectors.toList());
                estH = HurstEstimator.estimateHurst(data);
            }

            String summaryPath = runDir + "summary.txt";
            try (PrintWriter sw = new PrintWriter(new FileWriter(summaryPath))) {
                sw.println("=== FGN Generation Report ===");
                sw.printf("Run Directory: %s%n%n", runDir);
                appendMetadataSection(sw);

                sw.println();
                sw.println("Series Statistics");
                sw.println("-----------------");
                sw.printf("Samples Generated : %d%n", series.length);
                sw.printf("Target Hurst (H) : %.4f%n", H);
                sw.printf("Sigma            : %.6f%n", sigma);
                sw.printf("Mean Value       : %.6f%n", meanVal);
                sw.printf("Std Dev          : %.6f%n", Math.sqrt(Math.max(variance, 0)));
                if (estH.isNaN()) {
                    if (hurstNote == null) {
                        sw.println("Estimated Hurst  : not computed (estimator returned NaN)");
                    } else {
                        sw.printf("Estimated Hurst  : not computed (%s)%n", hurstNote);
                    }
                } else {
                    sw.printf("Estimated Hurst  : %.4f%n", estH);
                }

                sw.println();
                sw.println("Interpretation Notes");
                sw.println("--------------------");
                sw.printf("Sampling Interval : %.6f seconds%n", samplingInterval);
                sw.printf("ON/OFF Threshold  : %.6f (>= threshold logs as ON)%n", threshold);
                sw.println("===============================");
            }

            if (estH.isNaN()) {
                if (hurstNote == null) {
                    System.out.println("Estimated Hurst exponent could not be computed (estimator returned NaN).");
                } else {
                    System.out.printf("Estimated Hurst exponent skipped: %s.%n", hurstNote);
                }
            } else {
                System.out.printf("Estimated Hurst exponent (validation): %.3f%n", estH);
            }

            System.out.printf("FGN data saved: %s%n", csvPath);
            System.out.printf("FGN summary saved: %s%n", summaryPath);
            System.out.printf("All results saved in: %s%n", runDir);

        } catch (IOException e) {
            System.err.println("Error saving FGN results: " + e.getMessage());
        }
    }

    /**
     * @brief Writes the generated FGN series to the event log for traceability.
     * @param series Generated time-series.
     * @param samplingInterval Sampling interval used when generating data.
     * @param threshold Threshold for ON/OFF labeling.
     */
    private void writeFGNEventLog(double[] series, double samplingInterval, double threshold) {
        if (eventWriter == null) {
            return;
        }
        eventWriter.println("# Fractional Gaussian Noise samples");
        double time = 0.0;
        for (int i = 0; i < series.length; i++) {
            String state = series[i] >= threshold ? "ON" : "OFF";
            eventWriter.printf("t=%.6f, sample=%d, value=%.10f, state=%s%n",
                    time, i, series[i], state);
            time += samplingInterval;
        }
        eventWriter.flush();
    }

    /**
     * @brief Flushes and closes the event log safely at the end of the run.
     */
    @Override
    public void close() {
        if (eventWriter != null) {
            eventWriter.flush();
            eventWriter.close();
            System.out.println("Event log saved to: " + eventLogPath);
        }
    }

    /**
     * @brief Writes run metadata to a JSON file to aid later comparisons.
     */
    private void writeMetadataFile() {
        if (metadata.isEmpty()) {
            return;
        }
        Path metadataPath = Path.of(runDir, "metadata.json");
        try (BufferedWriter writer = Files.newBufferedWriter(metadataPath)) {
            writer.write("{\n");
            int index = 0;
            int size = metadata.size();
            for (Map.Entry<String, String> entry : metadata.entrySet()) {
                String key = escapeJson(entry.getKey());
                String value = escapeJson(entry.getValue());
                writer.write(String.format("  \"%s\": \"%s\"%s%n",
                        key, value, (++index < size) ? "," : ""));
            }
            writer.write("}\n");
        } catch (IOException e) {
            System.err.println("Error writing metadata file: " + e.getMessage());
        }
    }

    /**
     * @brief Appends a human-readable metadata section to a text-based report.
     * @param writer Appendable receiving the metadata section.
     * @throws IOException if writing to {@code writer} fails.
     */
    private void appendMetadataSection(Appendable writer) throws IOException {
        writer.append("Run Metadata\n");
        writer.append("------------\n");
        if (metadata.isEmpty()) {
            writer.append("  (no metadata provided)\n");
            return;
        }
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            writer.append("  ")
                    .append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue() == null ? "" : entry.getValue())
                    .append('\n');
        }
    }

    /**
     * @brief Escapes JSON special characters for metadata serialization.
     * @param value Raw metadata value.
     * @return Escaped representation safe for JSON output.
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
