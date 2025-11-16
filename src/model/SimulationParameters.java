package model;

/**
 * Holds all configuration options required by the {@link core.Simulator}.
 * <p>
 * The structure covers both the classic Pareto ON/OFF model and the optional
 * FGN-thresholded model, allowing callers to switch behavior with
 * {@link TrafficModel}.
 */
public class SimulationParameters {

    /** Total simulated time horizon in seconds. */
    public double totalSimulationTime;

    /** Number of traffic sources in the experiment. */
    public int numberOfSources;

    /** Pareto parameters for ON durations. */
    public double onShape, onScale;

    /** Pareto parameters for OFF durations. */
    public double offShape, offScale;

    /** Interval (seconds) between samples recorded by {@link core.StatisticsCollector}. */
    public double samplingInterval = 1.0;

    /** Optional seed for reproducible randomness. */
    public long randomSeed = System.currentTimeMillis();

    /** Chooses the underlying traffic model. */
    public enum TrafficModel {
        PARETO_ON_OFF,
        FGN_THRESHOLD
    }
    public TrafficModel trafficModel = TrafficModel.PARETO_ON_OFF;

    /** Hurst exponent used when {@link TrafficModel#FGN_THRESHOLD} is active. */
    public double hurst = 0.80;        // 0.5..1

    /** Standard deviation for the generated FGN process. */
    public double fgnSigma = 1.0;

    /** Threshold applied to the FGN sequence to derive ON/OFF states. */
    public double fgnThreshold = 0.0;  // 0 gives ~50% ON for the zero-mean FGN process

    /** Per-source seed base for the FGN generator. */
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

    /** Convenience factory mirroring the UI prompts. */
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
