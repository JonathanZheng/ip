package duke;

import java.util.ArrayList;
import java.util.List;

/**
 * Remembers the task list as it was before the most recent task-changing command.
 *
 * <p>The snapshot holds independent copies of the tasks, so later changes to the live task
 * list cannot alter what an undo command restores. Only one snapshot is kept, which is what
 * limits the chatbot to undoing a single command.
 */
public class UndoHistory {
    /** Copies of the tasks as they were before the most recent task-changing command. */
    private List<Task> savedTasks;

    /**
     * Replaces the snapshot with independent copies of the supplied tasks.
     *
     * @param tasks the tasks to remember.
     */
    public void save(Iterable<Task> tasks) {
        savedTasks = new ArrayList<>();
        for (Task task : tasks) {
            savedTasks.add(copyTask(task));
        }
    }

    /**
     * Checks whether there is a snapshot available to restore.
     *
     * @return {@code true} when a task-changing command has been recorded.
     */
    public boolean hasSnapshot() {
        return savedTasks != null;
    }

    /**
     * Returns the snapshot and clears it, so that each snapshot is restored at most once.
     *
     * @return the remembered tasks.
     */
    public List<Task> takeSnapshot() {
        assert hasSnapshot() : "Callers check hasSnapshot before taking the snapshot";
        List<Task> restoredTasks = savedTasks;
        savedTasks = null;
        return restoredTasks;
    }

    /**
     * Creates an independent copy of a task, including its type-specific details and status.
     *
     * @param task the task to copy.
     * @return an independent copy of the task.
     */
    private Task copyTask(Task task) {
        Task copiedTask;
        if (task instanceof Deadline deadline) {
            copiedTask = new Deadline(deadline.getDescription(), deadline.getBy(), deadline.getByTime());
        } else if (task instanceof Event event) {
            copiedTask = new Event(event.getDescription(), event.getFrom(), event.getFromTime(),
                    event.getTo(), event.getToTime());
        } else {
            copiedTask = new Todo(task.getDescription());
        }
        if (task.isDone()) {
            copiedTask.markAsDone();
        }
        return copiedTask;
    }
}
