package model;

import core.Event;
import core.EventType;
import util.RandomUtils;

/**
 * Represents a single ON/OFF traffic source in the simulation.
 * <p>
 * Each TrafficSource alternates between two states:
 * <ul>
 *     <li>ON: actively generating traffic (e.g., sending packets)</li>
 *     <li>OFF: idle state, generating no traffic</li>
 * </ul>
 * <p>
 * Durations of ON and OFF periods follow heavy-tailed (Pareto)
 * distributions to produce self-similar aggregated traffic when
 * multiple sources are combined.
 */
public class TrafficSource {

    // Unique identifier for this source (0 based index).
    private final int id;

    // Current state of this source (ON or OFF).
    private SourceState state;

    // Shape and scale parameters for Pareto-distributed ON durations.
    private final double onShape, onScale;

    // Shape and scale parameters for Pareto-distributed OFF durations.
    private final double offShape, offScale;

    // The simulation time of the next scheduled state change.
    private double nextEventTime;

    /**
     * Constructs a new ON/OFF traffic source.
     *
     * @param id        the source ID
     * @param onShape   Pareto alpha parameter for ON duration
     * @param onScale   Pareto scale (minimum) parameter for ON duration
     * @param offShape  Pareto alpha parameter for OFF duration
     * @param offScale  Pareto scale (minimum) parameter for OFF duration
     */
    public TrafficSource(int id, double onShape, double onScale, double offShape, double offScale) {
        this.id = id;
        this.onShape = onShape;
        this.onScale = onScale;
        this.offShape = offShape;
        this.offScale = offScale;
        this.state = SourceState.OFF; // default start state
        this.nextEventTime = 0.0;
    }

    /**
     * Generates the next event (ON→OFF or OFF→ON) for this source.
     *
     * @param currentTime the current simulation time
     * @return the next event for this source
     */
    public Event generateNextEvent(double currentTime) {
        double duration;

        // Determine the duration of the next period based on the current state
        if (state == SourceState.OFF) {
            duration = RandomUtils.samplePareto(onShape, onScale);
            nextEventTime = currentTime + duration;
            return new Event(nextEventTime, id, EventType.ON);
        } else {
            duration = RandomUtils.samplePareto(offShape, offScale);
            nextEventTime = currentTime + duration;
            return new Event(nextEventTime, id, EventType.OFF);
        }
    }

    /**
     * Handles a state change when an event occurs.
     *
     * @param event the event to process
     */
    public void processEvent(Event event) {
        if (event.getType() == EventType.ON) {
            state = SourceState.ON;
        } else {
            state = SourceState.OFF;
        }
    }

    // @return true if the source is currently ON (active)
    public boolean isOn() {
        return state == SourceState.ON;
    }

    // @return the ID of this source
    public int getId() {
        return id;
    }

    // @return the simulation time of the next scheduled event
    public double getNextEventTime() {
        return nextEventTime;
    }

    @Override
    public String toString() {
        return String.format("Source %d [%s] nextEvent=%.3f", id, state, nextEventTime);
    }
}
