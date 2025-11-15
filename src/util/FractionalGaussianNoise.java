package util;

import java.util.Random;

/**
 * Fractional Gaussian Noise (fGn) generator using the Davies–Harte method.
 * <p>
 * Produces a Gaussian, long-range dependent sequence with Hurst exponent H in (0.5, 1.0).
 * The implementation follows good SWE/OOP practices:
 *  - clear parameter validation
 *  - dependency injection of Random (reproducible tests)
 *  - private helpers for each mathematical step
 *  - guards to avoid numerical NaNs
 */
public final class FractionalGaussianNoise {

    private final double hurst;
    private final double sigma;
    private final Random rng;

    /**
     * Preferred constructor: inject a Random for reproducibility and tests.
     *
     * @param hurst Hurst exponent (0.5 &lt; H &lt; 1.0)
     * @param sigma Standard deviation (&gt; 0)
     * @param rng   Random generator
     */
    public FractionalGaussianNoise(double hurst, double sigma, Random rng) {
        validateParameters(hurst, sigma);
        this.hurst = hurst;
        this.sigma = sigma;
        this.rng = rng;
    }

    /**
     * Convenience constructor with a seed.
     */
    public FractionalGaussianNoise(double hurst, double sigma, long seed) {
        this(hurst, sigma, new Random(seed));
    }

    /**
     * Generate an fGn sequence of length n.
     *
     * @param n number of samples (n &ge; 2)
     * @return array of length n containing the generated samples
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
     * Build the circulant covariance vector c[0..m-1] used by the Davies–Harte method.
     * We ensure:
     *  - c[k] = gamma(k) for 0 <= k < n
     *  - c[k] = gamma(2n - k) for n <= k <= 2n
     *  - c[k] = 0 for k > 2n (safe zero-padding, prevents NaNs)
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
     * fGn autocovariance for non-negative lag k:
     * gamma(k) = 0.5 * (|k-1|^{2H} - 2|k|^{2H} + |k+1|^{2H}).
     */
    private double gamma(int k) {
        if (k < 0) throw new IllegalArgumentException("gamma(k) requires k >= 0");
        if (k == 0) return 1.0;
        double kp1 = Math.pow(k + 1.0, 2.0 * hurst);
        double km1 = Math.pow(Math.abs(k - 1.0), 2.0 * hurst);
        double k0  = Math.pow(k, 2.0 * hurst);
        return 0.5 * (km1 - 2.0 * k0 + kp1);
    }

    private static void clipNegatives(double[] eigenvalues) {
        for (int i = 0; i < eigenvalues.length; i++) {
            if (eigenvalues[i] < 0.0) eigenvalues[i] = 0.0;
        }
    }

    /**
     * Fill (Ur, Ui) with a conjugate-symmetric complex Gaussian vector whose
     * per-frequency variance equals the eigenvalue at that bin.
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

    private double[] scaleAndTrim(double[] data, int n) {
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            // FGN has zero mean by definition.
            out[i] = sigma * data[i];
        }
        return out;
    }

    private double nextGaussian() {
        // Box–Muller with guard to avoid log(0)
        double u1 = Math.max(1e-12, rng.nextDouble());
        double u2 = rng.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    private static int nextPowerOfTwo(int value) {
        int m = 1;
        while (m < value) m <<= 1;
        return m;
    }

    // Minimal in-place radix-2 FFT (forward/inverse)


    /**
     * In-place complex FFT.
     *
     * @param re real part (modified in place)
     * @param im imag part (modified in place)
     * @param inverse true for inverse FFT (also normalizes by N)
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

        // Cooley–Tukey
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


    // Getters


    public double getHurst() { return hurst; }
    public double getSigma() { return sigma; }
}
