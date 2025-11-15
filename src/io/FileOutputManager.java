package io;

import core.Event;
import core.StatisticsCollector;
import extensions.NetworkQueue;
import util.HurstEstimator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all file output operations for the simulator.
 *
 * This class is responsible for creating a new output folder for each run,
 * writing event logs, saving simulation summaries, and handling FGN mode output.
 * Keeping all file operations here keeps the simulator logic clean and organized.
 */
public class FileOutputManager {

    /** Minimum samples needed for a reliable Hurst estimate in FGN mode. */
    public static final int MIN_FGN_HURST_SAMPLES = 512;

    /** Base folder where all simulation results will be saved. */
    private static final String OUTPUT_ROOT = "output/";

    /** Directory for this specific simulation run. */
    private final String runDir;

    /** Path to the text file where all simulation events are logged. */
    private final String eventLogPath;

    /** Writer used to record events as they happen during the simulation. */
    private PrintWriter eventWriter;

    /**
     * Creates a new output folder for the current run.
     * The folder name includes a timestamp so that multiple runs can be stored
     * without overwriting previous results.
     */
    public FileOutputManager() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        this.runDir = OUTPUT_ROOT + "run_" + timestamp + "/";
        this.eventLogPath = runDir + "event_log.txt";

        try {
            Files.createDirectories(Path.of(runDir));
            eventWriter = new PrintWriter(new FileWriter(eventLogPath, true));
            eventWriter.printf("# Telecom Network Traffic Simulator Event Log%n# Created: %s%n%n", timestamp);
        } catch (IOException e) {
            System.err.println("Error creating output folder or log file: " + e.getMessage());
        }
    }

    /** Returns the path of this run's output folder. */
    public String getRunDirectory() {
        return runDir;
    }

    /**
     * Writes a single simulation event to the log file.
     * Each line includes the event time, source ID, and event type.
     */
    public void logEvent(Event event) {
        if (eventWriter == null) return;
        eventWriter.printf("t=%.3f, source=%d, type=%s%n",
                event.getTime(), event.getSourceId(), event.getType());
    }

    /**
     * Writes a summary file containing key statistics collected during the run.
     * This includes the number of samples, average and peak rates, standard deviation,
     * and an estimated Hurst exponent.
     */
    public void saveSummary(StatisticsCollector stats, NetworkQueue queue) {
        String summaryPath = runDir + "summary.txt";
        try (FileWriter writer = new FileWriter(summaryPath)) {
            writer.write("=== Telecom Network Traffic Simulator Summary ===\n");
            writer.write(String.format("Samples Recorded: %d%n", stats.getSampleCount()));
            writer.write(String.format("Average Rate: %.4f%n", stats.getAverageRate()));
            writer.write(String.format("Peak Rate: %.4f%n", stats.getPeakRate()));
            writer.write(String.format("Std Dev: %.4f%n", stats.getStdDevRate()));
            writer.write(String.format("Estimated Hurst Exponent: %.4f%n", stats.getHurstExponent()));
            writer.write("--- Network Queue Metrics ---\n");
            writer.write(String.format("Arrivals: %d%n", queue.getTotalArrived()));
            writer.write(String.format("Served: %d%n", queue.getTotalServed()));
            writer.write(String.format("Dropped: %d%n", queue.getTotalDropped()));
            writer.write(String.format("Average Waiting Time: %.4f%n", queue.getAvgWaitingTime()));
            writer.write(String.format("Average System Time: %.4f%n", queue.getAvgSystemTime()));
            writer.write("=================================================\n");
            System.out.println("Summary saved to: " + summaryPath);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    /**
     * Saves data generated in FGN mode.
     * The generated time-series is written to a CSV file, and a short text summary
     * with the Hurst estimation is saved in the same folder.
     */
    public void saveFGNResults(double[] series, double H, double sigma, double mean,
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

            final int MIN_SAMPLES = 512;
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
                sw.println("=== FGN Generation Summary ===");
                sw.printf("Samples Generated: %d%n", series.length);
                sw.printf("Target Hurst (H): %.4f%n", H);
                sw.printf("Sigma: %.6f%n", sigma);
                if (estH.isNaN()) {
                    if (hurstNote == null) {
                        sw.println("Estimated Hurst: not computed (estimator returned NaN)");
                    } else {
                        sw.printf("Estimated Hurst: not computed (%s)%n", hurstNote);
                    }
                } else {
                    sw.printf("Estimated Hurst: %.4f%n", estH);
                }
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

    /** Writes the generated FGN series to the event log for traceability. */
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

    /** Flushes and closes the event log safely at the end of the run. */
    public void close() {
        if (eventWriter != null) {
            eventWriter.flush();
            eventWriter.close();
            System.out.println("Event log saved to: " + eventLogPath);
        }
    }
}
