package core;

import model.*;
import util.RandomUtils;
import io.FileOutputManager;
import io.OutputSink;
import extensions.MultiSourceManager;
import extensions.NetworkQueue;
import io.ErrorHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.List;

/**
 * Main controller for the event-driven telecom traffic simulation.
 * <p>
 * Responsibilities include:
 * <ul>
 *     <li>Initializing either Pareto or FGN-based traffic sources.</li>
 *     <li>Advancing the {@link EventQueue} and processing ON/OFF transitions.</li>
 *     <li>Sampling aggregate activity and exporting statistics.</li>
 *     <li>Coordinating optional queue modeling to approximate downstream congestion.</li>
 * </ul>
 */
public class Simulator {

    private final SimulationParameters params;
    private final double totalSimulationTime;
    private final int numSources;

    private final EventQueue eventQueue;
    private final SimulationClock clock;
    private final StatisticsCollector stats;
    private final Supplier<OutputSink> outputSinkSupplier;
    private final MultiSourceManager multiSourceManager;
    private final NetworkQueue networkQueue;

    private final List<TrafficSource> sources = new ArrayList<>();

    /**
     * Next regularly spaced sampling instant for recording the aggregate rate.
     * Sampling at fixed intervals better matches the assignment requirement of
     * exporting a uniformly spaced time-series instead of sampling only when
     * events occur.
     */
    private double nextSampleTime = 0.0;

    /** Keeps track of the most recent aggregate rate applied to sampling. */
    private double lastRecordedRate = 0.0;

    /**
     * Constructs a new Simulator using the provided configuration parameters.
     *
     * @param params immutable snapshot of user-provided simulation settings
     */
    public Simulator(SimulationParameters params) {
        this(params, () -> new FileOutputManager(defaultMetadata(params)));
    }

    /**
     * Allows callers (and tests) to inject a custom output sink.
     */
    public Simulator(SimulationParameters params, Supplier<OutputSink> outputSinkSupplier) {
        this.params = params;
        this.totalSimulationTime = params.totalSimulationTime;
        this.numSources = params.numberOfSources;
        this.outputSinkSupplier = Objects.requireNonNull(outputSinkSupplier, "outputSinkSupplier");

        this.eventQueue = new EventQueue();
        this.clock = new SimulationClock();
        this.stats = new StatisticsCollector(params.samplingInterval);

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

        // Initial aggregate rate (all sources start OFF).
        this.lastRecordedRate = computeAggregateRate();
    }

    /**
     * Runs the simulation and exports results to organized output files.
     */
    public void run() {
        System.out.println("Starting simulation...");

        try (OutputSink outputManager = outputSinkSupplier.get()) {
            String runDir = outputManager.getRunDirectory();

            while (!eventQueue.isEmpty() && clock.getTime() < totalSimulationTime) {
                Event event = eventQueue.nextEvent();
                if (event == null) break;

                double eventTime = event.getTime();
                recordSamplesBefore(Math.min(eventTime, totalSimulationTime));

                clock.advanceTo(eventTime);
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

                    // Update aggregate rate for future sampling
                    lastRecordedRate = computeAggregateRate();

                } catch (Exception e) {
                    ErrorHandler.handleError("Error processing event: " + e.getMessage(), false);
                }
            }

            // Capture any remaining samples through the end of the simulation window
            recordRemainingSamples();
            networkQueue.processUntil(totalSimulationTime);

            System.out.println("Simulation complete!");
            stats.printSummary();

            // === Export clean, organized outputs ===
            stats.exportToCSV(runDir + "traffic_data.csv");
            outputManager.saveSummary(stats, networkQueue);

            System.out.printf("Results saved to: %s%n", runDir);
        }
    }

    /**
     * Builds the default metadata map injected into {@link FileOutputManager}
     * so that each Pareto/FGN simulation run documents its configuration.
     */
    private static Map<String, String> defaultMetadata(SimulationParameters params) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("mode", params.trafficModel.name());
        metadata.put("sources", Integer.toString(params.numberOfSources));
        metadata.put("simulationTime", Double.toString(params.totalSimulationTime));
        metadata.put("samplingInterval", Double.toString(params.samplingInterval));
        return metadata;
    }

    /**
     * Computes the current aggregate ON rate across all sources.
     *
     * @return fraction of sources that are currently ON
     */
    private double computeAggregateRate() {
        if (numSources == 0) return 0.0;
        long onCount = sources.stream().filter(TrafficSource::isOn).count();
        return (double) onCount / numSources;
    }

    /**
     * Records samples at fixed intervals before the provided cutoff time.
     * The series between events is piecewise-constant, so we simply reuse the
     * last recorded rate until the next event occurs.
     */
    private void recordSamplesBefore(double cutoffTime) {
        if (params.samplingInterval <= 0) {
            ErrorHandler.handleError("Sampling interval must be positive", true);
        }
        while (nextSampleTime < cutoffTime && nextSampleTime <= totalSimulationTime) {
            networkQueue.processUntil(nextSampleTime);
            int arrivals = (int) Math.round(lastRecordedRate * numSources);
            if (arrivals > 0) {
                networkQueue.enqueueBulk(nextSampleTime, arrivals);
            }
            stats.recordSample(nextSampleTime, lastRecordedRate);
            nextSampleTime += params.samplingInterval;
        }
    }

    /** Records samples until the global simulation time limit is reached. */
    private void recordRemainingSamples() {
        if (params.samplingInterval <= 0) return;
        while (nextSampleTime <= totalSimulationTime) {
            networkQueue.processUntil(nextSampleTime);
            int arrivals = (int) Math.round(lastRecordedRate * numSources);
            if (arrivals > 0) {
                networkQueue.enqueueBulk(nextSampleTime, arrivals);
            }
            stats.recordSample(nextSampleTime, lastRecordedRate);
            nextSampleTime += params.samplingInterval;
        }
    }
}
