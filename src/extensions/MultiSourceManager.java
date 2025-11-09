package extensions;

import model.TrafficSource;
import model.ParetoDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages multiple TrafficSource objects with
 * different ON/OFF Pareto parameters.
 * <p>
 * This enables simulation of heterogeneous sources,
 * a bonus feature of the project.
 */
public class MultiSourceManager {

    private final List<TrafficSource> sources;
    private final Random random;

    /**
     * Creates a manager for multiple traffic sources.
     */
    public MultiSourceManager() {
        this.sources = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Generates traffic sources with varying ON/OFF parameters.
     *
     * @param count      number of sources to generate
     * @param baseOnShape  base shape for ON duration
     * @param baseOnScale  base scale for ON duration
     * @param baseOffShape base shape for OFF duration
     * @param baseOffScale base scale for OFF duration
     * @param variation    percentage variation (e.g., 0.2 = ±20%)
     * @return list of heterogeneous TrafficSource objects
     */
    public List<TrafficSource> generateSources(int count,
                                               double baseOnShape, double baseOnScale,
                                               double baseOffShape, double baseOffScale,
                                               double variation) {
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

    /** Helper to apply random variation to a base parameter. */
    private double vary(double base, double variation) {
        double delta = (random.nextDouble() * 2 - 1) * variation; // ±variation
        return Math.max(0.0001, base * (1 + delta));
    }

    /** @return all managed sources */
    public List<TrafficSource> getSources() {
        return sources;
    }
}
