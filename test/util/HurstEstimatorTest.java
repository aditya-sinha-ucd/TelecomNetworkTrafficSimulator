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
}