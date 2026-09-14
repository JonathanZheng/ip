package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

/** Verifies complete console output and termination while restoring process-wide streams after each test. */
@ResourceLock("SYSTEM_STREAMS")
@ResourceLock("SYSTEM_PROPERTIES")
class ConsoleUiTest {
    /** Separators are asserted independently of Ui's private implementation. */
    private static final String SEPARATOR = "____________________________________________________________";
    /** Greeting text expected by both console startup paths. */
    private static final String GREETING = "Hello! I'm SevenSix, your productivity thought partner."
            + System.lineSeparator() + "Which deliverables are we unlocking today?";
    /** Each chatbot uses a separate task file. */
    @TempDir
    private Path temporaryDirectory;

    /** Healthy startup should print exactly one greeting block. */
    @Test
    void printGreetingWithoutWarningPrintsOneBlock() {
        try (ConsoleCapture console = new ConsoleCapture("")) {
            Ui.printGreeting("");

            assertEquals(block(GREETING), console.getOutput());
        }
    }

    /** Storage warnings should follow the greeting in their own clearly labeled block. */
    @Test
    void printGreetingWithWarningPrintsSeparateErrorBlock() {
        try (ConsoleCapture console = new ConsoleCapture("")) {
            Ui.printGreeting("storage unavailable");

            assertEquals(block(GREETING) + block("Flagging a blocker: storage unavailable"), console.getOutput());
        }
    }

    /** An empty input stream should terminate without output or an exception. */
    @Test
    void runConsoleLoopStopsAtImmediateEndOfInput() {
        SevenSix chatbot = new SevenSix(temporaryDirectory.resolve("tasks.txt"));
        try (ConsoleCapture console = new ConsoleCapture("")) {
            Ui.runConsoleLoop(chatbot);

            assertEquals("", console.getOutput());
        }
    }

    /** End-of-input is valid even when the user has not typed bye. */
    @Test
    void runConsoleLoopProcessesFinalLineWithoutNewline() {
        SevenSix chatbot = new SevenSix(temporaryDirectory.resolve("tasks.txt"));
        try (ConsoleCapture console = new ConsoleCapture("list")) {
            Ui.runConsoleLoop(chatbot);

            assertEquals(block("Your pipeline is empty. Nothing to action right now."), console.getOutput());
        }
    }

    /** A valid bye stops processing subsequent lines, including with Windows line endings. */
    @Test
    void runConsoleLoopStopsAfterBye() {
        SevenSix chatbot = new SevenSix(temporaryDirectory.resolve("tasks.txt"));
        try (ConsoleCapture console = new ConsoleCapture("bye now\r\n  bye\t\r\ntodo never added\r\n")) {
            Ui.runConsoleLoop(chatbot);

            assertEquals(block("Flagging a blocker: list, undo, and bye do not accept extra parameters.")
                    + block("Great sync. Let's touch base again soon!"), console.getOutput());
            assertEquals("Your pipeline is empty. Nothing to action right now.", chatbot.getResponse("list"));
        }
    }

    /** The console entry point must honor its configured data file and produce the full conversation. */
    @Test
    void mainUsesConfiguredFileAndPrintsGreetingAndFarewell() {
        String previousPath = System.getProperty("sevensix.data.file");
        try (ConsoleCapture console = new ConsoleCapture("bye\n")) {
            System.setProperty("sevensix.data.file", temporaryDirectory.resolve("tasks.txt").toString());
            SevenSix.main(new String[0]);

            assertEquals(block(GREETING) + block("Great sync. Let's touch base again soon!"), console.getOutput());
        } finally {
            if (previousPath == null) {
                System.clearProperty("sevensix.data.file");
            } else {
                System.setProperty("sevensix.data.file", previousPath);
            }
        }
    }

    /** Builds the expected platform-specific separators around a message.
     *
     * @param message the independently specified expected text.
     * @return one complete console block, including its final newline.
     */
    private static String block(String message) {
        return String.join(System.lineSeparator(), SEPARATOR, message, SEPARATOR, "");
    }

    /** Temporarily replaces standard input and output without closing the original streams. */
    private static class ConsoleCapture implements AutoCloseable {
        private final InputStream originalInput = System.in;
        private final PrintStream originalOutput = System.out;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final PrintStream replacementOutput = new PrintStream(output, true, StandardCharsets.UTF_8);

        /** Installs in-memory streams for one console test.
         *
         * @param input the commands to supply as UTF-8 text.
         */
        ConsoleCapture(String input) {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(replacementOutput);
        }

        /** Returns all text printed by the console so far.
         *
         * @return captured UTF-8 output.
         */
        String getOutput() {
            return output.toString(StandardCharsets.UTF_8);
        }

        /** Restores global streams even when a test assertion fails. */
        @Override
        public void close() {
            System.setIn(originalInput);
            System.setOut(originalOutput);
            replacementOutput.close();
        }
    }
}
