/**
 * @file src/core/StatisticsCollector.java
 * @brief Captures time-series samples and derives summary metrics.
 * @details The collector is fed by {@link core.Simulator} at a fixed sampling
 *          cadence. It stores timestamps and aggregate activity rates, then
 *          exposes descriptive statistics (mean, max, deviation) and advanced
 *          measures such as the Hurst exponent via {@link util.HurstEstimator}.
 *          Outputs are written to CSV or text summaries through export helpers.
 *          Collaborates with {@link util.MathUtils} for numerical routines.
 * @date 2024-05-30
 */
package core;

import util.MathUtils;
import util.HurstEstimator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @class StatisticsCollector
 * @brief Immutable-interval statistics sink for the simulator.
 * @details Receives (time, rate) tuples, keeps them in-memory, and offers
 *          reporting utilities suited for visualization or downstream analysis.
 *          Inputs: timestamps and aggregate rates from {@link core.Simulator}.
 *          Outputs: summary metrics, Hurst estimates, CSV/text exports.
 */
public class StatisticsCollector {

    /** Recorded timestamps for each sample. */
    private final List<Double> timeStamps;
    /** Recorded aggregate rate per timestamp. */
    private final List<Double> activityRates;
    /** Sampling interval provided via configuration. */
    private final double sampleInterval;

    /**
     * @brief Constructs a collector bound to a fixed sampling interval.
     * @param sampleInterval Interval between consecutive samples produced by
     *                       {@link core.Simulator}.
     */
    public StatisticsCollector(double sampleInterval) {
        this.sampleInterval = sampleInterval;
        this.timeStamps = new ArrayList<>();
        this.activityRates = new ArrayList<>();
    }

    /**
     * @brief Records a new aggregate traffic-rate sample.
     * @param time Simulation timestamp of the observation.
     * @param aggregateRate Fraction of ON sources at {@code time}.
     */
    public void recordSample(double time, double aggregateRate) {
        timeStamps.add(time);
        activityRates.add(aggregateRate);
    }

    /**
     * @brief Computes the mean of all recorded activity rates.
     * @return Average fraction of ON sources over the captured window.
     */
    public double getAverageRate() {
        return MathUtils.mean(activityRates);
    }

    /**
     * @brief Retrieves the maximum (peak) activity rate recorded.
     * @return Highest observed aggregate rate.
     */
    public double getPeakRate() {
        return MathUtils.max(activityRates);
    }

    /**
     * @brief Calculates the standard deviation of activity rates.
     * @return Sample standard deviation of the aggregate rate series.
     */
    public double getStdDevRate() {
        return MathUtils.stdDev(activityRates);
    }

    /**
     * @brief Reports the total number of recorded samples.
     * @return Count of stored observations.
     */
    public int getSampleCount() {
        return activityRates.size();
    }

    /**
     * @brief Provides a defensive copy of the activity-rate series.
     * @return Mutable list containing the recorded aggregate rates.
     */
    public List<Double> getActivityRates() {
        return new ArrayList<>(activityRates);
    }

    /**
     * @brief Estimates the Hurst exponent of the recorded activity series.
     * @return Hurst exponent or {@code 0.0} if insufficient data points exist.
     */
    public double getHurstExponent() {
        if (activityRates.size() < 10) return 0.0;
        return HurstEstimator.estimateHurst(activityRates);
    }

    /**
     * @brief Prints a formatted summary table to the console.
     */
    public void printSummary() {
        System.out.println("\n=== Simulation Summary ===");
        System.out.printf("Samples recorded: %d%n", getSampleCount());
        System.out.printf("Average rate: %.4f%n", getAverageRate());
        System.out.printf("Peak rate: %.4f%n", getPeakRate());
        System.out.printf("Std deviation: %.4f%n", getStdDevRate());
        System.out.printf("Estimated Hurst exponent: %.3f%n", getHurstExponent());
        System.out.println("===========================\n");
    }

    /**
     * @brief Writes recorded time-series data to a CSV file.
     * @param fileName Destination path relative to the simulation run directory.
     */
    public void exportToCSV(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Time,AggregateRate\n");
            for (int i = 0; i < timeStamps.size(); i++) {
                writer.write(String.format("%.4f,%.6f%n",
                        timeStamps.get(i), activityRates.get(i)));
            }
            System.out.println("Time-series data exported to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * @brief Exports summary statistics to a human-readable text file.
     * @param fileName Destination file for the summary block.
     */
    public void exportSummary(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("=== Simulation Summary ===\n");
            writer.write(String.format("Samples recorded: %d%n", getSampleCount()));
            writer.write(String.format("Average rate: %.4f%n", getAverageRate()));
            writer.write(String.format("Peak rate: %.4f%n", getPeakRate()));
            writer.write(String.format("Std deviation: %.4f%n", getStdDevRate()));
            writer.write(String.format("Estimated Hurst exponent: %.3f%n", getHurstExponent()));
            writer.write("===========================\n");
            System.out.println("Summary exported to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing summary file: " + e.getMessage());
        }
    }

    /**
     * @brief Appends summary stats to an experiment-level CSV.
     * @param fileName Aggregated CSV capturing multiple runs.
     */
    public void appendSummaryToCSV(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(String.format("%.4f,%.4f,%.4f,%.4f,%d%n",
                    getAverageRate(), getPeakRate(),
                    getStdDevRate(), getHurstExponent(), getSampleCount()));
        } catch (IOException e) {
            System.err.println("Error appending summary CSV: " + e.getMessage());
        }
    }

    /**
     * @brief Clears all recorded samples, resetting the collector.
     */
    public void reset() {
        timeStamps.clear();
        activityRates.clear();
    }
}
