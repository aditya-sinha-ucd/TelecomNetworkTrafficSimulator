package io;

/**
 * Simple strategy interface so each simulation mode can encapsulate the
 * prompts it needs and the workflow for executing the mode.
 */
public interface SimulationModeHandler {
    void run();
}
