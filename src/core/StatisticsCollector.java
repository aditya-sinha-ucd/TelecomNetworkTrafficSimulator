package core;

import util.MathUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects and computes key simulation statistics over time.
 * <p>
 * The simulator will record metrics such as:
 *  - Aggregate rate (fraction of active ON sources)
 *  - Time stamps corresponding to samples
 *  - Summary statistics (mean, peak, standard deviation)
 * <p>
 * This class also provides functionality to export time-series data
 * to a CSV file for external analysis or plotting.
 */
public class StatisticsCollector {

    // List of sampled simulation times.
    private final List<Double> timeStamps;

    // List of aggregate activity rates at each sampled time.
    private final List<Double> activityRates;

    // The fixed sampling interval used to record statistics (seconds).
    private final double sampleInterval;

    /* Constructs a new StatisticsCollector with a specified sampling rate. */
    public StatisticsCollector(double sampleInterval) {
        this.sampleInterval = sampleInterval;
        this.timeStamps = new ArrayList<>();
        this.activityRates = new ArrayList<>();
    }

    /**
     * Records a new sample of the current aggregate traffic rate.
     *
     * @param time          the current simulation time
     * @param aggregateRate the proportion of ON sources (0–1)
     */
    public void recordSample(double time, double aggregateRate) {
        timeStamps.add(time);
        activityRates.add(aggregateRate);
    }

    /** @return average (mean) of all recorded activity rates. */
    public double getAverageRate() {
        return MathUtils.mean(activityRates);
    }

    /** @return maximum (peak) activity rate recorded. */
    public double getPeakRate() {
        return MathUtils.max(activityRates);
    }

    /** @return standard deviation of the activity rates. */
    public double getStdDevRate() {
        return MathUtils.stdDev(activityRates);
    }

    /** @return total number of samples collected. */
    public int getSampleCount() {
        return activityRates.size();
    }

    /** @return list of all recorded activity rates (for analysis). */
    public List<Double> getActivityRates() {
        return new ArrayList<>(activityRates);
    }

    /**
     * Prints a formatted summary of the collected statistics to the console.
     */
    public void printSummary() {
        System.out.println("\n=== Simulation Summary ===");
        System.out.printf("Samples recorded: %d%n", getSampleCount());
        System.out.printf("Average rate: %.4f%n", getAverageRate());
        System.out.printf("Peak rate: %.4f%n", getPeakRate());
        System.out.printf("Std deviation: %.4f%n", getStdDevRate());
        System.out.println("===========================\n");
    }

    /**
     * Writes the recorded time-series data to a CSV file.
     *
     * @param fileName path of the file to write to (e.g., output/traffic_data.csv)
     */
    public void exportToCSV(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("Time,AggregateRate\n");
            for (int i = 0; i < timeStamps.size(); i++) {
                writer.write(String.format("%.4f,%.6f%n", timeStamps.get(i), activityRates.get(i)));
            }
            System.out.println("Time-series data exported to: " + fileName);
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }

    /**
     * Clears all recorded samples. Useful if the simulation is re-run.
     */
    public void reset() {
        timeStamps.clear();
        activityRates.clear();
    }
}
