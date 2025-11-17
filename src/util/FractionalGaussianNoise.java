/**
 * @file src/util/FractionalGaussianNoise.java
 * @brief Fractional Gaussian Noise (fGn) generator using the Davies–Harte method.
 * @details Produces Gaussian, long-range dependent sequences with configurable
 *          Hurst exponent and standard deviation. Emphasizes reproducibility via
 *          dependency injection of {@link java.util.Random} and guards against
 *          numerical instabilities in FFT computations.
 */
package util;

import java.util.Random;

/**
 * @class FractionalGaussianNoise
 * @brief Generates Fractional Gaussian Noise sequences for the simulator.
 */
public final class FractionalGaussianNoise {

    private final double hurst;
    private final double sigma;
    private final Random rng;

    /**
     * @brief Preferred constructor that injects a {@link Random} for reproducibility.
     * @param hurst Hurst exponent (0.5 &lt; H &lt; 1.0).
     * @param sigma Standard deviation (&gt; 0).
     * @param rng Random generator used internally.
     */
    public FractionalGaussianNoise(double hurst, double sigma, Random rng) {
        validateParameters(hurst, sigma);
        this.hurst = hurst;
        this.sigma = sigma;
        this.rng = rng;
    }

    /**
     * @brief Convenience constructor that seeds an internal {@link Random}.
     */
    public FractionalGaussianNoise(double hurst, double sigma, long seed) {
        this(hurst, sigma, new Random(seed));
    }

    /**
     * @brief Generates an fGn sequence of length {@code n}.
     * @param n Number of samples (n &ge; 2).
     * @return Array containing the generated samples.
     */
    public double[] generate(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");

        // FFT length m = next power of two >= 2n
        final int m = nextPowerOfTwo(2 * n);

        // Build circulant covariance vector (safe for all m >= 2n)
        double[] c = buildCovarianceVector(n, m);

        // Eigenvalues via FFT of covariance
        double[] re = c.clone();
        double[] im = new double[m];
        fft(re, im, false);

        // Clip tiny negative eigenvalues due to numerical noise
        clipNegatives(re);

        // Draw complex Gaussian with conjugate symmetry and variances = eigenvalues
        double[] Ur = new double[m];
        double[] Ui = new double[m];
        buildComplexGaussian(re, Ur, Ui);

        // IFFT to obtain real sequence with desired covariance
        fft(Ur, Ui, true); // normalized in-place

        // Scale to sigma, add mean, and trim to length n
        return scaleAndTrim(Ur, n);
    }

    // ---------------------------------------------------------------------
    // Validation and parameter helpers


    /**
     * @brief Validates Hurst and sigma parameters.
     */
    private static void validateParameters(double hurst, double sigma) {
        if (hurst <= 0.5 || hurst >= 1.0) {
            throw new IllegalArgumentException("Hurst exponent must be in (0.5, 1.0)");
        }
        if (sigma <= 0.0) {
            throw new IllegalArgumentException("Sigma must be > 0");
        }
    }

    // ---------------------------------------------------------------------
    // Davies–Harte components

    /**
     * @brief Builds the circulant covariance vector used by the Davies–Harte method.
     */
    private double[] buildCovarianceVector(int n, int m) {
        double[] c = new double[m];

        // First half: direct autocovariance values
        for (int k = 0; k < n; k++) {
            c[k] = gamma(k);
        }

        // Mirror part up to 2n, then zero-pad
        for (int k = n; k < m; k++) {
            int t = 2 * n - k;
            if (t >= 0) {
                c[k] = gamma(t);
            } else {
                c[k] = 0.0; // zero padding beyond 2n keeps eigenvalues valid
            }
        }
        return c;
    }

    /**
     * @brief fGn autocovariance for non-negative lag k.
     */
    private double gamma(int k) {
        if (k < 0) throw new IllegalArgumentException("gamma(k) requires k >= 0");
        if (k == 0) return 1.0;
        double kp1 = Math.pow(k + 1.0, 2.0 * hurst);
        double km1 = Math.pow(Math.abs(k - 1.0), 2.0 * hurst);
        double k0  = Math.pow(k, 2.0 * hurst);
        return 0.5 * (km1 - 2.0 * k0 + kp1);
    }

    /**
     * @brief Clamps tiny negative eigenvalues introduced by numerical noise.
     */
    private static void clipNegatives(double[] eigenvalues) {
        for (int i = 0; i < eigenvalues.length; i++) {
            if (eigenvalues[i] < 0.0) eigenvalues[i] = 0.0;
        }
    }

    /**
     * @brief Fills (Ur, Ui) with a conjugate-symmetric complex Gaussian vector.
     */
    private void buildComplexGaussian(double[] eigenvalues, double[] Ur, double[] Ui) {
        int m = eigenvalues.length;

        // DC and Nyquist (purely real)
        Ur[0] = Math.sqrt(eigenvalues[0]) * nextGaussian();
        Ui[0] = 0.0;

        if ((m & 1) == 0) { // even length -> Nyquist bin
            Ur[m / 2] = Math.sqrt(eigenvalues[m / 2]) * nextGaussian();
            Ui[m / 2] = 0.0;
        }

        // Positive frequencies (1..upper-1), mirror to negatives
        int upper = ((m & 1) == 0) ? (m / 2) : (m / 2 + 1);
        for (int k = 1; k < upper; k++) {
            double lam = eigenvalues[k];
            double s = Math.sqrt(lam / 2.0);
            double a = nextGaussian();
            double b = nextGaussian();

            double rk = s * a;
            double ik = s * b;

            Ur[k] = rk;          Ui[k] = ik;
            Ur[m - k] = rk;      Ui[m - k] = -ik; // conjugate symmetry
        }
    }

    /**
     * @brief Scales the inverse FFT result to σ and trims to length n.
     */
    private double[] scaleAndTrim(double[] data, int n) {
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            // FGN has zero mean by definition.
            out[i] = sigma * data[i];
        }
        return out;
    }

    /**
     * @brief Samples a standard normal variate via Box–Muller.
     */
    private double nextGaussian() {
        // Box–Muller with guard to avoid log(0)
        double u1 = Math.max(1e-12, rng.nextDouble());
        double u2 = rng.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    /**
     * @brief Computes the next power of two greater than or equal to {@code value}.
     */
    private static int nextPowerOfTwo(int value) {
        int m = 1;
        while (m < value) m <<= 1;
        return m;
    }

    // Minimal in-place radix-2 FFT (forward/inverse)


    /**
     * @brief In-place complex FFT (Cooley–Tukey radix-2).
     * @param re Real part (modified in place).
     * @param im Imag part (modified in place).
     * @param inverse {@code true} for inverse FFT (also normalizes by N).
     */
    private static void fft(double[] re, double[] im, boolean inverse) {
        int n = re.length;
        if (n <= 1) return;

        // Bit reversal
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
                int half = len >>> 1;

                for (int j = 0; j < half; j++) {
                    int u = i + j;
                    int v = u + half;

                    double vr = re[v] * wRe - im[v] * wIm;
                    double vi = re[v] * wIm + im[v] * wRe;

                    double ur = re[u];
                    double ui = im[u];

                    re[v] = ur - vr;  im[v] = ui - vi;
                    re[u] = ur + vr;  im[u] = ui + vi;

                    double nwRe = wRe * wlenRe - wIm * wlenIm;
                    double nwIm = wRe * wlenIm + wIm * wlenRe;
                    wRe = nwRe; wIm = nwIm;
                }
            }
        }

        // Normalize for inverse
        if (inverse) {
            for (int i = 0; i < n; i++) {
                re[i] /= n;
                im[i] /= n;
            }
        }
    }


    /**
     * @brief Hurst exponent configured for this generator.
     * @return H parameter.
     */
    public double getHurst() { return hurst; }

    /**
     * @brief Standard deviation configured for this generator.
     * @return σ parameter.
     */
    public double getSigma() { return sigma; }
}
