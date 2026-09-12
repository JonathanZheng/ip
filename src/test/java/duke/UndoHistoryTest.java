package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Verifies that the remembered task list is independent of later changes and is restored once.
 */
class UndoHistoryTest {
    @Test
    void hasSnapshotBeforeAnySaveReturnsFalse() {
        assertFalse(new UndoHistory().hasSnapshot());
    }

    @Test
    void takeSnapshotAfterSaveReturnsRememberedTasksAndClearsHistory() {
        UndoHistory history = new UndoHistory();
        history.save(new TaskList(List.of(new Todo("read book"))));

        assertTrue(history.hasSnapshot());
        List<Task> restoredTasks = history.takeSnapshot();

        assertEquals(1, restoredTasks.size());
        assertEquals("read book", restoredTasks.get(0).getDescription());
        assertFalse(history.hasSnapshot());
    }

    @Test
    void takeSnapshotAfterTaskIsMarkedReturnsStatusFromBeforeTheChange() {
        Todo task = new Todo("read book");
        UndoHistory history = new UndoHistory();
        history.save(new TaskList(List.of(task)));

        task.markAsDone();

        assertFalse(history.takeSnapshot().get(0).isDone());
    }

    @Test
    void saveReplacesThePreviousSnapshot() {
        UndoHistory history = new UndoHistory();
        history.save(new TaskList(List.of(new Todo("read book"))));
        history.save(new TaskList(List.of(new Todo("join sports club"), new Todo("borrow book"))));

        assertEquals(2, history.takeSnapshot().size());
    }
}
