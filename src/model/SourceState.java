/**
 * @file src/model/SourceState.java
 * @brief Enumerates the ON/OFF states of a {@link model.TrafficSource}.
 * @details Used by {@link core.Simulator}, {@link core.Event}, and source
 *          implementations to track transitions during the discrete-event
 *          simulation.
 */
package model;

/**
 * @enum SourceState
 * @brief Identifies whether a source is actively generating traffic.
 */
public enum SourceState {
    /** Source is emitting traffic. */
    ON,
    /** Source is idle. */
    OFF
}
