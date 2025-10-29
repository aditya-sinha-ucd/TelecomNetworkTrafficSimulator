package util;

import java.util.Arrays;
import java.util.Random;

/**
 * Fractional Gaussian Noise (FGN) generator using the Davies–Harte method.
 *
 * <p>FGN is a stationary Gaussian process with long-range dependence controlled by
 * the Hurst parameter H in (0, 1). When H &gt; 0.5 you get persistent, self-similar
 * behavior; H = 0.5 reduces to white noise.</p>
 *
 * <p>This class generates zero-mean, unit-variance FGN samples. Convenience methods
 * are provided to map the series into the [0,1] range for use as a multiplicative
 * rate modulator.</p>
 *
 * References:
 * - Davies, R.B., Harte, D.S. (1987) "Tests for Hurst effect.” Biometrika.
 */
public final class FGNGenerator {

    private FGNGenerator() {}

    /**
     * Generate n samples of zero-mean, unit-variance FGN with Hurst parameter H.
     * Uses a real-valued FFT-friendly Davies–Harte construction.
     *
     * @param n number of samples to generate (n >= 2)
     * @param H Hurst parameter in (0, 1)
     * @param seed RNG seed for reproducibility
     * @return double[n] FGN samples (mean ~ 0, variance ~ 1)
     */
    public static double[] generateFGN(int n, double H, long seed) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");
        if (!(H > 0.0 && H < 1.0)) throw new IllegalArgumentException("H must be in (0,1)");

        // Autocovariance of fractional Gaussian noise (increment process of fBm)
        // gamma(k) = 0.5 * ( |k+1|^{2H} - 2|k|^{2H} + |k-1|^{2H} )
        double[] gamma = new double[n];
        gamma[0] = 1.0; // normalized variance for FGN with unit variance
        for (int k = 1; k < n; k++) {
            gamma[k] = 0.5 * (Math.pow(k + 1, 2.0 * H) - 2.0 * Math.pow(k, 2.0 * H) + Math.pow(k - 1, 2.0 * H));
        }

        // Build the circulant embedding vector c of length 2(n-1)
        int m = 2 * (n - 1);
        double[] c = new double[m];
        c[0] = gamma[0];
        for (int k = 1; k < n; k++) c[k] = gamma[k];
        for (int k = n; k < m; k++) c[k] = gamma[m - k];

        // Real FFT of c to obtain eigenvalues (should be >= 0 for valid embedding)
        double[] lambda = realFFT(c); // returns power spectrum (non-normalized)
        for (int i = 0; i < lambda.length; i++) {
            if (lambda[i] < 0) lambda[i] = 0; // numerical safeguard
        }

        // Generate complex Gaussian vector with variances lambda/ (2m)
        Random rng = new Random(seed);
        double scale = Math.sqrt(1.0 / (2.0 * m));
        double[] Re = new double[m];
        double[] Im = new double[m];

        // Index 0 and Nyquist frequency are purely real
        Re[0] = Math.sqrt(lambda[0]) * gaussian(rng) * scale;
        Im[0] = 0.0;
        Re[m / 2] = Math.sqrt(lambda[m / 2]) * gaussian(rng) * scale;
        Im[m / 2] = 0.0;

        // Fill symmetric conjugate pairs
        for (int k = 1; k < m / 2; k++) {
            double r = gaussian(rng);
            double s = gaussian(rng);
            double a = Math.sqrt(lambda[k]) * scale;
            Re[k] = a * r;
            Im[k] = a * s;
            // conjugate symmetry
            Re[m - k] = Re[k];
            Im[m - k] = -Im[k];
        }

        // Inverse real FFT to time domain
        double[] z = inverseRealFFT(Re, Im); // length m

        // Take the first n samples -> Gaussian with target covariance
        double[] x = Arrays.copyOf(z, n);

        // Normalize to unit variance (due to accumulated numeric scaling)
        standardizeInPlace(x);
        return x;
    }

    /** Map a series linearly into [0,1]. If constant, returns 0.5 for all entries. */
    public static double[] toUnitInterval(double[] series) {
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (double v : series) { if (v < min) min = v; if (v > max) max = v; }
        double[] out = new double[series.length];
        if (max - min < 1e-12) {
            Arrays.fill(out, 0.5);
            return out;
        }
        double range = max - min;
        for (int i = 0; i < series.length; i++) {
            out[i] = (series[i] - min) / range;
        }
        return out;
    }

    // ----------------- helpers: FFT + stats -----------------

    private static double gaussian(Random rng) {
        // Box–Muller
        double u1 = Math.max(rng.nextDouble(), 1e-12);
        double u2 = rng.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    /** Real FFT using a naive Cooley–Tukey wrapper via complex DFT (OK for mid-size m). */
    private static double[] realFFT(double[] x) {
        int n = x.length;
        double[] Re = new double[n];
        double[] Im = new double[n];
        // compute complex DFT
        for (int k = 0; k < n; k++) {
            double sumRe = 0, sumIm = 0;
            for (int t = 0; t < n; t++) {
                double ang = -2.0 * Math.PI * k * t / n;
                sumRe += x[t] * Math.cos(ang);
                sumIm += x[t] * Math.sin(ang);
            }
            Re[k] = sumRe;
            Im[k] = sumIm;
        }
        // power spectrum (eigenvalues)
        double[] power = new double[n];
        for (int i = 0; i < n; i++) {
            power[i] = Re[i] * Re[i] + Im[i] * Im[i];
        }
        return power;
    }

    /** Inverse real FFT from symmetric complex spectrum (Re, Im). */
    private static double[] inverseRealFFT(double[] Re, double[] Im) {
        int n = Re.length;
        double[] x = new double[n];
        // inverse DFT
        for (int t = 0; t < n; t++) {
            double sumRe = 0, sumIm = 0;
            for (int k = 0; k < n; k++) {
                double ang = 2.0 * Math.PI * k * t / n;
                double r = Re[k], im = Im[k];
                sumRe += r * Math.cos(ang) - im * Math.sin(ang);
                sumIm += r * Math.sin(ang) + im * Math.cos(ang);
            }
            // Imaginary part should be ~0 for real signals
            x[t] = sumRe / n;
        }
        return x;
    }

    private static void standardizeInPlace(double[] x) {
        double mean = 0;
        for (double v : x) mean += v;
        mean /= x.length;
        double var = 0;
        for (double v : x) { double d = v - mean; var += d * d; }
        var /= Math.max(1, x.length - 1);
        double sd = (var > 0) ? Math.sqrt(var) : 1.0;
        for (int i = 0; i < x.length; i++) x[i] = (x[i] - mean) / sd;
    }
}
