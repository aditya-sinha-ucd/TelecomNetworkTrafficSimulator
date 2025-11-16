package io;

/**
 * Strategy interface that encapsulates the prompts and workflow for a specific
 * console simulation mode.
 */
public interface SimulationModeHandler {
    /** Executes the configured simulation or generation workflow. */
    void run();
}
