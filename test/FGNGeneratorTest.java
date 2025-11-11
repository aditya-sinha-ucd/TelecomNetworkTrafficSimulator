package util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the FGNGenerator class.
 * Verifies that generated samples are correct in length and range.
 */
public class FGNGeneratorTest {

    @Test
    void testGenerateFGNLengthAndVariance() {
        double[] fgn = FGNGenerator.generateFGN(128, 0.8, 123L);
        assertEquals(128, fgn.length, "FGN output length must match requested sample size");

        // Check not all zeros
        boolean allZero = true;
        for (double v : fgn) if (Math.abs(v) > 1e-6) { allZero = false; break; }
        assertFalse(allZero, "FGN samples should not be all zeros");
    }

    @Test
    void testToUnitIntervalBounds() {
        double[] fgn = { -2.0, 0.0, 2.0 };
        double[] norm = FGNGenerator.toUnitInterval(fgn);
        for (double v : norm) assertTrue(v >= 0.0 && v <= 1.0, "Normalized values must be within [0,1]");
    }

    @Test
    void testInvalidParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> FGNGenerator.generateFGN(0, 0.8, 1L));
        assertThrows(IllegalArgumentException.class, () -> FGNGenerator.generateFGN(10, 1.5, 1L));
    }
}
