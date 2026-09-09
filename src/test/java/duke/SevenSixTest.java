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
     * Unknown commands should use the same themed error prefix as invalid task commands.
     */
    @Test
    void getResponseUnknownCommandReturnsThemedError() {
        SevenSix chatbot = createChatbot();

        assertEquals("676767!!! I don't know that command yet. Try todo, deadline, event, list, mark,"
                        + " unmark, delete, or find.",
                chatbot.getResponse("blah"));
    }

    /**
     * Undoing an added task should restore the previous empty task list.
     */
    @Test
    void getResponseUndoAddTaskRestoresPreviousTaskList() {
        SevenSix chatbot = createChatbot();

        chatbot.getResponse("todo read book");

        assertEquals("OK, I've undone the last command.", chatbot.getResponse("undo"));
        assertEquals("There are no tasks in your list.", chatbot.getResponse("list"));
        assertEquals("676767!!! there is no command to undo.", chatbot.getResponse("undo"));
    }

    /**
     * Undoing a status change should restore the task and persist its previous state.
     */
    @Test
    void getResponseUndoMarkTaskRestoresAndPersistsPreviousState() {
        Path dataFile = temporaryDirectory.resolve("undo-tasks.txt");
        SevenSix chatbot = new SevenSix(dataFile);

        chatbot.getResponse("deadline submit report /by 2019-06-06");
        chatbot.getResponse("mark 1");

        assertEquals("OK, I've undone the last command.", chatbot.getResponse("undo"));
        assertEquals("1.[D][ ] submit report (by: Jun 06 2019)", chatbot.getResponse("list"));

        SevenSix reloadedChatbot = new SevenSix(dataFile);
        assertEquals("1.[D][ ] submit report (by: Jun 06 2019)",
                reloadedChatbot.getResponse("list"));
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
