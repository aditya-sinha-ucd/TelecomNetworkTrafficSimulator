/**
 * @file src/io/SimulationModeHandler.java
 * @brief Strategy interface describing console-driven simulation workflows.
 * @details Implementations guide the user through parameter input and invoke
 *          the appropriate simulator components (e.g., Pareto vs. FGN). The UI
 *          depends on this abstraction to add new modes without changing the
 *          dispatcher logic.
 * @date 2024-05-30
 */
package io;

/**
 * @interface SimulationModeHandler
 * @brief Contract for launching a particular simulation or data-generation mode.
 * @details Implementations such as {@link io.ParetoSimulationHandler} and
 *          {@link io.FGNSimulationHandler} collect inputs and run the core
 *          simulation pipeline.
 */
public interface SimulationModeHandler {
    /**
     * @brief Executes the configured simulation or generation workflow.
     */
    void run();
}
