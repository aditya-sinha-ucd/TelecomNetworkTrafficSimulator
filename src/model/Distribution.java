/**
 * @file src/model/Distribution.java
 * @brief Abstract base class for probability distributions used by sources.
 * @details Establishes the contract satisfied by concrete distributions such as
 *          {@link model.ParetoDistribution}. Collaborators include
 *          {@link model.TrafficSource}, which relies on {@link #sample()} to
 *          determine ON/OFF durations.
 */
package model;

/**
 * @class Distribution
 * @brief Defines the sampling interface for all supported distributions.
 */
public abstract class Distribution {

    /**
     * @brief Generates a random sample from this distribution.
     * @return Randomly sampled positive value.
     */
    public abstract double sample();
}
