/**
 * @file src/model/FGNGenerationParameters.java
 * @brief Immutable value object representing FGN generator settings.
 * @details Consumed by {@link io.FGNSimulationHandler} and
 *          {@link util.FractionalGaussianNoise} to derive sample sequences.
 *          Encapsulates Hurst exponent, variance, sampling cadence, ON/OFF
 *          threshold, and RNG seed.
 */
package model;

/**
 * @class FGNGenerationParameters
 * @brief Stores the configuration required to generate a Fractional Gaussian Noise time-series.
 */
public class FGNGenerationParameters {

    private final double hurst;
    private final double sigma;
    private final int sampleCount;
    private final double samplingInterval;
    private final double threshold;
    private final long seed;

    /**
     * @brief Builds a parameter bundle populated with generator settings.
     * @param hurst Hurst exponent for the generated process.
     * @param sigma Standard deviation.
     * @param sampleCount Number of samples to generate.
     * @param samplingInterval Spacing between samples (seconds).
     * @param threshold Value used to derive ON/OFF states.
     * @param seed Random seed for reproducibility.
     */
    public FGNGenerationParameters(double hurst, double sigma,
                                   int sampleCount, double samplingInterval,
                                   double threshold, long seed) {
        this.hurst = hurst;
        this.sigma = sigma;
        this.sampleCount = sampleCount;
        this.samplingInterval = samplingInterval;
        this.threshold = threshold;
        this.seed = seed;
    }

    /**
     * @brief Desired Hurst exponent.
     * @return Target H value.
     */
    public double getHurst() {
        return hurst;
    }

    /**
     * @brief Standard deviation of the generated series.
     * @return Sigma value.
     */
    public double getSigma() {
        return sigma;
    }

    /**
     * @brief Number of samples to produce.
     * @return Positive sample count.
     */
    public int getSampleCount() {
        return sampleCount;
    }

    /**
     * @brief Spacing between samples.
     * @return Sampling interval in seconds.
     */
    public double getSamplingInterval() {
        return samplingInterval;
    }

    /**
     * @brief Threshold applied when deriving ON/OFF states.
     * @return Numeric threshold.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @brief Random seed for reproducibility.
     * @return Seed value.
     */
    public long getSeed() {
        return seed;
    }

    /**
     * @brief Computes the total duration covered by the generated samples.
     * @return Total time horizon ({@code samples * samplingInterval}).
     */
    public double getTotalDuration() {
        return sampleCount * samplingInterval;
    }
}
