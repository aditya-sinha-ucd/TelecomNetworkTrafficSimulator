package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads simulation parameters from a simple text or JSON-like configuration file.
 * <p>
 * Supported formats (key=value per line or JSON-style):
 * <pre>
 * totalTime=1000
 * numSources=50
 * onShape=1.5
 * onScale=1.0
 * offShape=1.2
 * offScale=2.0
 * </pre>
 * or
 * <pre>
 * {
 *   "totalTime": 1000,
 *   "numSources": 50,
 *   "onShape": 1.5,
 *   "onScale": 1.0,
 *   "offShape": 1.2,
 *   "offScale": 2.0
 * }
 * </pre>
 * Lines starting with '#' are treated as comments.
 */
public class ConfigFileLoader {

    /**
     * Reads simulation parameters from a configuration file.
     *
     * @param path path to the configuration file
     * @return a Map<String, Double> containing numeric parameters
     * @throws IOException if the file cannot be read
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
     * Prints loaded parameters to the console for confirmation.
     *
     * @param params the map of loaded parameters
     */
    public static void printLoadedParameters(Map<String, Double> params) {
        System.out.println("Loaded configuration parameters:");
        for (Map.Entry<String, Double> entry : params.entrySet()) {
            System.out.printf("  %s = %.4f%n", entry.getKey(), entry.getValue());
        }
        System.out.println("-----------------------------------------------");
    }
}
