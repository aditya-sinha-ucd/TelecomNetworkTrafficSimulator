/**
 * @file test/core/SimulatorTest.java
 * @brief Integration-style tests for {@link core.Simulator} and its collaborators.
 * These tests validate end-to-end simulation behavior for both Pareto and
 * Fractional Gaussian Noise (FGN) models. They also verify that the simulator
 * can work with dependency-injected output sinks for improved modularity.
 */
package core;

import extensions.NetworkQueue;
import io.OutputSink;
import model.SimulationParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Random;


import static org.junit.jupiter.api.Assertions.*;

/**
 * @class SimulatorTest
 * @brief Ensures the simulator runs across Pareto and FGN modes and supports injected sinks.
 */
public class SimulatorTest {

    /**
     * @brief Verifies the simulator can execute a Pareto configuration without exceptions.
     */
    @Test
    void testSimulatorRunsSuccessfully() {
        // Use non-deterministic randomness so parameters vary on each test run.
        Random r = new Random();

        // Randomised but safe parameter ranges
        double totalTime = 20 + r.nextDouble() * 80;   // 20–100 seconds
        int sources = 1 + r.nextInt(20);               // 1–20 sources

        double onAlpha = 1.1 + r.nextDouble() * 1.5;   // 1.1–2.6
        double onScale = 0.5 + r.nextDouble() * 2.0;   // 0.5–2.5

        double offAlpha = 1.1 + r.nextDouble() * 1.5;  // 1.1–2.6
        double offScale = 0.5 + r.nextDouble() * 2.0;  // 0.5–2.5

        // Step 1: Configure parameters
        SimulationParameters params = new SimulationParameters(
                totalTime,
                sources,
                onAlpha,
                onScale,
                offAlpha,
                offScale
        );

        params.samplingInterval = 0.5 + r.nextDouble(); // 0.5–1.5
        params.trafficModel = SimulationParameters.TrafficModel.PARETO_ON_OFF;
        params.randomSeed = r.nextLong();

        // Step 2: Create simulator
        Simulator sim = new Simulator(params);

        // Step 3: Assert simulator runs without exceptions
        assertDoesNotThrow(sim::run,
                "Simulator should complete successfully with randomized parameters");
    }

    /**
     * @brief Ensures the simulator handles FGN-driven runs without errors.
     */
    @Test
    void testSimulatorHandlesFGNModel() {
        // Use non-deterministic randomness so parameters vary on each test run.
        Random r = new Random();

        // Step 1: Configure randomised but safe FGN parameters
        double totalTime = 10 + r.nextDouble() * 40;     // 10–50 seconds
        int sources = 1 + r.nextInt(5);                   // 1–5 sources (fast test)

        SimulationParameters params = new SimulationParameters(
                totalTime,
                sources,
                1.5, 1.0, 1.2, 2.0    // not used in FGN mode
        );

        params.trafficModel = SimulationParameters.TrafficModel.FGN_THRESHOLD;

        params.hurst = 0.6 + r.nextDouble() * 0.3;       // H = 0.6–0.9
        params.fgnSigma = 0.5 + r.nextDouble() * 2.0;    // σ = 0.5–2.5
        params.fgnThreshold = -0.25 + r.nextDouble() * 0.5; // threshold = -0.25–0.25
        params.samplingInterval = 0.05 + r.nextDouble() * 0.2; // 0.05–0.25 s
        params.randomSeed = r.nextLong();

        // Step 2: Run FGN simulation
        Simulator sim = new Simulator(params);

        assertDoesNotThrow(sim::run,
                "FGN-based Simulator should run with randomized parameters");
    }


    /**
     * @brief Confirms a custom {@link OutputSink} can be injected and observed
     * with correct events, summarizes, and close() calls.
     */
    @Test
    void testSimulatorUsesInjectedOutputSink() throws Exception {
        // Step 1: Configure test parameters
        SimulationParameters params = new SimulationParameters(
                15.0, // shorter runtime
                4,
                1.4, 1.0, 1.2, 2.0
        );
        params.samplingInterval = 0.5;

        // Step 2: Create temporary directory for isolated test output
        Path tempRoot = Files.createTempDirectory("simulator-output-test");
        Path runDir = tempRoot.resolve("injected-run");
        // Atomic reference for recording created sink
        AtomicReference<RecordingSink> sinkRef = new AtomicReference<>();


        // Step 3: Construct simulator with a custom sink supplier (dependency injection)
        Simulator simulator = new Simulator(params, () -> {
            try {
                RecordingSink sink = new RecordingSink(runDir);
                sinkRef.set(sink);
                return sink;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Run simulation safely and verify no runtime exceptions occur
        assertDoesNotThrow(simulator::run,
                "Simulator should be able to run with a custom sink");

        // Step 4: Verify sink behavior and side effects
        RecordingSink sink = sinkRef.get();
        assertNotNull(sink, "Supplier should expose the created sink");
        assertTrue(sink.summarySaved, "Summary should be written through the sink");
        assertTrue(sink.closed, "Sink close() should be invoked via try-with-resources");
        assertFalse(sink.events.isEmpty(), "Simulation should emit events to the sink");
        assertTrue(Files.exists(runDir.resolve("traffic_data.csv")),
                "CSV export should target the injected run directory");

        // Cleanup temporary directories to avoid pollution
        deleteRecursively(tempRoot);
    }

    /**
     * @brief Removes temporary directories created during the test.
     */
    private void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to delete test directory: " + path, e);
                }
            });
        }
    }

    /**
     * @class RecordingSink
     * @brief Minimal {@link OutputSink} that records interactions for assertions.
     */
    private static final class RecordingSink implements OutputSink {
        private final Path runDir;
        private final List<Event> events = new ArrayList<>();
        private boolean summarySaved;
        private boolean closed;

        RecordingSink(Path runDir) throws IOException {
            this.runDir = runDir;
            Files.createDirectories(runDir);
        }

        @Override
        public String getRunDirectory() {
            return runDir.toString() + "/";
        }

        @Override
        public void logEvent(Event event) {
            events.add(event);
        }

        @Override
        public void saveSummary(StatisticsCollector stats, NetworkQueue queue) {
            summarySaved = true;
        }

        @Override
        public void saveFGNResults(double[] series, double H, double sigma,
                                   double samplingInterval, double threshold) {
            throw new UnsupportedOperationException("Not used in this test");
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
