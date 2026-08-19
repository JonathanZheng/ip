/**
 * Identifies the kinds of tasks supported by SevenSix and their display icons.
 */
public enum TaskType {
    TODO("T"),
    DEADLINE("D"),
    EVENT("E");

    private final String icon;

    /**
     * Creates a task type with its one-letter display icon.
     *
     * @param icon the icon shown in the task list
     */
    TaskType(String icon) {
        this.icon = icon;
    }

    /**
     * Returns the icon used for this task type.
     *
     * @return the one-letter task type icon
     */
    public String getIcon() {
        return icon;
    }
}
