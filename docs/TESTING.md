# Running the automated tests

The project relies on standard JUnit 5 tests so you can execute the entire
regression suite directly from IntelliJ IDEA or from the command line.

## IntelliJ IDEA
1. Open the project folder in IntelliJ.
2. Let the IDE import the Maven configuration when prompted (or use
   **File → New → Project from Existing Sources…** and select `pom.xml`).
3. In the Project tool window, right-click the `test` directory and select
   **Run 'All Tests'**. IntelliJ will use its built-in JUnit runner and report the
   results in the Run tool window.

## Command line
The repository now ships with a small `pom.xml` so you can run the same tests
outside the IDE:

```bash
mvn test
```

Maven downloads JUnit 5 from Maven Central, compiles the sources in `src/` and
`test/`, and executes the suite via the Surefire plugin. The command exits with a
non-zero status if any test fails, making it safe to wire into CI pipelines.
