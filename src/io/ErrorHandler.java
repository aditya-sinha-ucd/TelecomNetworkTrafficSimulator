/**
 * @file src/io/ErrorHandler.java
 * @brief Centralized facility for reporting recoverable and fatal errors.
 * @details Ensures consistent messaging throughout the console UI and simulator
 *          core. Collaborates with classes such as {@link core.Simulator} and
 *          {@link io.ConsoleUI} that need to abort execution on unrecoverable
 *          input or runtime issues.
 * @date 2024-05-30
 */
package io;

/**
 * @class ErrorHandler
 * @brief Provides helpers for printing warnings and terminating failures.
 * @details Offers two static methods: {@link #handleError(String, boolean)} for
 *          fatal/non-fatal errors and {@link #warn(String)} for advisory
 *          messages. Inputs are user-facing strings; outputs are console
 *          messages and optional JVM termination.
 */
public class ErrorHandler {

    /**
     * @brief Prints an error message and optionally exits.
     * @param message Message to display.
     * @param fatal If {@code true}, terminates the program with exit code 1.
     */
    public static void handleError(String message, boolean fatal) {
        System.err.println("ERROR: " + message);
        if (fatal) {
            System.err.println("Program terminated.");
            System.exit(1);
        }
    }

    /**
     * @brief Prints a warning message (non-fatal).
     * @param message Message to display.
     */
    public static void warn(String message) {
        System.out.println("WARNING: " + message);
    }
}
