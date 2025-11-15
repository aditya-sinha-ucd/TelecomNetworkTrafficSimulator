package system;

import core.Simulator;
import io.ConfigFileLoader;
import io.ErrorHandler;
import model.SimulationParameters;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System-level tests that exercise the primary simulation workflow.
 */
public class SimulationFlowSystemTest {

    @Test
    void testSimulationFlowFromConfigToOutput() throws Exception {
        Path config = Files.createTempFile("sim", ".cfg");
        Files.writeString(config, String.join("\n",
                "# basic simulation inputs",
                "totalTime=6",
                "numSources=2",
                "onShape=1.4",
                "onScale=1.0",
                "offShape=1.2",
                "offScale=2.0"));

        Set<String> existingRuns = new HashSet<>(listRunDirectories());

        Map<String, Double> loaded = ConfigFileLoader.loadConfig(config.toString());
        assertEquals(6.0, loaded.get("totalTime"));
        assertEquals(2.0, loaded.get("numSources"));

        SimulationParameters params = new SimulationParameters(
                loaded.get("totalTime"),
                loaded.get("numSources").intValue(),
                loaded.get("onShape"),
                loaded.get("onScale"),
                loaded.get("offShape"),
                loaded.get("offScale"));
        params.randomSeed = 123L;
        params.samplingInterval = 0.5;

        Simulator simulator = new Simulator(params);
        assertDoesNotThrow(simulator::run);

        List<String> newRuns = listRunDirectories().stream()
                .filter(dir -> !existingRuns.contains(dir))
                .collect(Collectors.toList());
        assertFalse(newRuns.isEmpty(), "Simulation should create a new run directory");

        for (String dir : newRuns) {
            Path runDir = Path.of(dir);
            Path eventLog = runDir.resolve("event_log.txt");
            Path summary = runDir.resolve("summary.txt");
            Path csv = runDir.resolve("traffic_data.csv");

            assertTrue(Files.exists(eventLog), "Event log should be generated");
            assertTrue(Files.exists(summary), "Summary file should be generated");
            assertTrue(Files.exists(csv), "CSV file should be generated");

            assertTrue(Files.size(eventLog) > 0, "Event log should contain entries");
            assertTrue(Files.size(summary) > 0, "Summary should contain text");
            assertTrue(Files.size(csv) > 0, "CSV should contain samples");

            deleteRecursively(runDir);
        }

        Files.deleteIfExists(config);
    }

    @Test
    void testErrorHandlerNonFatalLogsMessage() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        try {
            System.setErr(new PrintStream(err));
            ErrorHandler.handleError("test failure", false);
        } finally {
            System.setErr(originalErr);
        }

        String output = err.toString();
        assertTrue(output.contains("ERROR: test failure"));
        assertFalse(output.contains("Program terminated."));
    }

    private List<String> listRunDirectories() throws IOException {
        Path outputRoot = Path.of("output");
        if (!Files.exists(outputRoot)) {
            return new ArrayList<>();
        }
        try (java.util.stream.Stream<Path> stream = Files.list(outputRoot)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (java.util.stream.Stream<Path> stream = Files.walk(dir)) {
            List<Path> paths = stream.sorted((a, b) -> b.compareTo(a)).collect(Collectors.toList());
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }
}
