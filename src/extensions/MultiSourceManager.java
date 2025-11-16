package extensions;

import model.FGNTrafficSource;
import model.SimulationParameters;
import model.TrafficSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility responsible for constructing collections of {@link model.TrafficSource} objects.
 * <p>
 * Depending on the {@link model.SimulationParameters}, it can build either
 * classic Pareto ON/OFF sources or {@link model.FGNTrafficSource} instances.
 */
public class MultiSourceManager {

    private final List<TrafficSource> sources = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Builds a list of Pareto-based traffic sources with per-parameter variation.
     *
     * @param count         number of sources to create
     * @param baseOnShape   baseline ON-shape (alpha) parameter
     * @param baseOnScale   baseline ON-scale parameter
     * @param baseOffShape  baseline OFF-shape parameter
     * @param baseOffScale  baseline OFF-scale parameter
     * @param variation     relative variation applied to each parameter (e.g., 0.15 = ±15%)
     * @return mutable list of configured traffic sources
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
     * Builds FGN-thresholded sources using the global simulation parameters.
     *
     * @param params simulation configuration containing FGN settings
     * @return list of {@link FGNTrafficSource} instances
     */
    public List<TrafficSource> generateFGNSources(SimulationParameters params) {
        sources.clear();
        for (int i = 0; i < params.numberOfSources; i++) {
            sources.add(new FGNTrafficSource(i, params));
        }
        return sources;
    }

    /** Randomly perturbs a base value by ±variation. */
    private double vary(double base, double variation) {
        double delta = (random.nextDouble() * 2 - 1) * variation; // ±variation
        return Math.max(0.0001, base * (1 + delta));
    }

    /**
     * @return the last generated list of sources (mutable)
     */
    public List<TrafficSource> getSources() {
        return sources;
    }
}
