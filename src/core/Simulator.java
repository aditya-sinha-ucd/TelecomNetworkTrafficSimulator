package core;

import model.*;
import util.RandomUtils;
import io.FileOutputManager;
import util.HurstEstimator;
import extensions.NetworkQueue;

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
 *  - Drive a simple single-server NetworkQueue (bonus).
 *  - Display and save output summaries (CSV + text).
 */
public class Simulator {

    // ---- Core configuration ----

    /** Total time to simulate (seconds). */
    private final double totalSimulationTime;

    /** Number of traffic sources to simulate. */
    private final int numSources;

    // ---- Core runtime state ----

    /** Event queue holding upcoming ON/OFF transitions. */
    private final EventQueue eventQueue;

    /** All traffic sources (ON/OFF generators). */
    private final List<TrafficSource> sources;

    /** Simulation clock (current time in seconds). */
    private double currentTime;

    /** Statistics collector for aggregate activity (0..1). */
    private final StatisticsCollector stats;

    /** File output manager for event log + summary file. */
    private final FileOutputManager outputManager;

    // ---- Bonus: simple network queue element ----

    /**
     * A single-server FIFO queue that receives packet arrivals driven by the
     * aggregate ON activity, and serves packets at a constant service rate.
     */
    private final NetworkQueue netQueue;

    /** For converting ON activity into packet arrivals. */
    private final double packetsPerSecondPerOnSource = 5.0; // tweak as desired

    /** Internal tracker to compute arrival volume between events. */
    private double lastQueueUpdateTime = 0.0;

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

        this.eventQueue   = new EventQueue();
        this.sources      = new ArrayList<>();
        this.stats        = new StatisticsCollector(1.0);  // sample every 1s
        this.outputManager = new FileOutputManager();
        this.currentTime  = 0.0;

        // Bonus queue: service rate = 50 packets/sec, buffer capacity = 200 (<=0 means infinite)
        this.netQueue = new NetworkQueue(50.0, 200);

        // Initialize all sources with the same ON/OFF Pareto parameters for now.
        // (You can later replace this with MultiSourceManager to mix types.)
        for (int i = 0; i < numSources; i++) {
            TrafficSource src = new TrafficSource(i, onShape, onScale, offShape, offScale);
            sources.add(src);

            // Schedule first ON event randomly to avoid synchronization artifacts.
            double initialOffset = RandomUtils.uniform(0, 5.0);
            eventQueue.addEvent(new Event(initialOffset, i, EventType.ON));
        }
    }

    /**
     * Runs the simulation until the total time is reached.
     * <p>
     * Processes events in chronological order, updates source states,
     * logs events, records statistics, and drives the NetworkQueue.
     */
    public void run() {
        System.out.println("Starting simulation...");

        while (!eventQueue.isEmpty() && currentTime < totalSimulationTime) {
            Event event = eventQueue.nextEvent();
            if (event == null) break;

            currentTime = event.getTime();

            // Stop if simulation time exceeded
            if (currentTime > totalSimulationTime) break;

            // ---- Process the ON/OFF state transition for this source ----
            TrafficSource src = sources.get(event.getSourceId());
            src.processEvent(event);

            // Log the event for external inspection
            outputManager.logEvent(event);

            // Schedule the next state-change event for this source
            Event next = src.generateNextEvent(currentTime);
            eventQueue.addEvent(next);

            // ---- Compute aggregate activity (fraction of ON sources) ----
            long onCount = sources.stream().filter(TrafficSource::isOn).count();
            double rate = (double) onCount / numSources;

            // Record a sample for statistics (time-series of activity)
            stats.recordSample(currentTime, rate);

            // ---- Bonus: drive the simple network queue ----
            // First, process any service completions up to 'currentTime'
            netQueue.processUntil(currentTime);

            // Approximate the number of arrivals since the last queue update:
            //   arrivals ≈ (dt seconds) × (#ON sources) × (pps per ON)
            double dt = Math.max(0.0, currentTime - lastQueueUpdateTime);
            int arrivals = (int) Math.floor(dt * onCount * packetsPerSecondPerOnSource);
            netQueue.enqueueBulk(currentTime, arrivals);
            lastQueueUpdateTime = currentTime;

            // Optional progress display every ~100 seconds
            if (((int) currentTime) % 100 == 0) {
                System.out.printf("[t=%.1f] Active sources: %d/%d%n", currentTime, onCount, numSources);
                System.out.println("           " + netQueue.toString());
            }
        }

        System.out.println("Simulation complete!");

        // ---- Output summaries ----
        stats.printSummary();
        stats.exportToCSV("output/traffic_data.csv");

        // BONUS: Estimate Hurst exponent from the recorded series
        double hurst = HurstEstimator.estimateHurst(stats.getActivityRates());
        System.out.printf("Estimated Hurst exponent: %.3f%n", hurst);

        // Save summary + close event log
        outputManager.saveSummary(stats);
        outputManager.close();

        // Final queue summary line
        System.out.println("Queue Summary: " + netQueue.toString());
    }
}
