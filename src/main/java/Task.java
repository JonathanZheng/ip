/**
 * Represents a task stored by SevenSix.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task that is initially not done.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the marker used to show whether this task is done.
     *
     * @return {@code X} for a done task, or a space otherwise
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the task in the format used by list and status messages.
     *
     * @return the status marker and task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
