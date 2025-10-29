package io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

/**
 * Centralized error handling utility for the simulator.
 * <p>
 * Responsibilities:
 * - Display user-friendly error messages on the console.
 * - Log technical details to an error log file.
 * - Prevent the program from crashing abruptly.
 */
public final class ErrorHandler {

    /** Path to the error log file. */
    private static final String LOG_FILE = "output/error_log.txt";

    // Private constructor to prevent instantiation
    private ErrorHandler() {}

    /**
     * Handles a recoverable error by printing a friendly message
     * and writing details to the log file.
     *
     * @param message brief description shown to the user
     * @param e the caught exception (may be null)
     */
    public static void handleError(String message, Exception e) {
        System.err.println("[Error] " + message);
        logToFile(message, e);
    }

    /**
     * Writes error information to a persistent log file.
     */
    private static void logToFile(String message, Exception e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println("=== Error Logged at " + LocalDateTime.now() + " ===");
            writer.println("Message: " + message);
            if (e != null) {
                writer.println("Exception: " + e.getClass().getSimpleName());
                writer.println("Details: " + e.getMessage());
            }
            writer.println();
        } catch (IOException ioException) {
            System.err.println("[Critical] Could not write to error log: " + ioException.getMessage());
        }
    }

    /**
     * Handles unrecoverable errors and exits the program safely.
     */
    public static void fatal(String message, Exception e) {
        handleError(message, e);
        System.err.println("A fatal error occurred. The simulation will now exit.");
        System.exit(1);
    }
}
