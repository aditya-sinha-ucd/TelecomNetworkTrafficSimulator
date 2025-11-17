package io;

import core.Event;
import core.StatisticsCollector;
import extensions.NetworkQueue;

/**
 * Small abstraction around simulation output destinations.
 * <p>
 * Allows the simulator and handlers to depend on an interface rather than the
 * concrete {@link FileOutputManager}, which simplifies testing and enables
 * alternate output strategies (e.g., in-memory or no-op sinks).
 */
public interface OutputSink extends AutoCloseable {

    /**
     * @return absolute or relative folder path containing every artifact for the run
     */
    String getRunDirectory();

    /** Records an event transition for traceability. */
    void logEvent(Event event);

    /**
     * Persists simulator statistics and queue metrics.
     *
     * @param stats computed traffic statistics for the run
     * @param queue aggregate queue metrics captured alongside traffic stats
     */
    void saveSummary(StatisticsCollector stats, NetworkQueue queue);

    /**
     * Persists Fractional Gaussian Noise generation artifacts.
     *
     * @param series generated time-series samples
     * @param H target Hurst exponent used for generation
     * @param sigma standard deviation of the sequence
     * @param samplingInterval spacing between consecutive samples (seconds)
     * @param threshold numeric threshold separating ON/OFF states for logging
     */
    void saveFGNResults(double[] series, double H, double sigma,
                        double samplingInterval, double threshold);

    /** Ensures any buffered content is flushed and files are closed. */
    @Override
    void close();
}
