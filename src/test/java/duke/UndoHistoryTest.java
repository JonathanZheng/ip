package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Verifies that the remembered task list is independent of later changes and is restored once.
 */
class UndoHistoryTest {
    /** Snapshots of dated tasks must preserve schedules and independently copy completion state. */
    @Test
    void saveCopiesDeadlineAndEventDetails() {
        LocalDate day = LocalDate.of(2024, 2, 29);
        Deadline deadline = new Deadline("report", day, LocalTime.NOON);
        Event event = new Event("call", day, LocalTime.NOON, day, LocalTime.of(13, 0));
        deadline.markAsDone();
        UndoHistory history = new UndoHistory();
        history.save(List.of(deadline, event));
        deadline.markAsNotDone();
        event.markAsDone();
        List<Task> restored = history.takeSnapshot();

        assertNotSame(deadline, restored.get(0));
        assertNotSame(event, restored.get(1));
        assertEquals("[D][X] report (by: Feb 29 2024 12:00 PM)", restored.get(0).toString());
        assertEquals("[E][ ] call (from: Feb 29 2024 12:00 PM to: Feb 29 2024 1:00 PM)", restored.get(1).toString());
    }

    /** An empty saved list is still a valid undo snapshot. */
    @Test
    void saveEmptyListCreatesConsumableSnapshot() {
        UndoHistory history = new UndoHistory();
        history.save(List.of());

        assertTrue(history.hasSnapshot());
        assertTrue(history.takeSnapshot().isEmpty());
        assertFalse(history.hasSnapshot());
    }

    /** Taking and modifying one history must not consume or modify its copy. */
    @Test
    void copyCreatesIndependentHistoryAndTasks() {
        UndoHistory original = new UndoHistory();
        original.save(List.of(new Todo("first")));
        UndoHistory copy = original.copy();
        original.takeSnapshot().get(0).markAsDone();

        assertFalse(original.hasSnapshot());
        assertTrue(copy.hasSnapshot());
        assertFalse(copy.takeSnapshot().get(0).isDone());
        assertFalse(new UndoHistory().copy().hasSnapshot());
    }

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
