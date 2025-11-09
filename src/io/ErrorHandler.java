package io;

/**
 * Simple centralized error handler.
 * <p>
 * Used for consistent printing of error messages
 * and graceful program termination.
 */
public class ErrorHandler {

    /**
     * Prints an error message and optionally exits.
     *
     * @param message message to display
     * @param fatal   if true, terminates the program
     */
    public static void handleError(String message, boolean fatal) {
        System.err.println("ERROR: " + message);
        if (fatal) {
            System.err.println("Program terminated.");
            System.exit(1);
        }
    }

    /**
     * Prints a warning message (non-fatal).
     *
     * @param message message to display
     */
    public static void warn(String message) {
        System.out.println("WARNING: " + message);
    }
}
