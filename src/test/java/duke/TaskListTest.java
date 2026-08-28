package duke;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Verifies searching tasks by their descriptions.
 */
class TaskListTest {
    @Test
    void find_keyword_returnsCaseInsensitivePartialMatchesInOrder() {
        TaskList tasks = new TaskList(List.of(
                new Todo("read book"),
                new Todo("join sports club"),
                new Deadline("return BOOK", LocalDate.of(2019, 6, 6))));

        List<Task> matchingTasks = tasks.find("book");

        assertEquals(2, matchingTasks.size());
        assertEquals("read book", matchingTasks.get(0).getDescription());
        assertEquals("return BOOK", matchingTasks.get(1).getDescription());
        assertEquals(3, tasks.size());
    }

    @Test
    void find_keywordWithNoMatches_returnsEmptyList() {
        TaskList tasks = new TaskList(List.of(new Todo("read book")));

        List<Task> matchingTasks = tasks.find("holiday");

        assertTrue(matchingTasks.isEmpty());
    }
}
