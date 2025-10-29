package core;

import model.*;
import util.RandomUtils;
import io.FileOutputManager;
import util.HurstEstimator;
import util.FGNGenerator;           //  FGN generator
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
 *  - (Bonus) Modulate arrivals with Fractional Gaussian Noise (FGN) for an
 *    alternative self-similar traffic method.
 *  - Display and save output summaries (CSV + text).
 */
public class Simulator {

    //  Core configuration

    /** Total time to simulate (seconds). */
    private final double totalSimulationTime;

    /** Number of traffic sources to simulate. */
    private final int numSources;

    // Core runtime state -

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

    // Bonus: simple network queue element

    /**
     * A single-server FIFO queue that receives packet arrivals driven by the
     * aggregate ON activity, and serves packets at a constant service rate.
     */
    private final NetworkQueue netQueue;

    /** For converting ON activity into packet arrivals (packets per second contributed by each ON source). */
    private final double packetsPerSecondPerOnSource = 5.0; // tweak as desired

    /** Internal tracker to compute arrival volume between events. */
    private double lastQueueUpdateTime = 0.0;

    //  Bonus: FGN-based rate modulator (alternative self-similar generator)

    /** Enable/disable multiplicative FGN modulation of arrivals. */
    private final boolean useFgnModulator = true;

    /** Hurst parameter for FGN (0 < H < 1), H>0.5 => long-range dependence. */
    private final double fgnH = 0.80;

    /** Seed for reproducibility. */
    private final long fgnSeed = 12345L;

    /** Sampling interval for the FGN series (seconds). */
    private final double fgnSampleInterval = 1.0;

    /** Precomputed FGN series mapped to [0,1] for use as a multiplicative factor over time. */
    private double[] fgnFactor01;

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

        this.eventQueue    = new EventQueue();
        this.sources       = new ArrayList<>();
        this.stats         = new StatisticsCollector(1.0);  // sample every 1s
        this.outputManager = new FileOutputManager();
        this.currentTime   = 0.0;

        // (Bonus) Precompute FGN based rate modulator series
        if (useFgnModulator) {
            int nSamples = (int) Math.ceil(totalSimulationTime / fgnSampleInterval) + 1;
            double[] fgn = FGNGenerator.generateFGN(nSamples, fgnH, fgnSeed); // zero-mean, unit-variance
            fgnFactor01 = FGNGenerator.toUnitInterval(fgn);                   // map to [0,1]
            System.out.printf("FGN rate modulator enabled: H=%.2f, samples=%d%n", fgnH, nSamples);
        } else {
            fgnFactor01 = null;
        }

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

            // Process the ON/OFF state transition for this source
            TrafficSource src = sources.get(event.getSourceId());
            src.processEvent(event);

            // Log the event for external inspection
            outputManager.logEvent(event);

            // Schedule the next state-change event for this source
            Event next = src.generateNextEvent(currentTime);
            eventQueue.addEvent(next);

            //  Compute aggregate activity (fraction of ON sources)
            long onCount = sources.stream().filter(TrafficSource::isOn).count();
            double rate = (double) onCount / numSources;

            // Record a sample for statistics (time-series of activity)
            stats.recordSample(currentTime, rate);

            // -Bonus: drive the simple network queue
            // First, process any service completions up to 'currentTime'
            netQueue.processUntil(currentTime);

            // Approximate the number of arrivals since the last queue update:
            // arrivals ≈ dt * (#ON sources) * (pps per ON) * FGN_factor
            double dt = Math.max(0.0, currentTime - lastQueueUpdateTime);
            double fgnFactor = fgnFactorAt(currentTime); // ∈ [0,1]
            int arrivals = (int) Math.floor(dt * onCount * packetsPerSecondPerOnSource * fgnFactor);

            netQueue.enqueueBulk(currentTime, arrivals);
            lastQueueUpdateTime = currentTime;

            // Optional progress display every ~100 seconds
            if (((int) currentTime) % 100 == 0) {
                System.out.printf("[t=%.1f] Active sources: %d/%d, FGN=%.2f%n",
                        currentTime, onCount, numSources, fgnFactor);
                System.out.println("           " + netQueue.toString());
            }
        }

        System.out.println("Simulation complete!");

        //  Output summaries
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

    // helpers

    /**
     * Returns the precomputed FGN modulator value in [0,1] for the given time.
     * If FGN modulation is disabled, returns 1.0 (no effect).
     */
    private double fgnFactorAt(double timeSeconds) {
        if (!useFgnModulator || fgnFactor01 == null) return 1.0;
        int idx = (int) Math.floor(timeSeconds / fgnSampleInterval);
        if (idx < 0) idx = 0;
        if (idx >= fgnFactor01.length) idx = fgnFactor01.length - 1;
        return fgnFactor01[idx];
    }
}
