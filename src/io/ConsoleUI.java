/**
 * @file src/io/ConsoleUI.java
 * @brief Primary CLI entry point for the Telecom Network Traffic Simulator.
 * @details Presents menu options, delegates actual simulation execution to
 *          {@link io.SimulationModeHandler} implementations, and coordinates
 *          repeated runs. Works with {@link ConsolePrompter} to collect user
 *          input and can launch Pareto or FGN-based experiments.
 * @date 2024-05-30
 */
package io;

import java.util.Scanner;

/**
 * @class ConsoleUI
 * @brief Handles all console interaction with the user.
 * @details Focuses on prompting and dispatching simulation modes while
 *          delegating heavy lifting to {@link SimulationModeHandler}
 *          implementations. Input: user menu selections and parameters via the
 *          {@link ConsolePrompter}. Output: triggered simulation runs and user
 *          feedback.
 */
public class ConsoleUI {

    private final ConsolePrompter prompter;

    /**
     * @brief Creates a UI backed by {@link java.util.Scanner}-based prompts.
     */
    public ConsoleUI() {
        this.prompter = new ConsolePrompter(new Scanner(System.in));
    }

    /**
     * @brief Displays the simulator banner and general instructions.
     */
    private void printBanner() {
        System.out.println("===============================================");
        System.out.println("      TELECOM NETWORK TRAFFIC SIMULATOR        ");
        System.out.println("===============================================");
        System.out.println("Type 'quit' or 'q' at any prompt to exit.");
        System.out.println();
    }

    /**
     * @brief Starts the console-driven interaction and launches simulations.
     * @details Loops until the user declines another run. For each cycle the
     *          UI asks for a mode, invokes the corresponding handler, then
     *          prompts whether to continue.
     */
    public void start() {
        printBanner();
        boolean continueRunning = true;
        while (continueRunning) {
            SimulationModeHandler handler = selectMode();
            handler.run();
            continueRunning = prompter.promptYesNo("Would you like to run another simulation? (y/n): ");
            System.out.println();
        }
        System.out.println("Thank you for using the Telecom Network Traffic Simulator. Goodbye!");
    }

    /**
     * @brief Displays the list of available modes and returns the selection.
     * @return Handler representing the requested simulation workflow.
     */
    private SimulationModeHandler selectMode() {
        System.out.println("Select simulation mode:");
        System.out.println("1 - Pareto ON/OFF (event-driven traffic model)");
        System.out.println("2 - Fractional Gaussian Noise (FGN generator)");
        while (true) {
            String choice = prompter.promptLine("Enter choice (1 or 2): ");
            switch (choice) {
                case "1":
                    return new ParetoSimulationHandler(prompter);
                case "2":
                    return new FGNSimulationHandler(prompter);
                default:
                    System.out.println("Invalid choice. Please select 1 or 2.");
            }
        }
    }
}
