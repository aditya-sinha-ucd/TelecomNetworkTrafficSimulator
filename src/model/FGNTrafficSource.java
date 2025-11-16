package model;

import core.Event;
import core.EventType;
import util.FractionalGaussianNoise;

import java.util.ArrayList;
import java.util.List;

/**
 * TrafficSource that follows an FGN-driven ON/OFF pattern.
 * <p>
 * The constructor converts an FGN series to a binary sequence using a threshold,
 * run-length encodes the sequence into durations, and then replays those durations
 * so the {@link core.Simulator} can continue operating without code changes.
 */
public final class FGNTrafficSource extends TrafficSource {

    private final SimulationParameters params;

    /** Precomputed durations between ON/OFF flips (seconds). */
    private final List<Double> durations = new ArrayList<>();
    private int durIndex = 0;

    private SourceState state = SourceState.OFF;
    private double nextTime = 0.0;

    /**
     * @param id     source identifier reused by the simulator
     * @param params simulation parameters containing all FGN settings
     */
    public FGNTrafficSource(int id, SimulationParameters params) {
        // Call parent with dummy Pareto values; we override all behavior.
        super(id, 1.0, 1.0, 1.0, 1.0);
        this.params = params;
        buildSchedule();
    }

    /** Build the flip schedule by thresholding an FGN series. */
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

    /** Return the next event time based on the next FGN duration. */
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

    /** Update current state when the simulator delivers the flip. */
    @Override
    public void processEvent(Event event) {
        if (event.getType() == EventType.ON) {
            state = SourceState.ON;
        } else {
            state = SourceState.OFF;
        }
    }

    /** @return {@code true} when the FGN schedule is currently ON */
    @Override
    public boolean isOn() {
        return state == SourceState.ON;
    }

    /**
     * @return timestamp of the next scheduled state change event
     */
    @Override
    public double getNextEventTime() {
        return nextTime;
    }
}
