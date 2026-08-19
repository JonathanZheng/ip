/**
 * Represents a task that starts and ends at specified dates or times.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /**
     * Creates an event task that is initially not done.
     *
     * @param description the text describing the task
     * @param from the start date or time, stored exactly as entered
     * @param to the end date or time, stored exactly as entered
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the letter used for event tasks in the user interface.
     *
     * @return {@code E}
     */
    @Override
    public String getTaskTypeIcon() {
        return "E";
    }

    /**
     * Returns the event task with its start and end dates or times.
     *
     * @return the formatted event task
     */
    @Override
    public String toString() {
        return super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
