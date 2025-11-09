package model;

/**
 * Abstract base class for probability distributions.
 * <p>
 * Defines the interface for all distribution types used
 * in the simulator (e.g., Pareto, Exponential, etc.).
 */
public abstract class Distribution {

    /**
     * Generates a random sample from this distribution.
     *
     * @return a randomly sampled value (must be positive)
     */
    public abstract double sample();
}
