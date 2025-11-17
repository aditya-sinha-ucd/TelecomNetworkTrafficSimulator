/**
 * @file src/model/ParetoDistribution.java
 * @brief Pareto (Type I) heavy-tailed distribution used for ON/OFF durations.
 * @details Provides inverse-transform sampling for generating ON/OFF durations
 *          consumed by {@link model.TrafficSource} instances. The distribution
 *          is parameterized by shape α and scale β, matching textbook Pareto.
 */
package model;

import java.util.Random;

/**
 * @class ParetoDistribution
 * @brief Implements the Pareto (Type I) heavy-tailed distribution.
 */
public class ParetoDistribution extends Distribution {

    private final double shape;   // α (alpha)
    private final double scale;   // β (minimum value)
    private final Random random;

    /**
     * @brief Constructs a Pareto distribution with given parameters.
     * @param shape Shape parameter α (must be > 0).
     * @param scale Scale parameter β (must be > 0).
     */
    public ParetoDistribution(double shape, double scale) {
        if (shape <= 0 || scale <= 0)
            throw new IllegalArgumentException("Shape and scale must be positive.");
        this.shape = shape;
        this.scale = scale;
        this.random = new Random();
    }

    /**
        * @brief Generates a random sample using inverse transform sampling.
        * @return Random sample drawn from the Pareto distribution.
     */
    @Override
    public double sample() {
        double u = random.nextDouble();
        u = Math.max(u, 1e-12); // avoid division by zero
        return scale / Math.pow(u, 1.0 / shape);
    }

    /**
     * @brief Shape parameter α.
     * @return Alpha value used during construction.
     */
    public double getShape() {
        return shape;
    }

    /**
     * @brief Scale parameter β.
     * @return Minimum value parameter.
     */
    public double getScale() {
        return scale;
    }
}
