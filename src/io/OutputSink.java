/**
 * @file src/io/OutputSink.java
 * @brief Interface describing how simulation artifacts are persisted.
 * @details Abstracts storage concerns away from {@link core.Simulator} and
 *          handler classes, enabling file, memory, or mock implementations.
 *          Collaborates primarily with {@link FileOutputManager}.
 * @date 2024-05-30
 */
package io;

import core.Event;
import core.StatisticsCollector;
import extensions.NetworkQueue;

/**
 * @interface OutputSink
 * @brief Contract for saving events, summaries, and FGN outputs.
 */
public interface OutputSink extends AutoCloseable {

    /**
     * @brief Provides the run directory where artifacts will be stored.
     * @return Absolute or relative folder path containing every artifact for the run.
     */
    String getRunDirectory();

    /**
     * @brief Records an event transition for traceability.
     * @param event Event being logged.
     */
    void logEvent(Event event);

    /**
     * @brief Persists simulator statistics and queue metrics.
     * @param stats Computed traffic statistics for the run.
     * @param queue Aggregate queue metrics captured alongside traffic stats.
     */
    void saveSummary(StatisticsCollector stats, NetworkQueue queue);

    /**
     * @brief Persists Fractional Gaussian Noise generation artifacts.
     * @param series Generated time-series samples.
     * @param H Target Hurst exponent used for generation.
     * @param sigma Standard deviation of the sequence.
     * @param samplingInterval Spacing between consecutive samples (seconds).
     * @param threshold Numeric threshold separating ON/OFF states for logging.
     */
    void saveFGNResults(double[] series, double H, double sigma,
                        double samplingInterval, double threshold);

    /**
     * @brief Ensures any buffered content is flushed and files are closed.
     */
    @Override
    void close();
}
