package duke;

/** Remembers the latest successful task change and its independent pre-command state. */
public class UndoHistory {
    private UndoEntry savedEntry;

    /** Remembers a change before its action modifies the live tasks.
     *
     * @param tasks the task list before the change.
     * @param type the action about to be performed.
     * @param task the affected task before the action.
     */
    public void save(Iterable<Task> tasks, ChangeType type, Task task) {
        savedEntry = new UndoEntry(tasks, type, task);
    }

    /** Checks whether a change is available to undo.
     *
     * @return true when an entry is available.
     */
    public boolean hasSnapshot() {
        return savedEntry != null;
    }

    /** Copies the history for recovery after a failed command.
     *
     * @return an independently consumable history sharing only immutable entries.
     */
    public UndoHistory copy() {
        UndoHistory copy = new UndoHistory();
        copy.savedEntry = savedEntry;
        return copy;
    }

    /** Consumes the saved entry exactly once.
     *
     * @return the change to reverse.
     */
    public UndoEntry takeEntry() {
        assert hasSnapshot() : "Callers check hasSnapshot before taking an entry";
        UndoEntry entry = savedEntry;
        savedEntry = null;
        return entry;
    }
}
