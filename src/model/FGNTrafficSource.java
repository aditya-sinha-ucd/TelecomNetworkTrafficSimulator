/**
 * @file src/model/FGNTrafficSource.java
 * @brief {@link TrafficSource} implementation driven by Fractional Gaussian Noise.
 * @details Converts an FGN time series into ON/OFF durations, enabling the
 *          {@link core.Simulator} to reuse existing scheduling logic without
 *          modification. Collaborates with {@link util.FractionalGaussianNoise}
 *          and {@link model.SimulationParameters} for generation parameters.
 * @date 2024-05-30
 */
package model;

import core.Event;
import core.EventType;
import util.FractionalGaussianNoise;

import java.util.ArrayList;
import java.util.List;

/**
 * @class FGNTrafficSource
 * @brief TrafficSource variant that follows an FGN-driven ON/OFF pattern.
 */
public final class FGNTrafficSource extends TrafficSource {

    private final SimulationParameters params;

    /** Precomputed durations between ON/OFF flips (seconds). */
    private final List<Double> durations = new ArrayList<>();
    private int durIndex = 0;

    private SourceState state = SourceState.OFF;
    private double nextTime = 0.0;

    /**
     * @brief Constructs an FGN-backed source that replays a deterministic schedule.
     * @param id Source identifier reused by the simulator.
     * @param params Simulation parameters containing all FGN settings.
     */
    public FGNTrafficSource(int id, SimulationParameters params) {
        // Call parent with dummy Pareto values; we override all behavior.
        super(id, 1.0, 1.0, 1.0, 1.0);
        this.params = params;
        buildSchedule();
    }

    /**
     * @brief Builds the flip schedule by thresholding an FGN series.
     */
    private void buildSchedule() {
        int n = (int) Math.ceil(params.totalSimulationTime / params.samplingInterval);
        if (n < 2) n = 2;

        long seed = params.fgnSeed + getId();
        FractionalGaussianNoise fgn = new FractionalGaussianNoise(
                params.hurst, params.fgnSigma, seed);
        double[] x = fgn.generate(n);

        boolean[] bin = new boolean[n];
        for (int i = 0; i < n; i++) bin[i] = (x[i] >= params.fgnThreshold);

        // Run-length encode to durations. We don't force an initial state here;
        // we just store durations between flips. The current state will be
        // set by the first event the simulator sends (ON or OFF).
        boolean cur = bin[0];
        int count = 1;
        for (int i = 1; i < n; i++) {
            if (bin[i] == cur) {
                count++;
            } else {
                durations.add(count * params.samplingInterval);
                cur = bin[i];
                count = 1;
            }
        }
        durations.add(count * params.samplingInterval);
        // If durations list ends early, Simulator will stop at totalSimulationTime anyway.
    }

    /**
     * @brief Returns the next event time based on the next FGN duration.
     * @param currentTime Current simulation time.
     * @return {@link Event} that flips the source state.
     */
    @Override
    public Event generateNextEvent(double currentTime) {
        if (durIndex >= durations.size()) {
            // No more flips planned; push far in the future so simulator will end naturally.
            nextTime = currentTime + params.totalSimulationTime;
            return new Event(nextTime, getId(), state == SourceState.ON ? EventType.OFF : EventType.ON);
        }
        double dt = durations.get(durIndex++);
        nextTime = currentTime + dt;
        // Flip to the opposite of whatever we are now.
        return new Event(nextTime, getId(), state == SourceState.ON ? EventType.OFF : EventType.ON);
    }

    /**
     * @brief Updates current state when the simulator delivers the flip.
     * @param event Event representing the new state.
     */
    @Override
    public void processEvent(Event event) {
        if (event.getType() == EventType.ON) {
            state = SourceState.ON;
        } else {
            state = SourceState.OFF;
        }
    }

    /**
     * @brief Indicates whether the FGN schedule is currently ON.
     * @return {@code true} if the state is ON.
     */
    @Override
    public boolean isOn() {
        return state == SourceState.ON;
    }

    /**
     * @brief Timestamp of the next scheduled state change event.
     * @return Absolute time of the next flip.
     */
    @Override
    public double getNextEventTime() {
        return nextTime;
    }
}
