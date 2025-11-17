/**
 * @file src/model/TrafficSource.java
 * @brief Models an ON/OFF traffic source backed by configurable distributions.
 * @details Collaborates with {@link core.Simulator} to emit {@link core.Event}
 *          objects and responds to processed events to update internal state.
 *          Default behavior uses {@link model.ParetoDistribution} to generate
 *          heavy-tailed durations, but tests can inject custom distributions.
 */
package model;

import core.Event;
import core.EventType;

/**
 * @class TrafficSource
 * @brief Represents a single ON/OFF traffic source in the simulation.
 * @details Alternates between active (ON) and idle (OFF) states with durations
 *          drawn from configured distributions. Generates events consumed by
 *          the simulator event queue.
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
     * @brief Constructs a new TrafficSource with given Pareto parameters.
     * @param id Unique identifier for the source.
     * @param onShape Shape parameter for ON durations.
     * @param onScale Scale parameter for ON durations.
     * @param offShape Shape parameter for OFF durations.
     * @param offScale Scale parameter for OFF durations.
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
     * @brief Generates the next event based on the current state.
     * @param currentTime Current simulation time.
     * @return Next {@link Event} describing a state change.
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
     * @brief Processes a state change event (ON or OFF).
     * @param event Event delivered by the simulator that flips the source state.
     */
    public void processEvent(Event event) {
        if (event.getType() == EventType.ON) {
            state = SourceState.ON;
        } else {
            state = SourceState.OFF;
        }
    }

    /**
     * @brief Indicates whether the source is currently ON.
     * @return {@code true} when in the ON state.
     */
    public boolean isOn() {
        return state == SourceState.ON;
    }

    /**
     * @brief Identifier of this source.
     * @return Unique source ID.
     */
    public int getId() {
        return id;
    }

    /**
     * @brief Time of the next scheduled event.
     * @return Absolute timestamp of the upcoming event.
     */
    public double getNextEventTime() {
        return nextEventTime;
    }

    @Override
    public String toString() {
        return String.format("Source %d [%s] nextEvent=%.3f", id, state, nextEventTime);
    }
}
