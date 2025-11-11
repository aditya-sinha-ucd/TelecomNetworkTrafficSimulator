package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the RandomUtils class.
 * Validates Pareto and uniform random number generation.
 */
public class RandomUtilsTest {

    @Test
    void testParetoGeneratesPositiveValues() {
        for (int i = 0; i < 100; i++) {
            double v = RandomUtils.samplePareto(1.5, 1.0);
            assertTrue(v >= 1.0, "Pareto samples must be >= scale");
        }
    }

    @Test
    void testUniformGeneratesInRange() {
        for (int i = 0; i < 100; i++) {
            double v = RandomUtils.uniform(5.0, 10.0);
            assertTrue(v >= 5.0 && v <= 10.0, "Uniform values must be within [min, max]");
        }
    }

    @Test
    void testInvalidParetoParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.samplePareto(0.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.samplePareto(1.5, -2.0));
    }
}
