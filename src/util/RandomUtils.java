package util;

import java.util.Random;

/**
 * Utility class providing random number generation functions
 * for the simulator, including support for Pareto-distributed
 * samples which produce heavy-tailed ON/OFF durations.
 * <p>
 * Pareto distribution:
 * X = scale / (U^(1/shape))
 * where U ~ Uniform(0, 1)
 */
public final class RandomUtils {

    /** Shared Random instance for reproducibility. */
    private static final Random RANDOM = new Random();

    // Private constructor to prevent instantiation
    private RandomUtils() {}

    /**
     * Generates a sample from a Pareto distribution.
     *
     * @param shape the alpha (shape) parameter (> 1 recommended)
     * @param scale the scale (minimum) parameter (> 0)
     * @return a Pareto-distributed random sample
     */
    public static double samplePareto(double shape, double scale) {
        if (shape <= 0 || scale <= 0)
            throw new IllegalArgumentException("Shape and scale must be positive");

        double u = RANDOM.nextDouble(); // uniform (0, 1)
        // Avoid u = 0 to prevent infinite results
        u = Math.max(u, 1e-12);
        return scale / Math.pow(u, 1.0 / shape);
    }

    /**
     * Generates a uniformly distributed random double in [min, max].
     *
     * @param min lower bound
     * @param max upper bound
     * @return uniformly distributed random value
     */
    public static double uniform(double min, double max) {
        if (max <= min)
            throw new IllegalArgumentException("Max must be greater than min");

        return min + (max - min) * RANDOM.nextDouble();
    }

    /**
     * Sets a custom random seed for reproducible simulations.
     *
     * @param seed the seed value
     */
    public static void setSeed(long seed) {
        RANDOM.setSeed(seed);
    }
}
