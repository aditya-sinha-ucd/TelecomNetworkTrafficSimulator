/**
 * @file test/util/RandomUtilsTest.java
 * @brief Validates the Pareto and uniform number generation helpers.
 */
package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @class RandomUtilsTest
 * @brief Unit tests for {@link util.RandomUtils}.
 */
public class RandomUtilsTest {

    /**
     * @brief Ensures Pareto sampling always returns values above the scale parameter.
     */
    @Test
    void testParetoGeneratesPositiveValues() {
        for (int i = 0; i < 100; i++) {
            double v = RandomUtils.samplePareto(1.5, 1.0);
            assertTrue(v >= 1.0, "Pareto samples must be >= scale");
        }
    }

    /**
     * @brief Verifies uniform sampling remains within the provided bounds.
     */
    @Test
    void testUniformGeneratesInRange() {
        for (int i = 0; i < 100; i++) {
            double v = RandomUtils.uniform(5.0, 10.0);
            assertTrue(v >= 5.0 && v <= 10.0, "Uniform values must be within [min, max]");
        }
    }

    /**
     * @brief Confirms invalid Pareto parameters raise {@link IllegalArgumentException}.
     */
    @Test
    void testInvalidParetoParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.samplePareto(0.0, 1.0));
        assertThrows(IllegalArgumentException.class, () -> RandomUtils.samplePareto(1.5, -2.0));
    }
}