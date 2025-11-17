/**
 * @file src/core/Event.java
 * @brief Immutable representation of a discrete simulator event.
 * @details Events are the atomic units processed by {@link core.Simulator}
 *          through the {@link core.EventQueue}. Each event indicates when a
 *          {@link model.TrafficSource} transitions between {@link model.SourceState}
 *          states, effectively driving the ON/OFF workload generation pipeline.
 *          Instances are lightweight value objects ordered by
 *          {@link #compareTo(Event)} for chronological scheduling.
 * @date 2024-05-30
 */
package core;

/**
 * @class Event
 * @brief Captures the time, source, and type of a discrete simulation event.
 * @details Collaborates with {@link core.EventQueue} (for ordering) and
 *          {@link core.Simulator} (for execution). Inputs include the event
 *          timestamp, the originating source ID, and the {@link core.EventType}
 *          describing the ON/OFF transition. The class outputs this structured
 *          information via accessors and comparison logic.
 */
public class Event implements Comparable<Event> {

    /** Simulation time (in seconds) when this event occurs. */
    private final double time;

    /** Unique identifier of the traffic source generating this event. */
    private final int sourceId;

    /** Type of event indicating ON or OFF state transitions. */
    private final EventType type;

    /**
     * @brief Constructs a new immutable event instance.
     * @param time Simulation time when the event occurs.
     * @param sourceId Identifier of the {@link model.TrafficSource} that
     *                 scheduled the event.
     * @param type {@link EventType} describing whether the source switches ON
     *             or OFF at {@code time}.
     */
    public Event(double time, int sourceId, EventType type) {
        this.time = time;
        this.sourceId = sourceId;
        this.type = type;
    }

    /**
     * @brief Exposes the absolute simulation time of the event.
     * @return Event timestamp expressed in seconds.
     */
    public double getTime() {
        return time;
    }

    /**
     * @brief Identifies which {@link model.TrafficSource} scheduled the event.
     * @return Zero-based source identifier as used by the simulator internals.
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * @brief Reveals the ON/OFF transition type carried by the event.
     * @return {@link EventType#ON} or {@link EventType#OFF} depending on the
     *         direction of the state transition.
     */
    public EventType getType() {
        return type;
    }

    /**
     * @brief Orders events chronologically for use in {@link core.EventQueue}.
     * @param other The event being compared.
     * @return Negative when this event occurs earlier, positive when later, and
     *         zero for simultaneous events.
     */
    @Override
    public int compareTo(Event other) {
        return Double.compare(this.time, other.time);
    }

    /**
     * @brief Builds a concise textual representation for logging/debugging.
     * @return Human-readable string summarizing time, source, and type.
     */
    @Override
    public String toString() {
        return String.format("[t=%.3f] Source %d -> %s", time, sourceId, type);
    }
}
