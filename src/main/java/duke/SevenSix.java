package duke;

import java.nio.file.Path;
import java.util.List;

/**
 * Processes SevenSix commands and provides the console entry point.
 *
 * <p>This class decides what each command does. Understanding command text is delegated to
 * {@link Parser}, wording the replies to {@link Ui}, remembering the previous task list to
 * {@link UndoHistory}, and persistence to {@link TaskStorage}.
 */
public class SevenSix {
    /** Default relative path for persisted tasks. */
    private static final Path DEFAULT_DATA_FILE = Path.of("data", "duke.txt");
    /** System property that overrides the default data-file path during automated runs. */
    private static final String DATA_FILE_PROPERTY = "sevensix.data.file";

    /** The task storage used by this chatbot instance. */
    private final TaskStorage storage;
    /** The in-memory task list used by this chatbot instance. */
    private final TaskList tasks;
    /** The task list as it was before the most recent task-changing command. */
    private UndoHistory undoHistory = new UndoHistory();

    /**
     * Creates a chatbot using the configured data-file path.
     */
    public SevenSix() {
        this(new TaskStorage(resolveDataFile()));
    }

    /**
     * Creates a chatbot using the supplied data-file path.
     *
     * @param dataFile the path used to persist tasks.
     */
    public SevenSix(Path dataFile) {
        this(new TaskStorage(dataFile));
    }

    /** Initializes a chatbot using storage whose configuration has already been checked.
     *
     * @param storage the task storage to load.
     */
    private SevenSix(TaskStorage storage) {
        this.storage = storage;
        tasks = new TaskList(storage.load());
    }

    /**
     * Runs the original console interface.
     *
     * @param args command-line arguments, which are not used by this application.
     */
    public static void main(String[] args) {
        SevenSix chatbot = new SevenSix();
        Ui.printGreeting(chatbot.getStartupWarning());
        Ui.runConsoleLoop(chatbot);
    }

    /**
     * Resolves the data-file path from the optional system property.
     *
     * @return the configured data-file path.
     */
    private static String resolveDataFile() {
        String configuredPath = System.getProperty(DATA_FILE_PROPERTY);
        return configuredPath == null || configuredPath.isBlank()
                ? DEFAULT_DATA_FILE.toString()
                : configuredPath;
    }

    /**
     * Processes one command and returns the response that should be shown to the user.
     *
     * @param command the command entered by the user.
     * @return the chatbot response, without console separators.
     */
    public String getResponse(String command) {
        try {
            return processWithRollback(Parser.normalize(command));
        } catch (SevenSixException exception) {
            return Ui.formatError(exception.getMessage());
        }
    }

    /** Returns a startup warning for both console and graphical interfaces.
     *
     * @return the storage warning, or an empty string when loading succeeded.
     */
    public String getStartupWarning() {
        return storage.getLoadWarning();
    }

    /** Restores tasks and undo history whenever a command cannot finish successfully.
     *
     * @param command the normalized command.
     * @return the successful command response.
     * @throws SevenSixException if validation or saving fails.
     */
    private String processWithRollback(String command) throws SevenSixException {
        UndoHistory previousTasks = new UndoHistory();
        previousTasks.save(tasks);
        UndoHistory previousHistory = undoHistory.copy();
        try {
            return processCommand(command);
        } catch (SevenSixException exception) {
            tasks.replaceWith(previousTasks.takeSnapshot());
            undoHistory = previousHistory;
            throw exception;
        }
    }

    /**
     * Routes a normalized command to the handler for its command type.
     *
     * @param command the normalized command to process.
     * @return the response produced by the selected command handler.
     * @throws SevenSixException if the command contains invalid details or is unknown.
     */
    private String processCommand(String command) throws SevenSixException {
        return switch (Parser.parseCommandKeyword(command)) {
        case Parser.COMMAND_BYE -> Ui.getFarewellMessage();
        case Parser.COMMAND_HELP -> Ui.getHelpMessage();
        case Parser.COMMAND_UNDO -> undoLastCommand();
        case Parser.COMMAND_TODO -> addTodo(command);
        case Parser.COMMAND_DEADLINE -> addDeadline(command);
        case Parser.COMMAND_EVENT -> addEvent(command);
        case Parser.COMMAND_LIST -> listTasks();
        case Parser.COMMAND_MARK -> markTask(command);
        case Parser.COMMAND_UNMARK -> unmarkTask(command);
        case Parser.COMMAND_DELETE -> deleteTask(command);
        case Parser.COMMAND_FIND -> findTasks(command);
        default -> throw new SevenSixException(Ui.getUnknownCommandMessage());
        };
    }

    /**
     * Adds a to-do task and reports the updated number of stored tasks.
     *
     * @param command the complete to-do command.
     * @return the response for the added task.
     * @throws SevenSixException if the to-do description is empty.
     */
    private String addTodo(String command) throws SevenSixException {
        return addTask(new Todo(Parser.parseTodoDescription(command)));
    }

    /**
     * Adds a deadline task and reports the updated number of stored tasks.
     *
     * @param command the complete deadline command.
     * @return the response for the added task.
     * @throws SevenSixException if the deadline format or its details are invalid.
     */
    private String addDeadline(String command) throws SevenSixException {
        return addTask(Parser.parseDeadline(command));
    }

    /**
     * Adds an event task and reports the updated number of stored tasks.
     *
     * @param command the complete event command.
     * @return the response for the added task.
     * @throws SevenSixException if the event format or its details are invalid.
     */
    private String addEvent(String command) throws SevenSixException {
        return addTask(Parser.parseEvent(command));
    }

    /**
     * Stores a task and reports the updated number of stored tasks.
     *
     * @param task the task to store.
     * @return the response for the added task.
     * @throws SevenSixException if the task is invalid, duplicated, or cannot be saved.
     */
    private String addTask(Task task) throws SevenSixException {
        TaskValidation.validate(task);
        if (tasks.hasDuplicate(task)) {
            throw new SevenSixException(Ui.DUPLICATE_TASK);
        }
        undoHistory.save(tasks);
        tasks.add(task);
        saveTasks();
        return Ui.formatAddedTask(task, tasks.size());
    }

    /**
     * Returns every stored task with a one-based number.
     *
     * @return the formatted task list.
     */
    private String listTasks() {
        if (tasks.size() == 0) {
            return Ui.getEmptyListMessage();
        }
        return Ui.formatTaskList(tasks);
    }

    /**
     * Marks a task as done and reports the task to the user.
     *
     * @param command the complete mark command.
     * @return the response for the marked task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String markTask(String command) throws SevenSixException {
        Task task = getTask(Parser.parseTaskNumber(command, Parser.COMMAND_MARK));
        undoHistory.save(tasks);
        task.markAsDone();
        saveTasks();
        return Ui.formatMarkedTask(task);
    }

    /**
     * Marks a task as not done and reports the task to the user.
     *
     * @param command the complete unmark command.
     * @return the response for the unmarked task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String unmarkTask(String command) throws SevenSixException {
        Task task = getTask(Parser.parseTaskNumber(command, Parser.COMMAND_UNMARK));
        undoHistory.save(tasks);
        task.markAsNotDone();
        saveTasks();
        return Ui.formatUnmarkedTask(task);
    }

    /**
     * Deletes a task by its one-based number and reports the updated task count.
     *
     * @param command the complete delete command.
     * @return the response for the removed task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String deleteTask(String command) throws SevenSixException {
        int taskNumber = Parser.parseTaskNumber(command, Parser.COMMAND_DELETE);
        Task removedTask = getTask(taskNumber);
        undoHistory.save(tasks);
        tasks.remove(taskNumber - 1);
        saveTasks();
        return Ui.formatDeletedTask(removedTask, tasks.size());
    }

    /**
     * Finds tasks whose descriptions contain the requested keyword.
     *
     * @param command the complete find command.
     * @return the matching tasks or a no-matches message.
     * @throws SevenSixException if the search keyword is empty.
     */
    private String findTasks(String command) throws SevenSixException {
        List<Task> matchingTasks = tasks.find(Parser.parseFindKeyword(command));
        if (matchingTasks.isEmpty()) {
            return Ui.getNoMatchesMessage();
        }
        return Ui.formatMatchingTasks(matchingTasks);
    }

    /**
     * Restores the task list saved before the most recent task-changing command.
     *
     * @return the response for the undo command.
     * @throws SevenSixException if there is no task-changing command to undo.
     */
    private String undoLastCommand() throws SevenSixException {
        if (!undoHistory.hasSnapshot()) {
            throw new SevenSixException(Ui.EMPTY_UNDO_HISTORY);
        }
        tasks.replaceWith(undoHistory.takeSnapshot());
        saveTasks();
        return Ui.getUndoMessage();
    }

    /**
     * Returns a task by its one-based number.
     *
     * @param taskNumber the one-based task number.
     * @return the requested task.
     * @throws SevenSixException if the task number is not in the list.
     */
    private Task getTask(int taskNumber) throws SevenSixException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SevenSixException(Ui.TASK_NOT_FOUND);
        }
        return tasks.get(taskNumber - 1);
    }

    /**
     * Saves the task list after a command changes it.
     *
     * @throws SevenSixException if saving fails, so the caller can roll back the command.
     */
    private void saveTasks() throws SevenSixException {
        if (!storage.save(tasks)) {
            throw new SevenSixException(Ui.SAVE_FAILURE);
        }
    }
}
