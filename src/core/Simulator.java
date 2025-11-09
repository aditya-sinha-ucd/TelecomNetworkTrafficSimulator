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
 * Main controller for the event-driven simulation.
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

    public Simulator(SimulationParameters params) {
        this.params = params;
        this.totalSimulationTime = params.totalSimulationTime;
        this.numSources = params.numberOfSources;

        this.eventQueue = new EventQueue();
        this.clock = new SimulationClock();
        this.stats = new StatisticsCollector(params.samplingInterval);
        this.outputManager = new FileOutputManager();

        this.multiSourceManager = new MultiSourceManager();
        this.networkQueue = new NetworkQueue(5.0); // example µ

        // Choose model
        if (params.trafficModel == SimulationParameters.TrafficModel.FGN_THRESHOLD) {
            sources.addAll(multiSourceManager.generateFGNSources(params));
        } else {
            sources.addAll(
                    multiSourceManager.generateSources(numSources,
                            params.onShape, params.onScale,
                            params.offShape, params.offScale,
                            0.15)
            );
        }

        // First event for each source at a small random offset.
        for (int i = 0; i < sources.size(); i++) {
            double offset = RandomUtils.uniform(0, 5.0);
            eventQueue.addEvent(new Event(offset, i, EventType.ON));
        }
    }

    public void run() {
        System.out.println("Starting simulation...");

        while (!eventQueue.isEmpty() && clock.getTime() < totalSimulationTime) {
            Event event = eventQueue.nextEvent();
            if (event == null) break;

            clock.advanceTo(event.getTime());
            if (clock.getTime() > totalSimulationTime) break;

            try {
                TrafficSource src = sources.get(event.getSourceId());
                src.processEvent(event);
                outputManager.logEvent(event);

                Event next = src.generateNextEvent(clock.getTime());
                eventQueue.addEvent(next);

                long onCount = sources.stream().filter(TrafficSource::isOn).count();
                double rate = (double) onCount / numSources;
                stats.recordSample(clock.getTime(), rate);

                if (rate > 0) networkQueue.enqueue(clock.getTime());

                if (((int) clock.getTime()) % 100 == 0) {
                    System.out.printf("[t=%.1f] Active sources: %d/%d%n",
                            clock.getTime(), onCount, numSources);
                    System.out.printf("[Queue] t=%.1f, Avg delay: %.4fs, Processed: %d%n",
                            clock.getTime(), networkQueue.getAverageDelay(), networkQueue.getProcessedPackets());
                }

            } catch (Exception e) {
                ErrorHandler.handleError("Error processing event: " + e.getMessage(), false);
            }
        }

        System.out.println("Simulation complete!");

        stats.printSummary();
        stats.exportToCSV("output/traffic_data.csv");

        double hurst = HurstEstimator.estimateHurst(stats.getActivityRates());
        System.out.printf("Estimated Hurst exponent: %.3f%n", hurst);

        System.out.printf("Average queue delay: %.4f s%n", networkQueue.getAverageDelay());
        outputManager.saveSummary(stats);
        outputManager.close();
    }
}
