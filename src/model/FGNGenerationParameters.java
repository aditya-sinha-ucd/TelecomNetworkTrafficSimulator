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

    public double getHurst() {
        return hurst;
    }

    public double getSigma() {
        return sigma;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public double getSamplingInterval() {
        return samplingInterval;
    }

    public double getThreshold() {
        return threshold;
    }

    public long getSeed() {
        return seed;
    }
}
