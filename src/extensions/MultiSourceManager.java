package extensions;

import model.FGNTrafficSource;
import model.SimulationParameters;
import model.TrafficSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages a list of sources. Can build Pareto or FGN sources.
 */
public class MultiSourceManager {

    private final List<TrafficSource> sources = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Pareto sources with ±variation.
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
     * FGN sources (no Pareto). Parameters come from SimulationParameters.
     */
    public List<TrafficSource> generateFGNSources(SimulationParameters params) {
        sources.clear();
        for (int i = 0; i < params.numberOfSources; i++) {
            sources.add(new FGNTrafficSource(i, params));
        }
        return sources;
    }

    private double vary(double base, double variation) {
        double delta = (random.nextDouble() * 2 - 1) * variation; // ±variation
        return Math.max(0.0001, base * (1 + delta));
    }

    public List<TrafficSource> getSources() {
        return sources;
    }
}
