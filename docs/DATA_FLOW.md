# Telecom Simulator Data Flow

This document summarizes how user input progresses through the CLI, how each
simulation mode builds its configuration, and how the shared simulator turns
that configuration into CSV/event outputs.

## 1. Console layer

1. `src/Main.java` boots the CLI by creating a single `ConsoleUI` instance.
2. `ConsoleUI` shows the menu and defers all prompting logic to a shared
   `ConsolePrompter` so the rest of the workflow receives already-parsed
   numbers/choices.
3. Based on the user selection, the UI instantiates either
   `ParetoSimulationHandler` or `FGNSimulationHandler`. Both handlers implement
   the `SimulationModeHandler` contract so the UI can treat them uniformly.

## 2. Mode-specific handlers

### Pareto mode
1. `ParetoSimulationHandler` prompts (or loads a file) for ON/OFF Pareto
   settings, the number of sources, and the sampling interval.
2. Those values are assembled into a `SimulationParameters` object with
   `trafficModel` left at its default Pareto value.
3. The handler prints a summary to the console and invokes
   `new Simulator(params).run()`.

### FGN mode
1. `FGNSimulationHandler` collects `FGNGenerationParameters` either interactively
   or from a config file. That immutable bundle holds Hurst exponent, sigma,
   sample count, sampling interval, ON/OFF threshold, and RNG seed.
2. The handler converts the bundle into `SimulationParameters` by copying the
   sampling interval, using `params.getTotalDuration()` for the simulation time,
   storing the source count, and setting `trafficModel = FGN_THRESHOLD` along
   with the FGN-specific fields (hurst, sigma, threshold, seed).
3. It then calls `new Simulator(simParams).run()` so FGN experiments traverse
   the same pipeline as Pareto ones.

## 3. Simulator pipeline

1. The `Simulator` reads `SimulationParameters` once during construction and
   initializes core infrastructure: `EventQueue`, `SimulationClock`,
   `StatisticsCollector`, `NetworkQueue`, and an `OutputSink` (defaulting to
   `FileOutputManager`).
2. The simulator delegates source creation to `MultiSourceManager`:
   - Pareto runs call `generateSources()` to create `TrafficSource` objects with
     randomized Pareto durations.
   - FGN runs call `generateFGNSources()` so each `FGNTrafficSource` builds its
     own deterministic ON/OFF schedule by thresholding an FGN series.
3. Each source receives its first event and feeds it into the shared
   `EventQueue`.
4. The main loop repeatedly:
   - Pulls the next event, advances the clock, and samples aggregate ON ratios
     at fixed `samplingInterval`s before the event fires.
   - Lets the addressed source process the event and immediately schedule its
     next one.
   - Logs the event via the `OutputSink` and updates the running aggregate rate.
5. After the queue drains or the global horizon is reached, the simulator
   records any remaining samples, finalizes the `NetworkQueue`, and exports CSV
   plus textual summaries through the `OutputSink`.

Both modes therefore share a single entry point (`Simulator.run()`), identical
sampling/queue/export behavior, and only differ in how their traffic sources are
instantiated at startup.
