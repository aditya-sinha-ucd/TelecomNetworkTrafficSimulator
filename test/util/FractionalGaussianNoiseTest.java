/**
 * @file test/util/FractionalGaussianNoiseTest.java
 * @brief Exercises the Davies–Harte implementation for correctness and reproducibility.
 */
package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class FractionalGaussianNoiseTest
 * @brief Unit tests for {@link util.FractionalGaussianNoise}.
 */
public class FractionalGaussianNoiseTest {

    /**
     * @brief Ensures generated series have correct length and non-zero variance.
     */
    @Test
    void testGenerateFGNLengthAndVariance() {
        FractionalGaussianNoise fgn = new FractionalGaussianNoise(0.8, 1.0, 123L);
        double[] samples = fgn.generate(128);

        assertEquals(128, samples.length, "Output length must match requested sample size");

        // Check not all zeros
        boolean allZero = true;

        // Use a tolerance of 1e-6 to ensure not all values are zero
        for (double v : samples)
            if (Math.abs(v) > 1e-6) { allZero = false; break; }

        assertFalse(allZero, "Generated samples should not be all zeros");
    }

    /**
     * Creates a Fractional Gaussian Noise generator.
     *
     * <p><b>Invalid parameter rules:</b>
     * <ul>
     *   <li><b>H ≤ 0.5</b> — The Hurst exponent must be in (0.5, 1).
     *       Values ≤ 0.5 do not produce long-memory FGN and would break the algorithm.</li>
     *
     *   <li><b>sigma ≤ 0</b> — Standard deviation must be strictly positive.
     *       A zero or negative σ makes no mathematical sense and would result in a
     *       degenerate or invalid covariance structure.</li>
     *
     *   <li>The seed may be any long value and is always valid.</li>
     * </ul>
     *
     * <p>If any invalid parameter is provided, the constructor throws
     * {@link IllegalArgumentException} to prevent constructing a broken generator.</p>
     */
    @Test
    void testInvalidParametersThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> new FractionalGaussianNoise(0.3, 1.0, 1L));
        assertThrows(IllegalArgumentException.class,
                () -> new FractionalGaussianNoise(0.8, -1.0, 1L));
    }

    /**
     * @brief Confirms seeding yields reproducible sample sequences.
     */
    @Test
    void testReproducibility() {
        FractionalGaussianNoise f1 = new FractionalGaussianNoise(0.8, 1.0, 42L);
        FractionalGaussianNoise f2 = new FractionalGaussianNoise(0.8, 1.0, 42L);
        assertArrayEquals(f1.generate(100), f2.generate(100), 1e-12, "Same seed must produce identical output");
    }
}
