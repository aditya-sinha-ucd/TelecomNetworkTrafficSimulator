package model;

import java.util.Random;

/**
 * Implements the Pareto (Type I) heavy-tailed distribution.
 * <p>
 * Probability Density Function:
 *   f(x) = (alpha * scale^alpha) / x^(alpha + 1),  for x >= scale
 * <p>
 * Used for modeling ON and OFF durations of traffic sources.
 */
public class ParetoDistribution extends Distribution {

    private final double shape;   // α (alpha)
    private final double scale;   // β (minimum value)
    private final Random random;

    /**
     * Constructs a Pareto distribution with given parameters.
     *
     * @param shape shape parameter α (must be > 0)
     * @param scale scale parameter β (must be > 0)
     */
    public ParetoDistribution(double shape, double scale) {
        if (shape <= 0 || scale <= 0)
            throw new IllegalArgumentException("Shape and scale must be positive.");
        this.shape = shape;
        this.scale = scale;
        this.random = new Random();
    }

    /**
     * Generates a random sample using inverse transform sampling.
     * Formula: X = β / (U)^(1/α), where U ~ Uniform(0,1)
     */
    @Override
    public double sample() {
        double u = random.nextDouble();
        u = Math.max(u, 1e-12); // avoid division by zero
        return scale / Math.pow(u, 1.0 / shape);
    }

    /** @return shape parameter α */
    public double getShape() {
        return shape;
    }

    /** @return scale parameter β */
    public double getScale() {
        return scale;
    }
}
