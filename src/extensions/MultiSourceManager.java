/**
 * @file src/extensions/MultiSourceManager.java
 * @brief Factory-like helper that constructs homogeneous or heterogeneous source pools.
 * @details The class centralizes creation of {@link model.TrafficSource}
 *          instances, supporting both Pareto ON/OFF sources with random
 *          parameter variation and {@link model.FGNTrafficSource} objects that
 *          leverage Fractional Gaussian Noise thresholds. Collaborates tightly
 *          with {@link model.SimulationParameters} to interpret requested
 *          traffic models. Outputs are mutable lists reused by
 *          {@link core.Simulator}.
 * @date 2024-05-30
 */
package extensions;

import model.FGNTrafficSource;
import model.SimulationParameters;
import model.TrafficSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @class MultiSourceManager
 * @brief Encapsulates construction strategies for multiple traffic sources.
 * @details Maintains an internally reused list to minimize allocations. Inputs
 *          are either explicit Pareto parameters or a
 *          {@link SimulationParameters} bundle for FGN. Outputs are lists of
 *          fully initialized {@link TrafficSource} implementations ready for
 *          scheduling.
 */
public class MultiSourceManager {

    /** Mutable list storing the latest generated sources. */
    private final List<TrafficSource> sources = new ArrayList<>();
    /** Random generator used to perturb baseline Pareto parameters. */
    private final Random random = new Random();

    /**
     * @brief Builds Pareto-based traffic sources with per-parameter variation.
     * @param count Number of sources to create.
     * @param baseOnShape Baseline ON-shape (alpha) parameter.
     * @param baseOnScale Baseline ON-scale parameter.
     * @param baseOffShape Baseline OFF-shape parameter.
     * @param baseOffScale Baseline OFF-scale parameter.
     * @param variation Relative variation applied to each parameter
     *                  (e.g., {@code 0.15 = ±15%}).
     * @return Mutable list of configured traffic sources.
     */
    public List<TrafficSource> generateSources(int count,
                                               double baseOnShape, double baseOnScale,
                                               double baseOffShape, double baseOffScale,
                                               double variation) {
        sources.clear();
        for (int i = 0; i < count; i++) {
            double onShape = vary(baseOnShape, variation);
            double onScale = vary(baseOnScale, variation);
            double offShape = vary(baseOffShape, variation);
            double offScale = vary(baseOffScale, variation);
            TrafficSource src = new TrafficSource(i, onShape, onScale, offShape, offScale);
            sources.add(src);
        }
        return sources;
    }

    /**
     * @brief Builds FGN-thresholded sources using the global parameters.
     * @param params Simulation configuration containing FGN settings such as
     *               Hurst exponent and threshold policies.
     * @return List of {@link FGNTrafficSource} instances.
     */
    public List<TrafficSource> generateFGNSources(SimulationParameters params) {
        sources.clear();
        for (int i = 0; i < params.numberOfSources; i++) {
            sources.add(new FGNTrafficSource(i, params));
        }
        return sources;
    }

    /**
     * @brief Randomly perturbs a base value by ±variation.
     * @param base Nominal parameter value.
     * @param variation Relative variation range.
     * @return Perturbed parameter bounded away from zero.
     */
    private double vary(double base, double variation) {
        double delta = (random.nextDouble() * 2 - 1) * variation; // ±variation
        return Math.max(0.0001, base * (1 + delta));
    }

    /**
     * @brief Exposes the last generated list of sources.
     * @return Mutable list that callers may further inspect.
     */
    public List<TrafficSource> getSources() {
        return sources;
    }
}
