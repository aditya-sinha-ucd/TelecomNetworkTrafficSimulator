/**
 * @file src/model/SimulationParameters.java
 * @brief Mutable configuration DTO consumed by {@link core.Simulator}.
 * @details Captures both Pareto and FGN-specific options, enabling the console
 *          UI to toggle between models via {@link TrafficModel}. Inputs come
 *          from {@link io.ConsolePrompter} or configuration files; outputs are
 *          read-only views accessed by simulation components.
 */
package model;

/**
 * @class SimulationParameters
 * @brief Encapsulates all configuration options required by the simulator.
 * @details Provides convenience constructors and sensible defaults so UI code
 *          can populate it incrementally. Serves as the primary input to the
 *          {@link core.Simulator} constructor.
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

    /**
     * @brief Creates an empty parameter bag to be populated manually.
     */
    public SimulationParameters() {}

    /**
     * @brief Populates key Pareto settings via constructor arguments.
     * @param totalTime Total simulated time horizon in seconds.
     * @param numSources Number of traffic sources in the experiment.
     * @param onShape Pareto shape for ON durations.
     * @param onScale Pareto scale for ON durations.
     * @param offShape Pareto shape for OFF durations.
     * @param offScale Pareto scale for OFF durations.
     */
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

    /**
     * @brief Convenience factory mirroring the UI prompts.
     * @return Populated {@link SimulationParameters} with Pareto inputs set.
     */
    public static SimulationParameters fromUserInput(double totalTime, int numSources,
                                                     double onShape, double onScale,
                                                     double offShape, double offScale) {
        return new SimulationParameters(totalTime, numSources, onShape, onScale, offShape, offScale);
    }

    /**
     * @brief Produces a descriptive string containing key parameter values.
     * @return Readable representation of the configuration.
     */
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
