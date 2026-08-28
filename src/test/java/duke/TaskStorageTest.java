package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies persistence of task data, including missing and corrupted files.
 */
class TaskStorageTest {
    @TempDir
    Path temporaryDirectory;

    /**
     * Saving and loading should preserve task types, details, dates, times, and status.
     */
    @Test
    void saveAndLoad_mixedTasks_preservesTaskData() {
        Path dataFile = temporaryDirectory.resolve("nested").resolve("tasks.txt");
        TaskStorage storage = new TaskStorage(dataFile);

        Todo todo = new Todo("read | book\\");
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 6, 6),
                LocalTime.of(18, 0));
        deadline.markAsDone();
        Event event = new Event("project meeting", LocalDate.of(2019, 8, 6),
                LocalTime.of(14, 0), LocalDate.of(2019, 8, 6), LocalTime.of(16, 0));

        assertTrue(storage.save(List.of(todo, deadline, event)));

        List<Task> loadedTasks = storage.load();
        assertEquals(3, loadedTasks.size());
        assertEquals("read | book\\", loadedTasks.get(0).getDescription());
        assertFalse(loadedTasks.get(0).isDone());

        assertTrue(loadedTasks.get(1) instanceof Deadline);
        Deadline loadedDeadline = (Deadline) loadedTasks.get(1);
        assertEquals(LocalDate.of(2019, 6, 6), loadedDeadline.getBy());
        assertEquals(LocalTime.of(18, 0), loadedDeadline.getByTime());
        assertTrue(loadedDeadline.isDone());

        assertTrue(loadedTasks.get(2) instanceof Event);
        Event loadedEvent = (Event) loadedTasks.get(2);
        assertEquals(LocalDate.of(2019, 8, 6), loadedEvent.getFrom());
        assertEquals(LocalTime.of(14, 0), loadedEvent.getFromTime());
        assertEquals(LocalDate.of(2019, 8, 6), loadedEvent.getTo());
        assertEquals(LocalTime.of(16, 0), loadedEvent.getToTime());
        assertTrue(Files.exists(dataFile));
    }

    /**
     * Loading a missing file should return no tasks and create its parent directory.
     */
    @Test
    void load_missingFile_returnsEmptyListAndCreatesParentDirectory() {
        Path dataFile = temporaryDirectory.resolve("new-folder").resolve("tasks.txt");
        TaskStorage storage = new TaskStorage(dataFile);

        List<Task> loadedTasks = storage.load();

        assertTrue(loadedTasks.isEmpty());
        assertTrue(Files.isDirectory(dataFile.getParent()));
        assertFalse(Files.exists(dataFile));
    }

    /**
     * Invalid records should be skipped while valid records in the same file are loaded.
     */
    @Test
    void load_corruptedRecords_ignoresInvalidLines() throws IOException {
        Path dataFile = temporaryDirectory.resolve("corrupted.txt");
        Files.writeString(dataFile, String.join(System.lineSeparator(),
                "T | 1 | valid task",
                "not a valid record",
                "D | 0 | invalid date | not-a-date",
                "D | 0 | return book | 2019-06-06"));

        List<Task> loadedTasks = new TaskStorage(dataFile).load();

        assertEquals(2, loadedTasks.size());
        assertEquals("valid task", loadedTasks.get(0).getDescription());
        assertTrue(loadedTasks.get(0).isDone());
        assertNotNull(loadedTasks.get(1));
        assertEquals("return book", loadedTasks.get(1).getDescription());
    }
}
