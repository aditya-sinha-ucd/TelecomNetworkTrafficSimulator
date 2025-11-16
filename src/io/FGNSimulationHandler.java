package io;

import model.FGNGenerationParameters;
import util.FractionalGaussianNoise;

import java.io.IOException;
import java.util.Map;

/**
 * Handles console interaction and execution for the FGN generator mode.
 */
public class FGNSimulationHandler implements SimulationModeHandler {

    private final ConsolePrompter prompter;

    public FGNSimulationHandler(ConsolePrompter prompter) {
        this.prompter = prompter;
    }

    @Override
    public void run() {
        System.out.println("\n=== Fractional Gaussian Noise Mode ===");
        FGNGenerationParameters params = prompter.promptYesNo("Load parameters from a file? (y/n): ")
                ? loadFromFile()
                : promptManually();

        if (params == null) {
            System.out.println("Returning to main menu...\n");
            return;
        }

        printConfiguration(params);

        try {
            FractionalGaussianNoise generator = new FractionalGaussianNoise(
                    params.getHurst(), params.getSigma(), params.getSeed());
            double[] series = generator.generate(params.getSampleCount());

            FileOutputManager outputManager = new FileOutputManager();
            outputManager.saveFGNResults(
                    series,
                    params.getHurst(),
                    params.getSigma(),
                    params.getSamplingInterval(),
                    params.getThreshold());
            outputManager.close();

            System.out.println("\nFGN sequence generated successfully!");
            System.out.printf("Samples written: %d%n", params.getSampleCount());
            System.out.printf("Sampling interval: %.4f seconds%n", params.getSamplingInterval());
            System.out.println("-----------------------------------------------");
        } catch (Exception e) {
            System.err.println("Error generating FGN sequence: " + e.getMessage());
        }
    }

    private void printConfiguration(FGNGenerationParameters params) {
        System.out.println("\nGenerating FGN with:");
        System.out.printf("  Hurst exponent (H): %.4f%n", params.getHurst());
        System.out.printf("  Sigma: %.6f%n", params.getSigma());
        System.out.printf("  Samples: %d%n", params.getSampleCount());
        System.out.printf("  Sampling interval: %.4f seconds%n", params.getSamplingInterval());
        System.out.printf("  Threshold: %.6f%n", params.getThreshold());
        System.out.println("-----------------------------------------------");
    }

    private FGNGenerationParameters promptManually() {
        double H = prompter.promptDoubleInRange("Enter Hurst exponent (0.5 < H < 1.0): ", 0.5, 1.0);
        double sigma = prompter.promptPositiveDouble("Enter standard deviation (σ): ");
        int samples = prompter.promptPositiveInt("Enter number of samples to generate: ");
        double dt = prompter.promptPositiveDouble("Enter sampling interval between samples (seconds): ");
        double threshold = prompter.promptDouble("Enter ON/OFF threshold for event log (e.g., 0): ");
        long seed = System.currentTimeMillis();
        warnIfHurstEstimateUnreliable(samples);
        return new FGNGenerationParameters(H, sigma, samples, dt, threshold, seed);
    }

    private FGNGenerationParameters loadFromFile() {
        while (true) {
            String path = prompter.promptLine("Enter path to FGN configuration file: ");
            try {
                Map<String, Double> params = ConfigFileLoader.loadConfig(path);
                ConfigFileLoader.printLoadedParameters(params);

                double hurst = require(params, "hurst");
                double sigma = require(params, "sigma");
                int samples = (int) Math.round(require(params, "samples"));

                double dt = params.getOrDefault("samplingInterval", 1.0);
                double threshold = params.getOrDefault("threshold", 0.0);
                long seed = params.containsKey("seed") ? params.get("seed").longValue() : System.currentTimeMillis();

                if (hurst <= 0.5 || hurst >= 1.0) {
                    throw new IllegalArgumentException("hurst must satisfy 0.5 < H < 1.0");
                }
                if (sigma <= 0) {
                    throw new IllegalArgumentException("sigma must be positive");
                }
                if (samples <= 0) {
                    throw new IllegalArgumentException("samples must be positive");
                }
                if (dt <= 0) {
                    throw new IllegalArgumentException("samplingInterval must be positive");
                }

                warnIfHurstEstimateUnreliable(samples);
                return new FGNGenerationParameters(hurst, sigma, samples, dt, threshold, seed);
            } catch (IOException e) {
                System.err.println("Error loading configuration file: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Configuration error: " + e.getMessage());
            }

            if (!prompter.promptYesNo("Would you like to try another file? (y/n): ")) {
                return null;
            }
        }
    }

    private void warnIfHurstEstimateUnreliable(int samples) {
        if (samples < FileOutputManager.MIN_FGN_HURST_SAMPLES) {
            System.out.printf("Warning: Hurst estimation requires at least %d samples; only %d requested.%n",
                    FileOutputManager.MIN_FGN_HURST_SAMPLES, samples);
        }
    }

    private double require(Map<String, Double> params, String key) {
        if (!params.containsKey(key)) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        return params.get(key);
    }
}
