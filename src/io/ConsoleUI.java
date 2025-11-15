package io;

import java.util.Scanner;

/**
 * Handles all console interaction with the user.
 * <p>
 * The UI now delegates the actual workflows to dedicated mode handlers so the
 * console loop simply selects a mode, runs it, and optionally repeats.
 */
public class ConsoleUI {

    private final ConsolePrompter prompter;

    public ConsoleUI() {
        this.prompter = new ConsolePrompter(new Scanner(System.in));
    }

    /** Displays the simulator banner. */
    private void printBanner() {
        System.out.println("===============================================");
        System.out.println("     TELECOM NETWORK TRAFFIC SIMULATOR 2025     ");
        System.out.println("===============================================");
        System.out.println("Type 'quit' or 'q' at any prompt to exit.");
        System.out.println();
    }

    /** Starts the console-driven interaction and launches the simulator. */
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
