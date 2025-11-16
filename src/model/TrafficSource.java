package model;

import core.Event;
import core.EventType;

/**
 * Represents a single ON/OFF traffic source in the simulation.
 * <p>
 * Each TrafficSource alternates between active (ON) and idle (OFF) states.
 * The ON and OFF durations are sampled from Pareto distributions to model
 * self-similar network traffic.
 */
public class TrafficSource {

    /** Unique identifier for this source. */
    private final int id;

    /** Current state of the source (ON or OFF). */
    private SourceState state;

    /** Distributions for ON and OFF durations. */
    private final Distribution onDist;
    private final Distribution offDist;

    /** Time of next scheduled event. */
    private double nextEventTime;

    /**
     * Constructs a new TrafficSource with given Pareto parameters.
     */
    public TrafficSource(int id, double onShape, double onScale,
                         double offShape, double offScale) {
        this(id,
                new ParetoDistribution(onShape, onScale),
                new ParetoDistribution(offShape, offScale));
    }

    /**
     * Package-private constructor that accepts arbitrary distributions.
     * <p>
     * Exposed primarily for testing so that deterministic distributions
     * (or other distribution implementations) can be injected.
     */
    TrafficSource(int id, Distribution onDist, Distribution offDist) {
        if (onDist == null || offDist == null) {
            throw new IllegalArgumentException("Distributions cannot be null");
        }
        this.id = id;
        this.state = SourceState.OFF;
        this.onDist = onDist;
        this.offDist = offDist;
        this.nextEventTime = 0.0;
    }

    /**
     * Generates the next event for this source based on its current state.
     *
     * @param currentTime the current simulation time
     * @return the next event (ON or OFF)
     */
    public Event generateNextEvent(double currentTime) {
        double duration;
        if (state == SourceState.OFF) {
            duration = onDist.sample();
            nextEventTime = currentTime + duration;
            return new Event(nextEventTime, id, EventType.ON);
        } else {
            duration = offDist.sample();
            nextEventTime = currentTime + duration;
            return new Event(nextEventTime, id, EventType.OFF);
        }
    }

    /**
     * Processes a state change event (ON or OFF).
     *
     * @param event event delivered by the simulator that flips the source state
     */
    public void processEvent(Event event) {
        if (event.getType() == EventType.ON) {
            state = SourceState.ON;
        } else {
            state = SourceState.OFF;
        }
    }

    /** @return true if the source is currently ON. */
    public boolean isOn() {
        return state == SourceState.ON;
    }

    /** @return the ID of this source. */
    public int getId() {
        return id;
    }

    /** @return the time of the next scheduled event. */
    public double getNextEventTime() {
        return nextEventTime;
    }

    @Override
    public String toString() {
        return String.format("Source %d [%s] nextEvent=%.3f", id, state, nextEventTime);
    }
}
