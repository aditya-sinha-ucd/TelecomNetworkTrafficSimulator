package io;

import core.Simulator;
import model.SimulationParameters;

import java.util.Map;
import java.util.Scanner;

/**
 * Handles all console interaction with the user.
 * <p>
 * Responsibilities:
 *  - Prompt for simulation parameters or load from file.
 *  - Validate and sanitize user input.
 *  - Display help and status messages.
 *  - Allow the user to quit gracefully.
 */
public class ConsoleUI {

    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /** Displays a banner at program start. */
    private void printBanner() {
        System.out.println("===============================================");
        System.out.println("     TELECOM NETWORK TRAFFIC SIMULATOR 2025     ");
        System.out.println("===============================================");
        System.out.println("Type 'quit' at any prompt to exit.");
        System.out.println();
    }

    /** Reads a positive double from the console. */
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

    /** Reads a positive integer from the console. */
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

        System.out.print("Type 'load' to load a config file or press Enter to continue: ");
        String firstInput = scanner.nextLine().trim();

        // === Load parameters from config file ===
        if (firstInput.equalsIgnoreCase("load")) {
            System.out.print("Enter path to configuration file (e.g., config.txt): ");
            String filePath = scanner.nextLine().trim();

            try {
                Map<String, Double> params = ConfigFileLoader.loadConfig(filePath);
                ConfigFileLoader.printLoadedParameters(params);

                SimulationParameters simParams = new SimulationParameters(
                        params.get("totalTime"),
                        params.get("numSources").intValue(),
                        params.get("onShape"), params.get("onScale"),
                        params.get("offShape"), params.get("offScale")
                );

                System.out.println("\nStarting simulation with loaded parameters...");
                System.out.println(simParams.toString());
                System.out.println("-----------------------------------------------");

                Simulator simulator = new Simulator(simParams);
                simulator.run();

                System.out.println("\nSimulation finished successfully!");
                System.out.println("Results saved in the output/ directory.");
                return;

            } catch (Exception e) {
                System.err.println("Error loading configuration file: " + e.getMessage());
                System.out.println("Falling back to manual input mode...\n");
            }
        }

        // === Manual input mode ===
        double totalTime = readPositiveDouble("Enter total simulation time (seconds): ");
        int numSources = readPositiveInt("Enter number of traffic sources: ");
        double onShape = readPositiveDouble("Enter Pareto shape parameter for ON duration (alpha): ");
        double onScale = readPositiveDouble("Enter Pareto scale (minimum) for ON duration: ");
        double offShape = readPositiveDouble("Enter Pareto shape parameter for OFF duration (alpha): ");
        double offScale = readPositiveDouble("Enter Pareto scale (minimum) for OFF duration: ");

        SimulationParameters params = SimulationParameters.fromUserInput(
                totalTime, numSources, onShape, onScale, offShape, offScale);

        System.out.println();
        System.out.println("Starting simulation with parameters:");
        System.out.println(params.toString());
        System.out.println("-----------------------------------------------");

        try {
            Simulator simulator = new Simulator(params);
            simulator.run();

            System.out.println("\nSimulation finished successfully!");
            System.out.println("Results saved in the output/ directory.");
        } catch (Exception e) {
            System.err.println("\nAn error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
