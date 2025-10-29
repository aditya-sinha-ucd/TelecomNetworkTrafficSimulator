package io;

import core.Simulator;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Handles all console interaction with the user.
 * <p>
 * Responsibilities:
 *  - Prompt for simulation parameters.
 *  - Validate and sanitize user input.
 *  - Display help and status messages.
 *  - Allow the user to quit gracefully.
 */
public class ConsoleUI {

    /** Shared Scanner instance for reading console input. */
    private final Scanner scanner;

    /** Constructs a ConsoleUI ready for user input. */
    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Displays a banner at program start.
     */
    private void printBanner() {
        System.out.println("===============================================");
        System.out.println("     TELECOM NETWORK TRAFFIC SIMULATOR 2025     ");
        System.out.println("===============================================");
        System.out.println("Type 'quit' at any prompt to exit.");
        System.out.println();
    }

    /**
     * Reads a positive double value from the console.
     *
     * @param prompt message shown to user
     * @return validated positive double
     */
    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
                System.out.println("Exiting simulator...");
                System.exit(0);
            }

            try {
                double value = Double.parseDouble(input);
                if (value > 0) return value;
                System.out.println("Value must be positive. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid positive value.");
            }
        }
    }

    /**
     * Reads a positive integer value from the console.
     *
     * @param prompt message shown to user
     * @return validated positive integer
     */
    private int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
                System.out.println("Exiting simulator...");
                System.exit(0);
            }

            try {
                int value = Integer.parseInt(input);
                if (value > 0) return value;
                System.out.println("Value must be positive. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid positive integer.");
            }
        }
    }

    /**
     * Starts the console-driven interaction and launches the simulator.
     */
    public void start() {
        printBanner();

        // Prompt for core simulation parameters
        double totalTime = readPositiveDouble("Enter total simulation time (seconds): ");
        int numSources = readPositiveInt("Enter number of traffic sources: ");
        double onShape = readPositiveDouble("Enter Pareto shape parameter for ON duration (alpha): ");
        double onScale = readPositiveDouble("Enter Pareto scale (minimum) for ON duration: ");
        double offShape = readPositiveDouble("Enter Pareto shape parameter for OFF duration (alpha): ");
        double offScale = readPositiveDouble("Enter Pareto scale (minimum) for OFF duration: ");

        System.out.println();
        System.out.println("Starting simulation with parameters:");
        System.out.printf("  Total time: %.1fs%n", totalTime);
        System.out.printf("  Sources: %d%n", numSources);
        System.out.printf("  ON (shape=%.2f, scale=%.2f)%n", onShape, onScale);
        System.out.printf("  OFF (shape=%.2f, scale=%.2f)%n", offShape, offScale);
        System.out.println("-----------------------------------------------");

        try {
            // Create and run simulator
            Simulator simulator = new Simulator(totalTime, numSources,
                    onShape, onScale,
                    offShape, offScale);
            simulator.run();

            System.out.println("\nSimulation finished successfully!");
            System.out.println("Results saved in the output/ directory.");
        } catch (Exception e) {
            System.err.println("\nAn error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
