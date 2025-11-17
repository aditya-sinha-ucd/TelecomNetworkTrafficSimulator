/**
 * @file src/util/RandomUtils.java
 * @brief Shared random helper functions used by the simulator.
 * @details Provides Pareto sampling and uniform draws needed by
 *          {@link model.TrafficSource} initialization and other modules.
 *          Centralizing randomness simplifies seeding for reproducibility.
 */
package util;

import java.util.Random;

/**
 * @class RandomUtils
 * @brief Utility class providing random number generation functions for the simulator.
 */
public final class RandomUtils {

    /** Shared Random instance for reproducibility. */
    private static final Random RANDOM = new Random();

    // Private constructor to prevent instantiation
    private RandomUtils() {}

    /**
     * @brief Generates a sample from a Pareto distribution.
     * @param shape Alpha (shape) parameter (> 0).
     * @param scale Scale (minimum) parameter (> 0).
     * @return Pareto-distributed random sample.
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
     * @brief Generates a uniformly distributed random double in [min, max].
     * @param min Lower bound.
     * @param max Upper bound.
     * @return Uniformly distributed random value.
     */
    public static double uniform(double min, double max) {
        if (max <= min)
            throw new IllegalArgumentException("Max must be greater than min");

        return min + (max - min) * RANDOM.nextDouble();
    }

    /**
     * @brief Sets a custom random seed for reproducible simulations.
     * @param seed Seed value applied to the shared RNG.
     */
    public static void setSeed(long seed) {
        RANDOM.setSeed(seed);
    }
}
