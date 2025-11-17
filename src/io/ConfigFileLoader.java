/**
 * @file src/io/ConfigFileLoader.java
 * @brief Utility for reading simulation parameters from simple config files.
 * @details Supports `key=value` and JSON-like syntaxes, ignoring comments and
 *          formatting characters. Converts numeric values into a
 *          {@link java.util.Map} so higher layers (e.g., {@link io.ConsoleUI})
 *          can hydrate {@link model.SimulationParameters}. Inputs are file paths
 *          and outputs are string-to-double mappings.
 * @date 2024-05-30
 */
package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @class ConfigFileLoader
 * @brief Parses textual configuration files into parameter maps.
 * @details Consumed by the console UI when users opt to load settings from
 *          disk. The loader sanitizes input lines, splits on `=` or `:`, and
 *          handles both plain text and JSON-like structures.
 */
public class ConfigFileLoader {

    /**
     * @brief Reads simulation parameters from a configuration file.
     * @param path Path to the configuration file.
     * @return Map containing numeric parameters keyed by name.
     * @throws IOException If the file cannot be read.
     */
    public static Map<String, Double> loadConfig(String path) throws IOException {
        Map<String, Double> params = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // skip comments and braces
                if (line.isEmpty() || line.startsWith("#") || line.equals("{") || line.equals("}"))
                    continue;

                // remove quotes and commas
                line = line.replace("\"", "").replace(",", "");

                // split by = or :
                String[] parts = line.split("[=:]");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    try {
                        params.put(key, Double.parseDouble(value));
                    } catch (NumberFormatException e) {
                        System.err.println("Warning: skipping invalid numeric value for key " + key);
                    }
                }
            }
        }

        return params;
    }

    /**
     * @brief Prints loaded parameters to the console for confirmation.
     * @param params Map of loaded parameters.
     */
    public static void printLoadedParameters(Map<String, Double> params) {
        System.out.println("Loaded configuration parameters:");
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            System.out.printf("  %s = %.4f%n", entry.getKey(), entry.getValue());
        }
        System.out.println("-----------------------------------------------");
    }
}
