/**
 * @file test/core/SimulatorTest.java
 * @brief Integration-style tests for {@link core.Simulator} and its collaborators.
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
        // Step 1: Create simulation parameters
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
        params.randomSeed = 42L;

        // Step 2: Create simulator instance
        Simulator sim = new Simulator(params);

        // tep 3: Verify that it runs without throwing any exceptions -
        assertDoesNotThrow(sim::run,
                "Simulator should complete a full run without exceptions");
    }

    /**
     * @brief Ensures the simulator handles FGN-driven runs without errors.
     */
    @Test
    void testSimulatorHandlesFGNModel() {
        // First Step: Configure FGN-based model
        SimulationParameters params = new SimulationParameters(
                20.0,  // short run
                3,     // fewer sources for faster test
                1.5, 1.0, 1.2, 2.0
        );
        params.trafficModel = SimulationParameters.TrafficModel.FGN_THRESHOLD;
        params.hurst = 0.8;
        params.fgnSigma = 1.0;
        params.fgnThreshold = 0.0;

        // Second Step: Run FGN simulation
        Simulator sim = new Simulator(params);

        assertDoesNotThrow(sim::run,
                "FGN-based Simulator should run without throwing exceptions");
    }

    /**
     * @brief Confirms a custom {@link OutputSink} can be injected and observed.
     */
    @Test
    void testSimulatorUsesInjectedOutputSink() throws Exception {
        SimulationParameters params = new SimulationParameters(
                15.0,
                4,
                1.4, 1.0, 1.2, 2.0
        );
        params.samplingInterval = 0.5;

        Path tempRoot = Files.createTempDirectory("simulator-output-test");
        Path runDir = tempRoot.resolve("injected-run");
        AtomicReference<RecordingSink> sinkRef = new AtomicReference<>();

        Simulator simulator = new Simulator(params, () -> {
            try {
                RecordingSink sink = new RecordingSink(runDir);
                sinkRef.set(sink);
                return sink;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        assertDoesNotThrow(simulator::run,
                "Simulator should be able to run with a custom sink");

        RecordingSink sink = sinkRef.get();
        assertNotNull(sink, "Supplier should expose the created sink");
        assertTrue(sink.summarySaved, "Summary should be written through the sink");
        assertTrue(sink.closed, "Sink close() should be invoked via try-with-resources");
        assertFalse(sink.events.isEmpty(), "Simulation should emit events to the sink");
        assertTrue(Files.exists(runDir.resolve("traffic_data.csv")),
                "CSV export should target the injected run directory");

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
