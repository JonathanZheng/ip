/**
 * Represents a task stored by SevenSix.
 */
public class Task {
    protected String description;
    protected boolean isDone;
    private final TaskType taskType;

    /**
     * Creates a task that is initially not done.
     *
     * @param description the text describing the task
     */
    public Task(String description) {
        this(description, TaskType.TODO);
    }

    /**
     * Creates a task with the specified type that is initially not done.
     *
     * @param description the text describing the task
     * @param taskType the type of this task
     */
    public Task(String description, TaskType taskType) {
        this.description = description;
        this.isDone = false;
        this.taskType = taskType;
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
     * Returns the letter that identifies this task's type in the user interface.
     *
     * @return the icon associated with this task's type
     */
    public String getTaskTypeIcon() {
        return taskType.getIcon();
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
        return "[" + getTaskTypeIcon() + "][" + getStatusIcon() + "] " + description;
    }
}
