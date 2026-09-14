package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests real console startup in a temporary working directory, never touching the user's default data file. */
class ConsoleProcessTest {
    /** Working directory for each child JVM and its default data folder. */
    @TempDir
    private Path temporaryDirectory;

    /** An absent configuration must create data/duke.txt relative to the process working directory. */
    @Test
    void mainWithoutConfiguredPathCreatesDefaultFile() throws Exception {
        String output = runConsole(null, "todo 读书\nbye\n");

        assertTrue(output.contains("Your pipeline now holds 1 deliverable."));
        assertEquals("T | 0 | 读书" + System.lineSeparator(),
                Files.readString(temporaryDirectory.resolve("data/duke.txt")));
    }

    /** Blank configuration uses the same default path as absent configuration.
     *
     * @param configuredPath the blank system-property value.
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void mainWithBlankConfiguredPathUsesDefaultFile(String configuredPath) throws Exception {
        runConsole(configuredPath, "todo first\nbye\n");

        assertEquals("T | 0 | first" + System.lineSeparator(),
                Files.readString(temporaryDirectory.resolve("data/duke.txt")));
    }

    /** Paths containing spaces and Unicode are valid and should be reloaded in a fresh JVM. */
    @Test
    void mainReloadsTasksFromUnicodePathWithSpaces() throws Exception {
        String configuredPath = "资料/task list.txt";
        runConsole(configuredPath, "todo 借书\nbye\n");
        String output = runConsole(configuredPath, "list\nbye\n");

        assertTrue(output.contains("1.[T][ ] 借书"));
        assertTrue(Files.exists(temporaryDirectory.resolve(configuredPath)));
    }

    /** A bad default path must produce a visible warning and remain intact. */
    @Test
    void mainReportsBlockedDefaultDataFolder() throws Exception {
        Files.writeString(temporaryDirectory.resolve("data"), "keep this file");
        String output = runConsole(null, "todo rejected\nlist\nbye\n");

        assertTrue(output.contains("Flagging a blocker: " + Ui.LOAD_FAILURE));
        assertTrue(output.contains("Flagging a blocker: " + Ui.SAVE_FAILURE));
        assertTrue(output.contains("Your pipeline is empty."));
        assertEquals("keep this file", Files.readString(temporaryDirectory.resolve("data")));
    }

    /** Runs the console with a bounded timeout and an isolated working directory.
     *
     * @param configuredPath the optional data-file property.
     * @param input the complete UTF-8 console input.
     * @return the combined output of a successful child JVM.
     * @throws Exception if launch, input, or process completion fails.
     */
    private String runConsole(String configuredPath, String input) throws Exception {
        Process process = new ProcessBuilder(launchCommand(configuredPath))
                .directory(temporaryDirectory.toFile()).redirectErrorStream(true).start();
        try {
            try (var commands = process.getOutputStream()) {
                commands.write(input.getBytes(StandardCharsets.UTF_8));
            }
            assertTrue(process.waitFor(15, TimeUnit.SECONDS), "Console process did not terminate");
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /** Uses the same JDK and, when available, coverage agent as the Gradle test worker.
     *
     * @param configuredPath the optional data-file property.
     * @return arguments for launching the production console entry point.
     * @throws Exception if the production class directory cannot be resolved.
     */
    private List<String> launchCommand(String configuredPath) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(), "-ea"));
        command.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(argument -> argument.startsWith("-javaagent:") && argument.contains("jacoco"))
                .map(ConsoleProcessTest::withAbsoluteCoverageDestination).toList());
        if (configuredPath != null) {
            command.add("-Dsevensix.data.file=" + configuredPath);
        }
        command.addAll(List.of("-Dfile.encoding=UTF-8", "-Duser.language=" + System.getProperty("user.language"),
                "-Duser.country=" + System.getProperty("user.country", ""), "-cp",
                Path.of(SevenSix.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                "duke.SevenSix"));
        return command;
    }

    /** Keeps child coverage in the parent's report despite the child's different working directory.
     *
     * @param argument the coverage agent argument inherited from Gradle.
     * @return the same argument with its execution-data destination made absolute.
     */
    private static String withAbsoluteCoverageDestination(String argument) {
        Matcher destination = Pattern.compile("destfile=([^,]+)").matcher(argument);
        if (!destination.find()) {
            return argument;
        }
        String absoluteDestination = "destfile=" + Path.of(destination.group(1)).toAbsolutePath();
        return destination.replaceFirst(Matcher.quoteReplacement(absoluteDestination));
    }
}
