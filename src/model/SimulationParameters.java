package model;

/**
 * Holds all configuration parameters for a simulation run.
 * <p>
 * This allows settings to be passed between the UI, file loader,
 * and simulator without using long parameter lists.
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

    @Override
    public String toString() {
        return String.format(
                "SimulationParameters [time=%.2fs, sources=%d, " +
                        "ON(alpha=%.2f, scale=%.2f), OFF(alpha=%.2f, scale=%.2f)]",
                totalSimulationTime, numberOfSources,
                onShape, onScale, offShape, offScale);
    }
}
