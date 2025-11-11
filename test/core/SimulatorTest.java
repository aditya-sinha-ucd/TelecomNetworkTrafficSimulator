package core;

import model.SimulationParameters;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the Simulator class.
 *
 * Verifies that the simulation can run end-to-end without throwing exceptions.
 * This test uses a small, deterministic configuration so it executes quickly.
 */
public class SimulatorTest {

    @Test
    void testSimulatorRunsSuccessfully() {
        // --- Step 1: Create simulation parameters ---
        SimulationParameters params = new SimulationParameters(
                50.0,   // total simulation time [s]
                5,      // number of sources
                1.5,    // ON shape (Pareto alpha)
                1.0,    // ON scale
                1.2,    // OFF shape
                2.0     // OFF scale
        );

        // Optional fine-tuning
        params.samplingInterval = 1.0;
        params.trafficModel = SimulationParameters.TrafficModel.PARETO_ON_OFF;
        params.randomSeed = 42L;  // deterministic runs for reproducibility

        // --- Step 2: Create simulator instance ---
        Simulator sim = new Simulator(params);

        // --- Step 3: Verify that it runs without throwing any exceptions ---
        assertDoesNotThrow(sim::run,
                "Simulator should complete a full run without exceptions");
    }

    @Test
    void testSimulatorHandlesFGNModel() {
        // --- Step 1: Configure FGN-based model ---
        SimulationParameters params = new SimulationParameters(
                20.0,  // short run
                3,     // fewer sources for faster test
                1.5, 1.0, 1.2, 2.0
        );
        params.trafficModel = SimulationParameters.TrafficModel.FGN_THRESHOLD;
        params.hurst = 0.8;
        params.fgnSigma = 1.0;
        params.fgnMean = 0.0;
        params.fgnThreshold = 0.0;

        // --- Step 2: Run FGN simulation ---
        Simulator sim = new Simulator(params);

        assertDoesNotThrow(sim::run,
                "FGN-based Simulator should run without throwing exceptions");
    }
}
