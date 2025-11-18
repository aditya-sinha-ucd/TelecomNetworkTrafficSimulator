/**
 * @file src/io/FGNSimulationHandler.java
 * @brief Console workflow that configures FGN-driven simulations.
 * @details Guides the user through configuration, builds
 *          {@link model.FGNGenerationParameters}, converts them into
 *          {@link model.SimulationParameters}, and invokes the
 *          {@link core.Simulator}. Collaborates with {@link ConsolePrompter}
 *          for input collection and {@link ConfigFileLoader} for optional
 *          file-based parameters.
 * @date 2024-05-30
 */
package io;

import core.Simulator;
import model.FGNGenerationParameters;
import model.SimulationParameters;

import java.io.IOException;
import java.util.Map;

/**
 * @class FGNSimulationHandler
 * @brief Implements {@link SimulationModeHandler} for the FGN generator mode.
 * @details Responsibilities include parameter gathering (interactive or file
 *          based), generator bundle creation, conversion to
 *          {@link SimulationParameters}, and launching the shared
 *          {@link Simulator} pipeline.
 */
public class FGNSimulationHandler implements SimulationModeHandler {

    private final ConsolePrompter prompter;

    /**
     * @brief Creates a handler bound to the shared {@link ConsolePrompter}.
     * @param prompter Shared console input helper.
     */
    public FGNSimulationHandler(ConsolePrompter prompter) {
        this.prompter = prompter;
    }

    /**
     * @brief Executes the FGN generation workflow from prompt to export.
     */
    @Override
    public void run() {
        System.out.println("\n=== Fractional Gaussian Noise Mode ===");
        FGNInputBundle bundle = prompter.promptYesNo("Load parameters from a file? (y/n): ")
                ? loadFromFile()
                : promptManually();

        if (bundle == null) {
            System.out.println("Returning to main menu...\n");
            return;
        }

        FGNGenerationParameters generatorParams = bundle.parameters;
        int sourceCount = bundle.numberOfSources != null
                ? bundle.numberOfSources
                : prompter.promptPositiveInt("Enter number of FGN traffic sources: ");

        SimulationParameters simParams = buildSimulationParameters(generatorParams, sourceCount);

        printConfiguration(generatorParams, sourceCount);

        try {
            Simulator simulator = new Simulator(simParams);
            simulator.run();
            System.out.println("\nSimulation finished successfully!");
        } catch (Exception e) {
            System.err.println("Error running FGN simulation: " + e.getMessage());
        }
    }

    /**
     * @brief Echoes the chosen configuration to the console before generation.
     * @param params Fully specified generator settings.
     * @param numSources Number of FGN-driven sources to simulate.
     */
    private void printConfiguration(FGNGenerationParameters params, int numSources) {
        System.out.println("\nRunning FGN simulation with:");
        System.out.printf("  Hurst exponent (H): %.4f%n", params.getHurst());
        System.out.printf("  Sigma: %.6f%n", params.getSigma());
        System.out.printf("  Samples: %d%n", params.getSampleCount());
        System.out.printf("  Sampling interval: %.4f seconds%n", params.getSamplingInterval());
        System.out.printf("  Threshold: %.6f%n", params.getThreshold());
        System.out.printf("  Sources: %d%n", numSources);
        System.out.printf("  Total simulation time: %.4f seconds%n", params.getTotalDuration());
        System.out.println("-----------------------------------------------");
    }

    /**
     * @brief Collects generator parameters through interactive prompts.
     * @return Bundle containing generator parameters and the requested source count.
     */
    private FGNInputBundle promptManually() {
        double H = prompter.promptDoubleInRange("Enter Hurst exponent (0.5 < H < 1.0): ", 0.5, 1.0);
        double sigma = prompter.promptPositiveDouble("Enter standard deviation (σ): ");
        int samples = prompter.promptPositiveInt("Enter number of samples to generate: ");
        double dt = prompter.promptPositiveDouble("Enter sampling interval between samples (seconds): ");
        double threshold = prompter.promptDouble("Enter ON/OFF threshold for event log (e.g., 0): ");
        long seed = System.currentTimeMillis();
        warnIfHurstEstimateUnreliable(samples);
        int numSources = prompter.promptPositiveInt("Enter number of FGN traffic sources: ");
        return new FGNInputBundle(new FGNGenerationParameters(H, sigma, samples, dt, threshold, seed), numSources);
    }

    /**
     * @brief Loads generator parameters from a configuration file.
     * @return Bundle with parsed settings (and optional source count) or {@code null} if aborted.
     */
    private FGNInputBundle loadFromFile() {
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

                Integer fileSources = null;
                if (params.containsKey("numSources")) {
                    int parsedSources = (int) Math.round(params.get("numSources"));
                    if (parsedSources <= 0) {
                        throw new IllegalArgumentException("numSources must be positive");
                    }
                    fileSources = parsedSources;
                }

                FGNGenerationParameters generatorParams = new FGNGenerationParameters(
                        hurst, sigma, samples, dt, threshold, seed);
                return new FGNInputBundle(generatorParams, fileSources);
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

    /**
     * @brief Emits a warning if the requested series is too short for meaningful Hurst estimation.
     * @param samples Requested number of samples.
     */
    private void warnIfHurstEstimateUnreliable(int samples) {
        if (samples < FileOutputManager.MIN_FGN_HURST_SAMPLES) {
            System.out.printf("Warning: Hurst estimation requires at least %d samples; only %d requested.%n",
                    FileOutputManager.MIN_FGN_HURST_SAMPLES, samples);
        }
    }

    /**
     * @brief Validates that a configuration map contains a numeric value for the key.
     * @param params Parameter map loaded from disk.
     * @param key Required key.
     * @return Numeric value associated with {@code key}.
     * @throws IllegalArgumentException if the key is absent.
     */
    private double require(Map<String, Double> params, String key) {
        if (!params.containsKey(key)) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        return params.get(key);
    }

    /**
     * @brief Builds {@link SimulationParameters} configured for FGN-driven sources.
     * @param generatorParams Immutable FGN generation settings.
     * @param numSources Number of sources participating in the run.
     * @return Populated simulation parameters ready for {@link Simulator}.
     */
    private SimulationParameters buildSimulationParameters(FGNGenerationParameters generatorParams, int numSources) {
        SimulationParameters simParams = new SimulationParameters();
        simParams.trafficModel = SimulationParameters.TrafficModel.FGN_THRESHOLD;
        simParams.totalSimulationTime = generatorParams.getTotalDuration();
        simParams.numberOfSources = numSources;
        simParams.samplingInterval = generatorParams.getSamplingInterval();
        simParams.hurst = generatorParams.getHurst();
        simParams.fgnSigma = generatorParams.getSigma();
        simParams.fgnThreshold = generatorParams.getThreshold();
        simParams.fgnSeed = generatorParams.getSeed();
        return simParams;
    }

    /**
     * @class FGNInputBundle
     * @brief Aggregates generator parameters and an optional source count.
     */
    private static final class FGNInputBundle {
        final FGNGenerationParameters parameters;
        final Integer numberOfSources;

        FGNInputBundle(FGNGenerationParameters parameters, Integer numberOfSources) {
            this.parameters = parameters;
            this.numberOfSources = numberOfSources;
        }
    }
}
