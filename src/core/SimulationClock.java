/**
 * @file src/core/SimulationClock.java
 * @brief Shared notion of simulation time across the core subsystems.
 * @details The clock is advanced monotonically by {@link core.Simulator} as it
 *          processes {@link core.Event} objects from the {@link core.EventQueue}.
 *          Other collaborators such as {@link core.StatisticsCollector} consult
 *          the clock to compute durations, rates, and time-weighted averages.
 *          Inputs consist of the next event time, while outputs are time reads
 *          through {@link #getTime()}.
 * @date 2024-05-30
 */
package core;

/**
 * @class SimulationClock
 * @brief Maintains and exposes the current simulation timestamp.
 * @details Provides deterministic timekeeping for queues, collectors, and
 *          estimators. It enforces monotonic advancement, ensuring causality in
 *          the discrete-event simulation.
 */
public class SimulationClock {

    /** Current simulation time in seconds. */
    private double currentTime;

    /**
     * @brief Creates a clock initialized to time zero.
     */
    public SimulationClock() {
        this.currentTime = 0.0;
    }

    /**
     * @brief Reads the present simulation time.
     * @return Current simulation timestamp in seconds.
     */
    public double getTime() {
        return currentTime;
    }

    /**
     * @brief Advances the clock to the specified time if it is not in the past.
     * @param newTime Next event time supplied by {@link core.EventQueue}.
     */
    public void advanceTo(double newTime) {
        if (newTime >= currentTime)
            currentTime = newTime;
    }

    /**
     * @brief Resets the clock to its initial state.
     */
    public void reset() {
        currentTime = 0.0;
    }

    /**
     * @brief Produces a formatted representation for logging contexts.
     * @return String with millisecond precision of {@link #currentTime}.
     */
    @Override
    public String toString() {
        return String.format("%.3f", currentTime);
    }
}
