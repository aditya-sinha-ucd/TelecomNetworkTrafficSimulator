/**
 * @file src/io/ConsolePrompter.java
 * @brief Centralizes console prompting logic and validation rules.
 * @details Works alongside {@link io.ConsoleUI} to request numeric/text input
 *          while providing consistent handling of quit commands. Encapsulating
 *          user input parsing keeps UI flow logic tidy and testable.
 * @date 2024-05-30
 */
package io;

import java.util.Scanner;

/**
 * @class ConsolePrompter
 * @brief Wraps {@link Scanner} to collect validated user responses.
 * @details Supports prompting for positive numbers, bounded doubles, integers,
 *          and yes/no answers. Collaborates exclusively with the console UI and
 *          signals exit when the user types `quit` or `q`.
 */
public class ConsolePrompter {

    private final Scanner scanner;

    /**
     * @brief Creates a prompter bound to a shared {@link Scanner} instance.
     * @param scanner Input source shared with the console UI.
     */
    public ConsolePrompter(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * @brief Displays a prompt and returns the entered line.
     * @param message Prompt shown to the user.
     * @return User-entered string (trimmed) unless a quit command terminates the app.
     */
    public String promptLine(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim();
        if (isQuitCommand(input)) {
            System.out.println("Exiting simulator...");
            System.exit(0);
        }
        return input;
    }

    /**
     * @brief Reads a strictly positive double value.
     * @param message Prompt shown to the user.
     * @return Positive double supplied via the console.
     */
    public double promptPositiveDouble(String message) {
        while (true) {
            String input = promptLine(message);
            try {
                double value = Double.parseDouble(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be positive. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid positive value.");
            }
        }
    }

    /**
     * @brief Reads an arbitrary double value.
     * @param message Prompt displayed to the user.
     * @return Parsed double result.
     */
    public double promptDouble(String message) {
        while (true) {
            String input = promptLine(message);
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    /**
     * @brief Reads a double within an exclusive range.
     * @param message Prompt displayed to the user.
     * @param minExclusive Lower bound (exclusive).
     * @param maxExclusive Upper bound (exclusive).
     * @return Double value strictly within the bounds.
     */
    public double promptDoubleInRange(String message, double minExclusive, double maxExclusive) {
        while (true) {
            String input = promptLine(message);
            try {
                double value = Double.parseDouble(input);
                if (value > minExclusive && value < maxExclusive) {
                    return value;
                }
                System.out.printf("Value must be in range (%.3f, %.3f). Try again.%n", minExclusive, maxExclusive);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid value.");
            }
        }
    }

    /**
     * @brief Reads a strictly positive integer value.
     * @param message Prompt displayed to the user.
     * @return Positive integer provided via the console.
     */
    public int promptPositiveInt(String message) {
        while (true) {
            String input = promptLine(message);
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be a positive integer. Try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Please try again.");
            }
        }
    }

    /**
     * @brief Reads a yes/no response.
     * @param message Prompt displayed to the user.
     * @return {@code true} for yes, {@code false} for no.
     */
    public boolean promptYesNo(String message) {
        while (true) {
            String input = promptLine(message).toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("Please answer with 'y' or 'n'.");
        }
    }

    /**
     * @brief Checks if the provided input matches the quit command.
     * @param value Raw user input.
     * @return {@code true} when the user requested to exit.
     */
    private boolean isQuitCommand(String value) {
        return value.equalsIgnoreCase("quit") || value.equalsIgnoreCase("q");
    }
}
