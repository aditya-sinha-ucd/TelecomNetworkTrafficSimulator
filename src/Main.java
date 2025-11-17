/**
 * @file Main.java
 * @brief Boots the Telecom Network Traffic Simulator console experience.
 * @details The {@code Main} class serves as the minimal entry point that
 *          constructs the {@link io.ConsoleUI} and hands off execution to its
 *          interactive loop. No simulation logic lives here; instead this class
 *          simply wires user interaction with the rest of the system.
 * @date 2024-05-30
 */
import io.ConsoleUI;

/**
 * @class Main
 * @brief Entry point that launches the {@link io.ConsoleUI}.
 * @details The class is intentionally lightweight; its sole responsibility is
 *          to instantiate the console user interface which in turn orchestrates
 *          configuration loading, simulation execution, and output management.
 *          It collaborates directly with {@link io.ConsoleUI} and indirectly
 *          with the simulator stack through that interface. The method
 *          signature mirrors the standard Java launcher contract.
 */
public class Main {

    /**
     * @brief Launches the console UI loop and blocks until termination.
     * @param args Standard command-line arguments (unused because configuration
     *             is provided interactively or via config files managed by the
     *             UI layer).
     */
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.start();
    }
}
