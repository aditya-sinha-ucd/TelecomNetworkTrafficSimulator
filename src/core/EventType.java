
package core;

/**
 * Enum representing the two possible types of simulation events.
 * <p>
 * Each event corresponds to a transition in a TrafficSource's state.
 * <ul>
 *     <li>ON: The source starts transmitting (entering active state).</li>
 *     <li>OFF: The source stops transmitting (entering idle state).</li>
 * </ul>
 */
public enum EventType {
    ON,
    OFF
}
