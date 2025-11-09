package core;

import util.MathUtils;
import util.HurstEstimator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects and computes key simulation statistics over time.
 * <p>
 * The simulator records:
 *  - Aggregate rate (fraction of active ON sources)
 *  - Time stamps of samples
 *  - Summary stats (mean, peak, std deviation)
 * <p>
 * Provides methods to export time-series and summaries for external plotting.
 */
public class StatisticsCollector {

    private final List<Double> timeStamps;
    private final List<Double> activityRates;
    private final double sampleInterval;

    public StatisticsCollector(double sampleInterval) {
        this.sampleInterval = sampleInterval;
        this.timeStamps = new ArrayList<>();
        this.activityRates = new ArrayList<>();
    }

    /** Records a new sample of the aggregate traffic rate. */
    public void recordSample(double time, double aggregateRate) {
        timeStamps.add(time);
        activityRates.add(aggregateRate);
    }

    /** @return mean of all recorded activity rates. */
    public double getAverageRate() {
        return MathUtils.mean(activityRates);
    }

    /** @return maximum (peak) activity rate recorded. */
    public double getPeakRate() {
        return MathUtils.max(activityRates);
    }

    /** @return standard deviation of activity rates. */
    public double getStdDevRate() {
        return MathUtils.stdDev(activityRates);
    }

    /** @return total number of recorded samples. */
    public int getSampleCount() {
        return activityRates.size();
    }

    /** @return list of recorded activity rates (copy). */
    public List<Double> getActivityRates() {
        return new ArrayList<>(activityRates);
    }

    /** Estimates Hurst exponent of the activity series. */
    public double getHurstExponent() {
        if (activityRates.size() < 10) return 0.0;
        return HurstEstimator.estimateHurst(activityRates);
    }

    /** Prints formatted summary to console. */
    public void printSummary() {
        System.out.println("\n=== Simulation Summary ===");
        System.out.printf("Samples recorded: %d%n", getSampleCount());
        System.out.printf("Average rate: %.4f%n", getAverageRate());
        System.out.printf("Peak rate: %.4f%n", getPeakRate());
        System.out.printf("Std deviation: %.4f%n", getStdDevRate());
        System.out.printf("Estimated Hurst exponent: %.3f%n", getHurstExponent());
        System.out.println("===========================\n");
    }

    /** Writes recorded time-series data to a CSV file (for plotting). */
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

    /** Exports summary statistics to a text file (human-readable). */
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

    /** Appends summary stats to CSV (for batch experiments). */
    public void appendSummaryToCSV(String fileName) {
        try (FileWriter writer = new FileWriter(fileName, true)) {
            writer.write(String.format("%.4f,%.4f,%.4f,%.4f,%d%n",
                    getAverageRate(), getPeakRate(),
                    getStdDevRate(), getHurstExponent(), getSampleCount()));
        } catch (IOException e) {
            System.err.println("Error appending summary CSV: " + e.getMessage());
        }
    }

    /** Clears all recorded samples. */
    public void reset() {
        timeStamps.clear();
        activityRates.clear();
    }
}
