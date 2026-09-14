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
                "Circling back on your ask. I've actioned this deliverable:",
                "  [T][ ] read book",
                "Your pipeline now holds 1 deliverable."), chatbot.getResponse("todo read book"));
        assertEquals("1.[T][ ] read book", chatbot.getResponse("list"));
    }

    /**
     * Listing an empty task list should provide visible feedback.
     */
    @Test
    void getResponseListWithNoTasksReturnsHelpfulMessage() {
        SevenSix chatbot = createChatbot();

        assertEquals("Your pipeline is empty. Nothing to action right now.",
                chatbot.getResponse("list"));
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
     * Invalid input should use a clearly labeled error response.
     */
    @Test
    void getResponseInvalidCommandReturnsHelpfulError() {
        SevenSix chatbot = createChatbot();

        assertEquals("Flagging a blocker: a todo needs a description."
                        + " Let us put some substance behind it.",
                chatbot.getResponse("todo"));
    }

    /**
     * Unknown commands should use the same error prefix as invalid task commands.
     */
    @Test
    void getResponseUnknownCommandReturnsLabeledError() {
        SevenSix chatbot = createChatbot();

        assertEquals("Flagging a blocker: that one is outside my wheelhouse. My core competencies"
                        + " are todo, deadline, event, list, mark, unmark, delete, and find.",
                chatbot.getResponse("blah"));
    }

    /**
     * Undoing an added task should restore the previous empty task list.
     */
    @Test
    void getResponseUndoAddTaskRestoresPreviousTaskList() {
        SevenSix chatbot = createChatbot();

        chatbot.getResponse("todo read book");

        assertEquals("Rolled back. Your pipeline is restored to its previous state.",
                chatbot.getResponse("undo"));
        assertEquals("Your pipeline is empty. Nothing to action right now.",
                chatbot.getResponse("list"));
        assertEquals("Flagging a blocker: there is nothing in the rollback history yet.",
                chatbot.getResponse("undo"));
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

        assertEquals("Rolled back. Your pipeline is restored to its previous state.",
                chatbot.getResponse("undo"));
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
