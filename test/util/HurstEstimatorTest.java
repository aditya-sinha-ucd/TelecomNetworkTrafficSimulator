/**
 * @file test/util/HurstEstimatorTest.java
 * @brief Tests for the {@link util.HurstEstimator} to ensure bounded outputs.
 */
package util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class HurstEstimatorTest
 * @brief Validates the Hurst estimator for typical and edge-case inputs.
 */
public class HurstEstimatorTest {

    /**
     * @brief Ensures estimated values always fall within the [0,1] interval.
     */
    @Test
    void testHurstValueWithinValidRange() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 200; i++) data.add(Math.random());

        double hurst = HurstEstimator.estimateHurst(data);
        assertTrue(hurst >= 0.0 && hurst <= 1.0, "Hurst exponent must be within [0,1]");
    }

    /**
     * @brief Verifies small datasets default to 0.5 as a fallback.
     */
    @Test
    void testHurstReturnsDefaultForSmallDataset() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) data.add(0.1);
        double hurst = HurstEstimator.estimateHurst(data);
        assertEquals(0.5, hurst, 0.01, "Small datasets should default to 0.5");
    }

    /**
     * @brief Ensures a constant series returns a low Hurst value (no long-term correlation).
     */
    @Test
    void testHurstForConstantSeries() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) data.add(1.0);

        double hurst = HurstEstimator.estimateHurst(data);
        assertTrue(hurst <= 0.6,
                "Constant data should produce a low Hurst value near 0.5 (no correlation)");
    }

    /**
     * @brief Ensures an increasing sequence produces a high Hurst value (persistent trend).
     */
    @Test
    void testHurstForIncreasingSequence() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) data.add((double) i);

        double hurst = HurstEstimator.estimateHurst(data);
        assertTrue(hurst >= 0.5,
                "Increasing sequence should yield a higher Hurst value (positive correlation)");
    }

    /**
     * @brief Ensures NaN or invalid inputs do not crash the estimator.
     */
    @Test
    void testHurstHandlesNaNValuesGracefully() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) data.add(Math.random());
        data.set(50, Double.NaN);

        assertDoesNotThrow(() -> HurstEstimator.estimateHurst(data),
                "Estimator should handle NaN values gracefully");
    }
    /**
     * @brief Confirms repeated estimation on similar data yields consistent results.
     */
    @Test
    void testHurstRepeatability() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 150; i++) data.add(Math.sin(i * 0.1));

        double h1 = HurstEstimator.estimateHurst(data);
        double h2 = HurstEstimator.estimateHurst(data);

        assertEquals(h1, h2, 0.05,
                "Repeated estimates on identical data should be consistent");
    }



}