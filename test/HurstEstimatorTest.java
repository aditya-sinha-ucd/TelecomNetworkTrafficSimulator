package util;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the HurstEstimator class.
 * Validates that computed Hurst values are within [0,1].
 */
public class HurstEstimatorTest {

    @Test
    void testHurstValueWithinValidRange() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 200; i++) data.add(Math.random());

        double hurst = HurstEstimator.estimateHurst(data);
        assertTrue(hurst >= 0.0 && hurst <= 1.0, "Hurst exponent must be within [0,1]");
    }

    @Test
    void testHurstReturnsDefaultForSmallDataset() {
        List<Double> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) data.add(0.1);
        double hurst = HurstEstimator.estimateHurst(data);
        assertEquals(0.5, hurst, 0.01, "Small datasets should default to 0.5");
    }
}
