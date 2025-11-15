package io;

import java.util.Scanner;

/**
 * Utility class responsible for prompting and validating console input.
 * Centralizing all console parsing keeps the UI code small and allows the
 * quit command to be handled consistently anywhere a prompt is shown.
 */
public class ConsolePrompter {

    private final Scanner scanner;

    public ConsolePrompter(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Displays a prompt and returns the entered line. */
    public String promptLine(String message) {
        System.out.print(message);
        String input = scanner.nextLine().trim();
        if (isQuitCommand(input)) {
            System.out.println("Exiting simulator...");
            System.exit(0);
        }
        return input;
    }

    /** Reads a strictly positive double value. */
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

    /** Reads a generic double value. */
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

    /** Reads a double within an exclusive range. */
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

    /** Reads a strictly positive integer value. */
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

    /** Reads a yes/no response. */
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

    /** Checks if the provided input matches the quit command. */
    private boolean isQuitCommand(String value) {
        return value.equalsIgnoreCase("quit") || value.equalsIgnoreCase("q");
    }
}
