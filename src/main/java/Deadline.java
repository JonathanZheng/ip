/**
 * Represents a task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    private final String by;

    /**
     * Creates a deadline task that is initially not done.
     *
     * @param description the text describing the task
    * @param by the due date or time, stored exactly as entered
     */
    public Deadline(String description, String by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    /**
     * Returns the deadline task with its due date or time.
     *
     * @return the formatted deadline task
     */
    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
