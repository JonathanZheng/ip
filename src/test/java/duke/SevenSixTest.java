package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies command processing shared by the console and JavaFX interfaces.
 */
class SevenSixTest {
    /** The temporary folder used to isolate task-storage tests. */
    @TempDir
    private Path temporaryDirectory;

    /**
     * Adding and listing a task should return the same responses used by the console interface.
     */
    @Test
    void getResponseAddAndListTaskReturnsExpectedResponses() {
        SevenSix chatbot = createChatbot();

        assertEquals(String.join(System.lineSeparator(),
                "Got it. I've added this task:",
                "  [T][ ] read book",
                "Now you have 1 task in the list."), chatbot.getResponse("todo read book"));
        assertEquals("1.[T][ ] read book", chatbot.getResponse("list"));
    }

    /**
     * Listing an empty task list should provide visible feedback.
     */
    @Test
    void getResponseListWithNoTasksReturnsHelpfulMessage() {
        SevenSix chatbot = createChatbot();

        assertEquals("There are no tasks in your list.", chatbot.getResponse("list"));
    }

    /**
     * A new chatbot should load tasks saved by an earlier chatbot instance.
     */
    @Test
    void getResponseSavedTaskIsReloadedReturnsPersistedTask() {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        SevenSix firstChatbot = new SevenSix(dataFile);
        firstChatbot.getResponse("todo saved task");

        SevenSix secondChatbot = new SevenSix(dataFile);

        assertEquals("1.[T][ ] saved task", secondChatbot.getResponse("list"));
    }

    /**
     * Invalid input should use the chatbot's themed error response.
     */
    @Test
    void getResponseInvalidCommandReturnsHelpfulError() {
        SevenSix chatbot = createChatbot();

        assertEquals("676767!!! a todo needs a description. Give it a little something to do!",
                chatbot.getResponse("todo"));
    }

    /**
     * Creates a chatbot with an isolated data file.
     *
     * @return a chatbot backed by the test directory.
     */
    private SevenSix createChatbot() {
        return new SevenSix(temporaryDirectory.resolve("tasks.txt"));
    }
}
