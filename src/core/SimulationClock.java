package core;

/**
 * A simple utility class that tracks and manages the current time
 * of the simulation.
 *
 * <p>This class isolates the handling of simulation time from the
 * rest of the system. By centralizing time management, the simulator
 * can more easily support features like resets, multiple runs, or
 * time synchronization across components.</p>
 *
 * <p>Typical usage:
 * <pre>
 *     SimulationClock clock = new SimulationClock();
 *     clock.setTime(5.0);  // advance to t = 5.0s
 *     System.out.println(clock.getTime());  // prints 5.0
 * </pre>
 * </p>
 */
public class SimulationClock {
    /** The current simulation time (in seconds). */
    private double currentTime;

    /** Constructs a new simulation clock initialized to time = 0.0s. */
    public SimulationClock() {
        this.currentTime = 0.0;
    }

    /**
     * Retrieves the current simulation time.
     *
     * @return the current time in seconds
     */
    public double getTime() {
        return currentTime;
    }

    /**
     * Sets or updates the current simulation time.
     *
     * @param time the new time value (in seconds)
     */
    public void setTime(double time) {
        this.currentTime = time;
    }

    /**
     * Resets the simulation clock back to zero.
     * Useful for restarting simulations without
     * creating a new clock instance.
     */
    public void reset() {
        this.currentTime = 0.0;
    }


    /**
     * Returns a formatted string showing the current
     * simulation time (useful for debugging/logging).
     *
     * @return a string representation of the clock state
     */
    @Override
    public String toString() {
        return String.format("SimulationClock{time=%.3f}", currentTime);
    }
}
