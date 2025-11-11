package extensions;

import model.TrafficSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a heterogeneous set of TrafficSources using per-source ON/OFF parameters.
 * This version uses your existing 5-argument TrafficSource constructor:
 *   TrafficSource(int id, double onShape, double onScale, double offShape, double offScale)
 */
public class MultiSourceManager {

    /** Simple holder for one source's ON/OFF Pareto parameters (and optional seed placeholder). */
    public static class SourceSpec {
        public final double onShape, onScale;
        public final double offShape, offScale;
        public final Long seed; // not used in this simple version (kept for future reproducibility)

        public SourceSpec(double onShape, double onScale, double offShape, double offScale, Long seed) {
            this.onShape = onShape;
            this.onScale = onScale;
            this.offShape = offShape;
            this.offScale = offScale;
            this.seed = seed;
        }
    }

    /**
     * Create a list of TrafficSource instances from the provided specs.
     * Uses the 5-arg constructor that your TrafficSource currently exposes.
     */
    public List<TrafficSource> createSources(List<SourceSpec> specs) {
        List<TrafficSource> result = new ArrayList<>(specs.size());
        for (int i = 0; i < specs.size(); i++) {
            SourceSpec s = specs.get(i);
            // IMPORTANT: Use the 5-arg constructor that exists in your project right now.
            TrafficSource src = new TrafficSource(i, s.onShape, s.onScale, s.offShape, s.offScale);
            result.add(src);
        }
        return result;
    }
}
