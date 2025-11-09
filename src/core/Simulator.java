package core;

import model.*;
import util.RandomUtils;
import util.HurstEstimator;
import io.FileOutputManager;
import extensions.MultiSourceManager;
import extensions.NetworkQueue;
import io.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the event-driven telecom traffic simulation.
 * <p>
 * Responsibilities:
 *  - Initialize traffic sources (Pareto or FGN-based)
 *  - Manage event queue and process ON/OFF transitions
 *  - Record and export statistics
 *  - Integrate optional queue modeling
 */
public class Simulator {

    private final SimulationParameters params;
    private final double totalSimulationTime;
    private final int numSources;

    private final EventQueue eventQueue;
    private final SimulationClock clock;
    private final StatisticsCollector stats;
    private final FileOutputManager outputManager;
    private final MultiSourceManager multiSourceManager;
    private final NetworkQueue networkQueue;

    private final List<TrafficSource> sources = new ArrayList<>();

    /**
     * Constructs a new Simulator using the provided configuration parameters.
     */
    public Simulator(SimulationParameters params) {
        this.params = params;
        this.totalSimulationTime = params.totalSimulationTime;
        this.numSources = params.numberOfSources;

        this.eventQueue = new EventQueue();
        this.clock = new SimulationClock();
        this.stats = new StatisticsCollector(params.samplingInterval);

        // Initialize output manager (creates its own per-run folder)
        this.outputManager = new FileOutputManager();
        String runDir = outputManager.getRunDirectory();

        // Initialize managers
        this.multiSourceManager = new MultiSourceManager();
        this.networkQueue = new NetworkQueue(5.0); // Example service rate µ = 5 pkt/s

        // === Initialize sources ===
        if (params.trafficModel == SimulationParameters.TrafficModel.FGN_THRESHOLD) {
            // Alternative model using Fractional Gaussian Noise-based thresholds
            sources.addAll(multiSourceManager.generateFGNSources(params));
        } else {
            // Default Pareto-based ON/OFF sources
            sources.addAll(
                    multiSourceManager.generateSources(numSources,
                            params.onShape, params.onScale,
                            params.offShape, params.offScale,
                            0.15)
            );
        }

        // Schedule each source's first ON event at a small random offset
        for (int i = 0; i < sources.size(); i++) {
            double offset = RandomUtils.uniform(0, 5.0);
            eventQueue.addEvent(new Event(offset, i, EventType.ON));
        }
    }

    /**
     * Runs the simulation and exports results to organized output files.
     */
    public void run() {
        System.out.println("Starting simulation...");
        String runDir = outputManager.getRunDirectory();

        while (!eventQueue.isEmpty() && clock.getTime() < totalSimulationTime) {
            Event event = eventQueue.nextEvent();
            if (event == null) break;

            clock.advanceTo(event.getTime());
            if (clock.getTime() > totalSimulationTime) break;

            try {
                // Process source event
                TrafficSource src = sources.get(event.getSourceId());
                src.processEvent(event);

                // Log to event log
                outputManager.logEvent(event);

                // Schedule next state-change event
                Event next = src.generateNextEvent(clock.getTime());
                eventQueue.addEvent(next);

                // Compute aggregate rate
                long onCount = sources.stream().filter(TrafficSource::isOn).count();
                double rate = (double) onCount / numSources;
                stats.recordSample(clock.getTime(), rate);

                // Queue arrivals (optional feature)
                if (rate > 0) networkQueue.enqueue(clock.getTime());

            } catch (Exception e) {
                ErrorHandler.handleError("Error processing event: " + e.getMessage(), false);
            }
        }

        System.out.println("Simulation complete!");
        stats.printSummary();

        // === Export clean, organized outputs ===
        stats.exportToCSV(runDir + "traffic_data.csv");
        outputManager.saveSummary(stats);
        outputManager.close();

        System.out.printf("Results saved to: %s%n", runDir);
    }
}
