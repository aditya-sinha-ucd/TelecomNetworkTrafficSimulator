package io;

import core.Simulator;
import model.SimulationParameters;
import util.FractionalGaussianNoise;
import util.HurstEstimator;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Handles all console interaction with the user.
 * - Prompt for simulation parameters or load from file.
 * - Allow switching between Pareto ON/OFF and FGN modes.
 * - Validate and sanitize user input.
 * - Allow the user to quit gracefully at any prompt.
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

    /** Reads user input; exits on 'quit' or 'q'. */
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

    /** Reads a double within (min, max). */
    private double readDoubleInRange(String prompt, double min, double max) {
        while (true) {
            String input = readInput(prompt);
            try {
                double value = Double.parseDouble(input);
                if (value > min && value < max) return value;
                System.out.printf("Value must be in range (%.2f, %.2f). Try again.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid value.");
            }
        }
    }

    /** Starts the console-driven interaction and launches the simulator. */
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

            double H = readDoubleInRange("Enter Hurst exponent (0.5 < H < 1.0): ", 0.5, 1.0);
            double sigma = readPositiveDouble("Enter standard deviation (σ): ");
            double mean = 0.0;
            int samples = readPositiveInt("Enter number of samples to generate: ");

            try {
                FractionalGaussianNoise fgn =
                        new FractionalGaussianNoise(H, sigma, mean, System.currentTimeMillis());
                double[] series = fgn.generate(samples);

                String ts = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
                String runDir = "output/run_" + ts + "/";
                new java.io.File(runDir).mkdirs();

                java.nio.file.Path csvPath = java.nio.file.Path.of(runDir, "traffic_data.csv");
                try (java.io.PrintWriter writer =
                             new java.io.PrintWriter(java.nio.file.Files.newBufferedWriter(csvPath))) {
                    writer.println("Index,Value");
                    for (int i = 0; i < series.length; i++) {
                        writer.printf("%d,%.10f%n", i, series[i]);
                    }
                }

                // Hurst estimation with safety checks
                final int MIN_SAMPLES = 512;
                final double MIN_VAR = 1e-12;
                double meanVal = java.util.Arrays.stream(series).average().orElse(0.0);
                double variance = java.util.Arrays.stream(series)
                        .map(v -> (v - meanVal) * (v - meanVal))
                        .average().orElse(0.0);

                Double estH = Double.NaN;
                if (series.length >= MIN_SAMPLES && variance > MIN_VAR) {
                    estH = util.HurstEstimator.estimateHurst(
                            java.util.Arrays.stream(series).boxed()
                                    .collect(java.util.stream.Collectors.toList()));
                }

                // Write summary file
                java.nio.file.Path summaryPath = java.nio.file.Path.of(runDir, "summary.txt");
                try (java.io.PrintWriter sw =
                             new java.io.PrintWriter(java.nio.file.Files.newBufferedWriter(summaryPath))) {
                    sw.println("=== FGN Generation Summary ===");
                    sw.printf("Samples Generated: %d%n", samples);
                    sw.printf("Target Hurst (H): %.4f%n", H);
                    sw.printf("Sigma: %.6f%n", sigma);
                    sw.printf("Mean: %.6f%n", mean);
                    if (estH.isNaN()) sw.println("Estimated Hurst: not computed (increase samples or σ)");
                    else               sw.printf("Estimated Hurst: %.4f%n", estH);
                    sw.println("===============================");
                }

                // Console output
                if (estH.isNaN()) {
                    System.out.println("Estimated Hurst exponent (validation): not computed (increase samples or σ)");
                } else {
                    System.out.printf("Estimated Hurst exponent (validation): %.3f%n", estH);
                }
                System.out.printf("Time-series data exported to: %s%n", csvPath.toString());
                System.out.printf("Summary saved to: %s%n", summaryPath.toString());
                // no event log line for FGN
                System.out.printf("Results saved to: %s%n", runDir);
                System.out.println("\nSimulation finished successfully!");
                System.out.println("Results saved in the output/ directory.");
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

        // Load from config file
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

        // Manual input mode
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
