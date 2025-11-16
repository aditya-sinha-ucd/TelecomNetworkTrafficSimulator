package model;

/**
 * Holds the configuration required to generate a Fractional Gaussian Noise
 * time-series from the console or a configuration file.
 */
public class FGNGenerationParameters {

    private final double hurst;
    private final double sigma;
    private final int sampleCount;
    private final double samplingInterval;
    private final double threshold;
    private final long seed;

    /**
     * @param hurst             Hurst exponent for the generated process
     * @param sigma             standard deviation
     * @param sampleCount       number of samples to generate
     * @param samplingInterval  spacing between samples (seconds)
     * @param threshold         value used to derive ON/OFF states
     * @param seed              random seed for reproducibility
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

    /** @return desired Hurst exponent */
    public double getHurst() {
        return hurst;
    }

    /** @return standard deviation */
    public double getSigma() {
        return sigma;
    }

    /** @return number of samples to produce */
    public int getSampleCount() {
        return sampleCount;
    }

    /** @return spacing between samples */
    public double getSamplingInterval() {
        return samplingInterval;
    }

    /** @return threshold applied when deriving ON/OFF states */
    public double getThreshold() {
        return threshold;
    }

    /** @return random seed */
    public long getSeed() {
        return seed;
    }
}
