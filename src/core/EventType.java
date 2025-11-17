/**
 * @file src/core/EventType.java
 * @brief Declares the discrete event categories processed by the simulator.
 * @details Used in conjunction with {@link core.Event} and
 *          {@link model.TrafficSource} to describe ON/OFF transitions for each
 *          network source. The enum values act as the primary inputs to the
 *          {@link core.Simulator}'s state machine logic.
 * @date 2024-05-30
 */
package core;

/**
 * @enum EventType
 * @brief Enumerates the mutually exclusive state changes a source can emit.
 * @details The enum doubles as documentation of the binary nature of the ON/OFF
 *          traffic sources used in this simulator. Consumers such as
 *          {@link core.Simulator}, {@link core.EventQueue}, and
 *          {@link model.TrafficSource} rely on these values to branch behavior.
 */
public enum EventType {
    /** Source transitions into the active (transmitting) state. */
    ON,

    /** Source transitions into the idle (silent) state. */
    OFF
}
