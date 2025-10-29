package core;

import model.*;
import util.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The main controller class that runs the event-driven simulation.
 * <p>
 * Responsibilities:
 *  - Initialize all traffic sources.
 *  - Populate the event queue with initial events.
 *  - Process events in chronological order.
 *  - Collect and display aggregate traffic statistics.
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

    // Simulation clock tracking current time.
    private double currentTime;

    // Statistics collector to record aggregate traffic activity.
    private final StatisticsCollector stats;

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
        this.stats = new StatisticsCollector(1.0); // sample every 1 second
        this.currentTime = 0.0;

        // Initialize sources
        for (int i = 0; i < numSources; i++) {
            TrafficSource src = new TrafficSource(i, onShape, onScale, offShape, offScale);
            sources.add(src);

            // Schedule each source's first event randomly to avoid synchronization
            double initialOffset = RandomUtils.uniform(0, 5.0);
            eventQueue.addEvent(new Event(initialOffset, i, EventType.ON));
        }
    }

    /**
     * Runs the simulation until the total time is reached.
     */
    public void run() {
        System.out.println("Starting simulation...");

        while (!eventQueue.isEmpty() && currentTime < totalSimulationTime) {
            Event event = eventQueue.nextEvent();
            if (event == null) break;

            currentTime = event.getTime();

            // Stop if we exceed the total simulation duration
            if (currentTime > totalSimulationTime) break;

            // Process the event (source ON/OFF toggle)
            TrafficSource src = sources.get(event.getSourceId());
            src.processEvent(event);

            // Schedule the next event for this source
            Event next = src.generateNextEvent(currentTime);
            eventQueue.addEvent(next);

            // Compute the number of active (ON) sources
            long onCount = sources.stream().filter(TrafficSource::isOn).count();
            double rate = (double) onCount / numSources;

            // Record this sample in the statistics collector
            stats.recordSample(currentTime, rate);

            // Optional: progress update every 100 seconds
            if (((int) currentTime) % 100 == 0) {
                System.out.printf("[t=%.1f] Active sources: %d/%d%n", currentTime, onCount, numSources);
            }
        }

        System.out.println("Simulation complete!");

        // Print and export summary statistics
        stats.printSummary();
        stats.exportToCSV("output/traffic_data.csv");
    }
}
