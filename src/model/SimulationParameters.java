package model;

/**
 * Holds all configuration parameters for a simulation run.
 * <p>
 * Used to pass values between ConsoleUI, ConfigFileLoader, and Simulator.
 */
public class SimulationParameters {

    public double totalSimulationTime;
    public int numberOfSources;
    public double onShape, onScale;
    public double offShape, offScale;
    public double samplingInterval = 1.0;
    public long randomSeed = System.currentTimeMillis();

    public SimulationParameters() {}

    public SimulationParameters(double totalTime, int numSources,
                                double onShape, double onScale,
                                double offShape, double offScale) {
        this.totalSimulationTime = totalTime;
        this.numberOfSources = numSources;
        this.onShape = onShape;
        this.onScale = onScale;
        this.offShape = offShape;
        this.offScale = offScale;
    }

    /** Convenience builder for ConsoleUI input */
    public static SimulationParameters fromUserInput(double totalTime, int numSources,
                                                     double onShape, double onScale,
                                                     double offShape, double offScale) {
        return new SimulationParameters(totalTime, numSources, onShape, onScale, offShape, offScale);
    }

    @Override
    public String toString() {
        return String.format(
                "SimulationParameters [time=%.2fs, sources=%d, ON(alpha=%.2f, scale=%.2f), OFF(alpha=%.2f, scale=%.2f)]",
                totalSimulationTime, numberOfSources, onShape, onScale, offShape, offScale);
    }
}
