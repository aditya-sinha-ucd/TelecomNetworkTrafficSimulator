package model;

/**
 * Simulation parameters.
 * Keep the existing fields and add a simple switch for the FGN option.
 */
public class SimulationParameters {

    // existing
    public double totalSimulationTime;
    public int numberOfSources;
    public double onShape, onScale;
    public double offShape, offScale;
    public double samplingInterval = 1.0;
    public long randomSeed = System.currentTimeMillis();

    // NEW: choose traffic model
    public enum TrafficModel {
        PARETO_ON_OFF,
        FGN_THRESHOLD
    }
    public TrafficModel trafficModel = TrafficModel.PARETO_ON_OFF;

    // NEW: FGN parameters
    public double hurst = 0.80;        // 0.5..1
    public double fgnSigma = 1.0;
    public double fgnThreshold = 0.0;  // 0 gives ~50% ON for mean 0
    public long   fgnSeed = 42L;

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

    /** Convenience */
    public static SimulationParameters fromUserInput(double totalTime, int numSources,
                                                     double onShape, double onScale,
                                                     double offShape, double offScale) {
        return new SimulationParameters(totalTime, numSources, onShape, onScale, offShape, offScale);
    }

    @Override
    public String toString() {
        String base = String.format(
                "SimulationParameters [time=%.2fs, sources=%d, ON(alpha=%.2f, scale=%.2f), OFF(alpha=%.2f, scale=%.2f), dt=%.3f]",
                totalSimulationTime, numberOfSources, onShape, onScale, offShape, offScale, samplingInterval);
        if (trafficModel == TrafficModel.FGN_THRESHOLD) {
            base += String.format(" | FGN(H=%.2f, sigma=%.2f, thr=%.2f)", hurst, fgnSigma, fgnThreshold);
        } else {
            base += " | Pareto";
        }
        return base;
    }
}
