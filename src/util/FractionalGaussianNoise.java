package util;

import java.util.Random;

/**
 * Fractional Gaussian Noise generator (Davies–Harte style).
 * Enough for our use: generate a long Gaussian series with H in (0.5,1).
 */
public final class FractionalGaussianNoise {

    private final double H;
    private final double sigma;
    private final double mean;
    private final Random rng;

    public FractionalGaussianNoise(double H, double sigma, double mean, long seed) {
        if (H <= 0.5 || H >= 1.0) throw new IllegalArgumentException("H must be in (0.5,1)");
        if (sigma <= 0) throw new IllegalArgumentException("sigma must be > 0");
        this.H = H;
        this.sigma = sigma;
        this.mean = mean;
        this.rng = new Random(seed);
    }

    /** Make n samples. */
    public double[] generate(int n) {
        if (n < 2) throw new IllegalArgumentException("n must be >= 2");

        // Circulant first row from fGn autocovariance.
        int m = 1;
        while (m < 2 * n) m <<= 1; // power-of-two >= 2n

        double[] c = new double[m];
        for (int k = 0; k <= n - 1; k++) c[k] = gamma(k);
        for (int k = n; k < m; k++) c[k] = gamma(2 * n - k);

        // FFT → eigenvalues (clip tiny negatives).
        double[] re = new double[m];
        double[] im = new double[m];
        System.arraycopy(c, 0, re, 0, m);
        fft(re, im, false);
        for (int i = 0; i < m; i++) if (re[i] < 0) re[i] = 0;

        // Complex Gaussian with var = eigenvalue, conjugate symmetric.
        double[] Ur = new double[m];
        double[] Ui = new double[m];

        Ur[0] = Math.sqrt(re[0]) * gauss();
        Ui[0] = 0.0;
        if ((m & 1) == 0) {
            Ur[m/2] = Math.sqrt(re[m/2]) * gauss();
            Ui[m/2] = 0.0;
        }
        int upper = ((m & 1) == 0) ? m/2 : (m/2 + 1);
        for (int k = 1; k < upper; k++) {
            double lam = re[k];
            double a = gauss();
            double b = gauss();
            double s = Math.sqrt(lam / 2.0);
            double rk = s * a;
            double ik = s * b;
            Ur[k] = rk;        Ui[k] = ik;
            Ur[m - k] = rk;    Ui[m - k] = -ik;
        }

        // IFFT (this divides by N).
        fft(Ur, Ui, true);

        // Scale to sigma, add mean, take first n.
        double[] out = new double[n];
        for (int i = 0; i < n; i++) out[i] = mean + sigma * Ur[i];
        return out;
    }

    // fGn autocovariance for lag k ≥ 0:
    // gamma(k) = 0.5 * (|k-1|^{2H} - 2|k|^{2H} + |k+1|^{2H})
    private double gamma(int k) {
        if (k == 0) return 1.0;
        double kp1 = Math.pow(k + 1.0, 2.0 * H);
        double km1 = Math.pow(Math.abs(k - 1.0), 2.0 * H);
        double k0  = Math.pow(k, 2.0 * H);
        return 0.5 * (km1 - 2.0 * k0 + kp1);
    }

    private double gauss() {
        double u1 = Math.max(1e-12, rng.nextDouble());
        double u2 = rng.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    // In-place radix-2 complex FFT (forward and inverse).
    private static void fft(double[] re, double[] im, boolean inverse) {
        int n = re.length;
        if (n == 1) return;

        // bit-reversal
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >>> 1;
            for (; (j & bit) != 0; bit >>>= 1) j &= ~bit;
            j |= bit;
            if (i < j) {
                double tr = re[i]; re[i] = re[j]; re[j] = tr;
                double ti = im[i]; im[i] = im[j]; im[j] = ti;
            }
        }

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

                    double nwRe = wRe * wlenRe - wIm * wlenIm;
                    double nwIm = wRe * wlenIm + wIm * wlenRe;
                    wRe = nwRe; wIm = nwIm;
                }
            }
        }

        if (inverse) {
            for (int i = 0; i < n; i++) { re[i] /= n; im[i] /= n; }
        }
    }
}
