package core;

/**
 * Manages the current simulation time and provides
 * a consistent time reference for all components.
 * <p>
 * This class allows multiple modules (e.g., queues,
 * statistics, estimators) to stay synchronized.
 */
public class SimulationClock {

    /** The current simulation time in seconds. */
    private double currentTime;

    /** Creates a new simulation clock starting at time 0. */
    public SimulationClock() {
        this.currentTime = 0.0;
    }

    /** @return the current simulation time */
    public double getTime() {
        return currentTime;
    }

    /**
     * Advances the simulation clock to a new time.
     * Only moves forward, never backward.
     *
     * @param newTime the next event time
     */
    public void advanceTo(double newTime) {
        if (newTime >= currentTime)
            currentTime = newTime;
    }

    /** Resets the simulation clock to zero. */
    public void reset() {
        currentTime = 0.0;
    }

    /** Returns a formatted string for logging. */
    @Override
    public String toString() {
        return String.format("%.3f", currentTime);
    }
}
