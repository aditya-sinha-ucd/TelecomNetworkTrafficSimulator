package core;

/**
 * Tracks and manages the current simulation time.
 */
public class SimulationClock {
    private double currentTime;

    public SimulationClock() {
        this.currentTime = 0.0;
    }

    public double getTime() {
        return currentTime;
    }

    public void setTime(double time) {
        this.currentTime = time;
    }

    public void reset() {
        this.currentTime = 0.0;
    }

    @Override
    public String toString() {
        return String.format("SimulationClock{time=%.3f}", currentTime);
    }
}
