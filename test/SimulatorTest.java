package core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Simulator class.
 * Ensures the simulation runs end-to-end without throwing exceptions.
 */
public class SimulatorTest {

    @Test
    void testSimulatorRunsSuccessfully() {
        Simulator sim = new Simulator(
                50.0,  // total time
                5,     // number of sources
                1.5,   // ON shape
                1.0,   // ON scale
                1.2,   // OFF shape
                2.0    // OFF scale
        );

        assertDoesNotThrow(sim::run, "Simulator should complete without throwing exceptions");
    }
}
