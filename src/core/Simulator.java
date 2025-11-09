package core;

import model.*;
import util.RandomUtils;
import io.FileOutputManager;
import util.HurstEstimator;
import java.util.ArrayList;
import java.util.List;

/**
 * The main controller class that runs the event-driven simulation.
 * <p>
 * Responsibilities:
 *  - Initialize all traffic sources.
 *  - Populate the event queue with initial events.
 *  - Process events in chronological order.
 *  - Log events and collect traffic statistics.
 *  - Display and save output summaries.
 */
public class Simulator {

    // Total time to simulate (seconds).
    private final double totalSimulationTime;

    // Number of traffic sources to simulate.
    private final int numSources;

    // Event queue holding upcoming ON/OFF transitions.
    private final EventQueue eventQueue;

    // List of all traffic sources.
    private final List<TrafficSource> sources;

    // Central simulation clock to keep consistent time across components.
    private final SimulationClock clock;

    // Statistics collector to record aggregate traffic activity.
    private final StatisticsCollector stats;

    // File output manager for logs and summaries.
    private final FileOutputManager outputManager;

    /**
     * Constructs a Simulator with the specified parameters.
     *
     * @param totalSimulationTime total time to run the simulation (seconds)
     * @param numSources          number of independent ON/OFF sources
     * @param onShape             Pareto shape parameter for ON durations
     * @param onScale             Pareto scale parameter for ON durations
     * @param offShape            Pareto shape parameter for OFF durations
     * @param offScale            Pareto scale parameter for OFF durations
     */
    public Simulator(double totalSimulationTime, int numSources,
                     double onShape, double onScale,
                     double offShape, double offScale) {

        this.totalSimulationTime = totalSimulationTime;
        this.numSources = numSources;
        this.eventQueue = new EventQueue();
        this.sources = new ArrayList<>();
        this.clock = new SimulationClock(); // NEW: centralized time management
        this.stats = new StatisticsCollector(1.0); // sample every 1 second
        this.outputManager = new FileOutputManager();

        // Initialize all sources
        for (int i = 0; i < numSources; i++) {
            TrafficSource src = new TrafficSource(i, onShape, onScale, offShape, offScale);
            sources.add(src);

            // Schedule first ON event randomly to prevent synchronization artifacts
            double initialOffset = RandomUtils.uniform(0, 5.0);
            eventQueue.addEvent(new Event(initialOffset, i, EventType.ON));
        }
    }

    /**
     * Runs the simulation until the total time is reached.
     * <p>
     * Processes events in chronological order, updates source states,
     * records statistics, and logs each event to file.
     */
    public void run() {
        System.out.println("Starting simulation...");

        while (!eventQueue.isEmpty() && clock.getTime() < totalSimulationTime) {
            Event event = eventQueue.nextEvent();
            if (event == null) break;

            // Advance the clock to this event's time
            clock.advanceTo(event.getTime());

            // Stop if simulation time exceeded
            if (clock.getTime() > totalSimulationTime) break;

            // Process this event
            TrafficSource src = sources.get(event.getSourceId());
            src.processEvent(event);

            // Log the event for external inspection
            outputManager.logEvent(event);

            // Schedule the next state-change event for this source
            Event next = src.generateNextEvent(clock.getTime());
            eventQueue.addEvent(next);

            // Compute aggregate rate (fraction of ON sources)
            long onCount = sources.stream().filter(TrafficSource::isOn).count();
            double rate = (double) onCount / numSources;

            // Record a sample for statistics
            stats.recordSample(clock.getTime(), rate);

            // Optional progress display every ~100 seconds
            if (((int) clock.getTime()) % 100 == 0) {
                System.out.printf("[t=%.1f] Active sources: %d/%d%n",
                        clock.getTime(), onCount, numSources);
            }
        }

        System.out.println("Simulation complete!");

        // Print summary to console and export results
        stats.printSummary();
        stats.exportToCSV("output/traffic_data.csv");

        // BONUS: Estimate Hurst exponent
        double hurst = HurstEstimator.estimateHurst(stats.getActivityRates());
        System.out.printf("Estimated Hurst exponent: %.3f%n", hurst);

        // Save summary + logs
        outputManager.saveSummary(stats);
        outputManager.close();
    }
}
