import io.ConsoleUI;

/**
 * Application entry point for the Telecom Network Traffic Simulator.
 * <p>
 * The {@code Main} class simply boots the {@link io.ConsoleUI} so that the
 * user can choose between the different simulation modes.
 */
public class Main {

    /**
     * Launches the console UI loop and blocks until the user exits.
     *
     * @param args standard command-line arguments (unused)
     */
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
