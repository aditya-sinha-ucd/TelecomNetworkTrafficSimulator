/**
 * @file src/io/ParetoSimulationHandler.java
 * @brief Console workflow for the classic Pareto ON/OFF simulation mode.
 * @details Collects configuration data, constructs {@link core.Simulator}, and
 *          reports completion to the user. Supports both manual and file-based
 *          parameter entry.
 * @date 2024-05-30
 */
package io;

import core.Simulator;
import model.SimulationParameters;

import java.io.IOException;
import java.util.Map;

/**
 * @class ParetoSimulationHandler
 * @brief Implements {@link SimulationModeHandler} for Pareto-driven sources.
 * @details Responsibilities include collecting parameters, instantiating the
 *          simulator, and handling error conditions gracefully.
 */
public class ParetoSimulationHandler implements SimulationModeHandler {

    private final ConsolePrompter prompter;

    /**
     * @brief Creates a handler bound to the shared input helper.
     * @param prompter Shared input helper used by the console UI.
     */
    public ParetoSimulationHandler(ConsolePrompter prompter) {
        this.prompter = prompter;
    }

    /**
     * @brief Drives the Pareto simulation from prompts to execution.
     */
    @Override
    public void run() {
        System.out.println("\n=== Pareto ON/OFF Traffic Simulation ===");
        SimulationParameters params = prompter.promptYesNo("Load parameters from a file? (y/n): ")
                ? loadFromFile()
                : promptManually();

        if (params == null) {
            System.out.println("Returning to main menu...\n");
            return;
        }

        System.out.println();
        System.out.println("Starting simulation with parameters:");
        System.out.println(params);
        System.out.println("-----------------------------------------------");

        try {
            Simulator simulator = new Simulator(params);
            simulator.run();
            System.out.println("\nSimulation finished successfully!");
        } catch (Exception e) {
            System.err.println("\nAn error occurred during simulation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @brief Interactively asks the user for all parameters needed to run the simulator.
     * @return Populated {@link SimulationParameters} instance.
     */
    private SimulationParameters promptManually() {
        double totalTime = prompter.promptPositiveDouble("Enter total simulation time (seconds): ");
        int numSources = prompter.promptPositiveInt("Enter number of traffic sources: ");
        double onShape = prompter.promptPositiveDouble("Enter Pareto shape parameter for ON duration (alpha): ");
        double onScale = prompter.promptPositiveDouble("Enter Pareto scale (minimum) for ON duration: ");
        double offShape = prompter.promptPositiveDouble("Enter Pareto shape parameter for OFF duration (alpha): ");
        double offScale = prompter.promptPositiveDouble("Enter Pareto scale (minimum) for OFF duration: ");
        double samplingInterval = prompter.promptPositiveDouble("Enter sampling interval (seconds): ");

        SimulationParameters params = SimulationParameters.fromUserInput(
                totalTime, numSources, onShape, onScale, offShape, offScale);
        params.samplingInterval = samplingInterval;
        return params;
    }

    /**
     * @brief Loads simulation parameters from a configuration file selected by the user.
     * @return {@link SimulationParameters} parsed from disk, or {@code null} if aborted.
     */
    private SimulationParameters loadFromFile() {
        while (true) {
            String path = prompter.promptLine("Enter path to configuration file (e.g., config.txt): ");
            try {
                Map<String, Double> params = ConfigFileLoader.loadConfig(path);
                ConfigFileLoader.printLoadedParameters(params);

                double totalTime = require(params, "totalTime");
                double numSources = require(params, "numSources");
                double onShape = require(params, "onShape");
                double onScale = require(params, "onScale");
                double offShape = require(params, "offShape");
                double offScale = require(params, "offScale");

                SimulationParameters simParams = new SimulationParameters(
                        totalTime,
                        (int) Math.round(numSources),
                        onShape, onScale,
                        offShape, offScale
                );

                if (params.containsKey("samplingInterval")) {
                    double dt = params.get("samplingInterval");
                    if (dt > 0) {
                        simParams.samplingInterval = dt;
                    }
                }
                return simParams;
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
     * @brief Ensures a required field is present in the loaded map.
     * @param params Map from configuration file.
     * @param key Required key.
     * @return Value associated with {@code key}.
     * @throws IllegalArgumentException if the key is missing.
     */
    private double require(Map<String, Double> params, String key) {
        if (!params.containsKey(key)) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        return params.get(key);
    }
}
