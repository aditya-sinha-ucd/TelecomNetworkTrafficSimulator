package util;

import java.util.Random;

/**
 * Generates Fractional Gaussian Noise (fGn) using the Davies–Harte method.
 * <p>
 * This class produces self-similar Gaussian processes with a specified
 * Hurst exponent H in (0.5, 1.0). It supports deterministic seeding for
 * reproducible results and follows OOP best practices for readability
 * and testability.
 */
public class FractionalGaussianNoise {

    private final double hurst;
    private final double sigma;
    private final double mean;
    private final Random rng;

    /**
     * Constructs a FractionalGaussianNoise generator.
     *
     * @param hurst Hurst exponent (0.5 < H < 1.0)
     * @param sigma Standard deviation (> 0)
     * @param mean  Mean value (center of distribution)
     * @param rng   Random generator (dependency-injected for reproducibility)
     */
    public FractionalGaussianNoise(double hurst, double sigma, double mean, Random rng) {
        validateParameters(hurst, sigma);
        this.hurst = hurst;
        this.sigma = sigma;
        this.mean = mean;
        this.rng = rng;
    }

    /** Alternate constructor for convenience with a seed. */
    public FractionalGaussianNoise(double hurst, double sigma, double mean, long seed) {
        this(hurst, sigma, mean, new Random(seed));
    }

    /**
     * Generates a fractional Gaussian noise sequence of length n.
     *
     * @param n number of samples to generate (>= 2)
     * @return double array of Gaussian samples
     */
    public double[] generate(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");

        int m = nextPowerOfTwo(2 * n);
        double[] covariance = buildCovarianceVector(n, m);
        double[] real = covariance.clone();
        double[] imag = new double[m];

        fft(real, imag, false);
        clipNegatives(real);

        double[] reNoise = new double[m];
        double[] imNoise = new double[m];
        buildComplexGaussian(real, reNoise, imNoise);

        fft(reNoise, imNoise, true); // inverse FFT (normalized)
        return scaleAndTrim(reNoise, n);
    }

    // -----------------------------
    // Internal Utility Methods
    // -----------------------------

    /** Validates input parameters. */
    private void validateParameters(double hurst, double sigma) {
        if (hurst <= 0.5 || hurst >= 1.0)
            throw new IllegalArgumentException("Hurst exponent must be in (0.5, 1.0)");
        if (sigma <= 0)
            throw new IllegalArgumentException("Sigma must be > 0");
    }

    /** Builds the circulant covariance vector for fGn. */
    private double[] buildCovarianceVector(int n, int m) {
        double[] c = new double[m];
        for (int k = 0; k < n; k++) c[k] = gamma(k);
        for (int k = n; k < m; k++) c[k] = gamma(2 * n - k);
        return c;
    }

    /** fGn autocovariance function. */
    private double gamma(int k) {
        if (k == 0) return 1.0;
        double kp1 = Math.pow(k + 1.0, 2.0 * hurst);
        double km1 = Math.pow(Math.abs(k - 1.0), 2.0 * hurst);
        double k0  = Math.pow(k, 2.0 * hurst);
        return 0.5 * (km1 - 2.0 * k0 + kp1);
    }

    /** Ensures no negative eigenvalues from FFT of covariance. */
    private void clipNegatives(double[] re) {
        for (int i = 0; i < re.length; i++) {
            if (re[i] < 0) re[i] = 0;
        }
    }

    /** Constructs conjugate-symmetric complex Gaussian vector. */
    private void buildComplexGaussian(double[] eigenvalues, double[] Ur, double[] Ui) {
        int m = eigenvalues.length;
        Ur[0] = Math.sqrt(eigenvalues[0]) * nextGaussian();
        Ui[0] = 0.0;

        if ((m & 1) == 0) {
            Ur[m / 2] = Math.sqrt(eigenvalues[m / 2]) * nextGaussian();
            Ui[m / 2] = 0.0;
        }

        int upper = (m & 1) == 0 ? m / 2 : (m / 2 + 1);
        for (int k = 1; k < upper; k++) {
            double lambda = eigenvalues[k];
            double s = Math.sqrt(lambda / 2.0);
            double a = nextGaussian();
            double b = nextGaussian();

            Ur[k] = s * a;
            Ui[k] = s * b;
            Ur[m - k] = s * a;
            Ui[m - k] = -s * b;
        }
    }

    /** Scales and trims the inverse FFT output. */
    private double[] scaleAndTrim(double[] data, int n) {
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = mean + sigma * data[i];
        return out;
    }

    /** Generates one standard normal sample. */
    private double nextGaussian() {
        double u1 = Math.max(1e-12, rng.nextDouble());
        double u2 = rng.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    /** Next power of two >= value. */
    private static int nextPowerOfTwo(int value) {
        int m = 1;
        while (m < value) m <<= 1;
        return m;
    }

    // -----------------------------
    // Internal FFT Implementation
    // -----------------------------

    /** In-place radix-2 complex FFT (forward or inverse). */
    private static void fft(double[] re, double[] im, boolean inverse) {
        int n = re.length;
        if (n <= 1) return;

        // bit reversal
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >>> 1;
            for (; (j & bit) != 0; bit >>>= 1) j &= ~bit;
            j |= bit;
            if (i < j) {
                double tr = re[i]; re[i] = re[j]; re[j] = tr;
                double ti = im[i]; im[i] = im[j]; im[j] = ti;
            }
        }

        // Cooley–Tukey FFT
        for (int len = 2; len <= n; len <<= 1) {
            double ang = 2.0 * Math.PI / len * (inverse ? -1.0 : 1.0);
            double wlenRe = Math.cos(ang);
            double wlenIm = Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                double wRe = 1.0, wIm = 0.0;
                for (int j = 0; j < len / 2; j++) {
                    int u = i + j;
                    int v = u + len / 2;

                    double vr = re[v] * wRe - im[v] * wIm;
                    double vi = re[v] * wIm + im[v] * wRe;

                    double ur = re[u];
                    double ui = im[u];

                    re[v] = ur - vr;  im[v] = ui - vi;
                    re[u] = ur + vr;  im[u] = ui + vi;

                    double newRe = wRe * wlenRe - wIm * wlenIm;
                    double newIm = wRe * wlenIm + wIm * wlenRe;
                    wRe = newRe;
                    wIm = newIm;
                }
            }
        }

        // Normalize if inverse FFT
        if (inverse) {
            for (int i = 0; i < n; i++) {
                re[i] /= n;
                im[i] /= n;
            }
        }
    }

    // -----------------------------
    // Getters
    // -----------------------------

    public double getHurst() { return hurst; }
    public double getSigma() { return sigma; }
    public double getMean() { return mean; }
}
