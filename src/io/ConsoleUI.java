package io;

import core.Simulator;
import model.SimulationParameters;
import util.FractionalGaussianNoise;
import util.HurstEstimator;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

/**
 * Handles all console interaction with the user.
 * <p>
 * Responsibilities:
 *  - Prompt for simulation parameters or load from file.
 *  - Allow switching between Pareto ON/OFF and FGN modes.
 *  - Validate and sanitize user input.
 *  - Allow the user to quit gracefully at any prompt.
 */
public class ConsoleUI {

    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
    }

    /** Displays the simulator banner. */
    private void printBanner() {
        System.out.println("===============================================");
        System.out.println("     TELECOM NETWORK TRAFFIC SIMULATOR 2025     ");
        System.out.println("===============================================");
        System.out.println("Type 'quit' or 'q' at any prompt to exit.");
        System.out.println();
    }

    /**
     * Reads user input from the console.
     * Exits gracefully if the user types 'quit' or 'q'.
     */
    private String readInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("q")) {
            System.out.println("Exiting simulator...");
            System.exit(0);
        }
        return input;
    }

    /** Reads a positive double from the console. */
    private double readPositiveDouble(String prompt) {
        while (true) {
            String input = readInput(prompt);
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
            String input = readInput(prompt);
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

        // === Choose simulation mode ===
        System.out.println("Select simulation mode:");
        System.out.println("1 - Pareto ON/OFF (event-driven traffic model)");
        System.out.println("2 - Fractional Gaussian Noise (FGN generator)");
        String modeChoice = readInput("Enter choice (1 or 2): ");

        // =====================================================
        // === FGN Mode ===
        // =====================================================
        if (modeChoice.equals("2")) {
            System.out.println("\n=== Fractional Gaussian Noise Mode ===");
            double H = readPositiveDouble("Enter Hurst exponent (0.5 < H < 1.0): ");
            double sigma = readPositiveDouble("Enter standard deviation (σ): ");
            double mean = 0.0;
            int samples = readPositiveInt("Enter number of samples to generate: ");

            try {
                FractionalGaussianNoise fgn = new FractionalGaussianNoise(
                        H, sigma, mean, System.currentTimeMillis());
                double[] series = fgn.generate(samples);
                double estH = HurstEstimator.estimateHurst(Arrays.stream(series).boxed().toList());

                System.out.println("\nGenerated FGN sequence successfully!");
                System.out.printf("Target Hurst exponent: %.3f%n", H);
                System.out.printf("Estimated Hurst exponent (validation): %.3f%n", estH);
                System.out.println("FGN data generated in memory (can be exported later).");
                System.out.println("-----------------------------------------------");

            } catch (Exception e) {
                System.err.println("Error generating FGN sequence: " + e.getMessage());
            }
            return;
        }

        // =====================================================
        // === Pareto ON/OFF Mode (default) ===
        // =====================================================
        String firstInput = readInput("Type 'load' to load a config file or press Enter to continue: ");

        // === Load parameters from config file ===
        if (firstInput.equalsIgnoreCase("load")) {
            String filePath = readInput("Enter path to configuration file (e.g., config.txt): ");

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
                System.out.println(simParams);
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
        System.out.println(params);
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
