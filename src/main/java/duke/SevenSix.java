package duke;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Processes SevenSix commands and provides the console entry point.
 */
public class SevenSix {
    /** Separates the chatbot's greeting, responses, and prompts. */
    private static final String SEPARATOR = "____________________________________________________________";
    /** Command keyword for creating a to-do task. */
    private static final String COMMAND_TODO = "todo";
    /** Command keyword for creating a deadline task. */
    private static final String COMMAND_DEADLINE = "deadline";
    /** Command keyword for creating an event task. */
    private static final String COMMAND_EVENT = "event";
    /** Command keyword for marking a task as done. */
    private static final String COMMAND_MARK = "mark";
    /** Command keyword for marking a task as not done. */
    private static final String COMMAND_UNMARK = "unmark";
    /** Command keyword for deleting a task. */
    private static final String COMMAND_DELETE = "delete";
    /** Command keyword for searching task descriptions. */
    private static final String COMMAND_FIND = "find";
    /** Default relative path for persisted tasks. */
    private static final Path DEFAULT_DATA_FILE = Path.of("data", "duke.txt");
    /** System property that overrides the default data-file path during automated runs. */
    private static final String DATA_FILE_PROPERTY = "sevensix.data.file";

    /** The task storage used by this chatbot instance. */
    private final TaskStorage storage;
    /** The in-memory task list used by this chatbot instance. */
    private final TaskList tasks;

    /**
     * Creates a chatbot using the configured data-file path.
     */
    public SevenSix() {
        this(resolveDataFile());
    }

    /**
     * Creates a chatbot using the supplied data-file path.
     *
     * @param dataFile the path used to persist tasks.
     */
    public SevenSix(Path dataFile) {
        storage = new TaskStorage(dataFile);
        tasks = new TaskList(storage.load());
    }

    /**
     * Processes one command and returns the response that should be shown to the user.
     *
     * @param command the command entered by the user.
     * @return the chatbot response, without console separators.
     */
    public String getResponse(String command) {
        String normalizedCommand = command == null ? "" : command.trim();
        try {
            if (normalizedCommand.equals("bye")) {
                return "Bye. Hope to see you again soon!";
            }

            if (normalizedCommand.equals(COMMAND_TODO)
                    || normalizedCommand.startsWith(COMMAND_TODO + " ")) {
                return addTodo(normalizedCommand);
            } else if (normalizedCommand.equals(COMMAND_DEADLINE)
                    || normalizedCommand.startsWith(COMMAND_DEADLINE + " ")) {
                return addDeadline(normalizedCommand);
            } else if (normalizedCommand.equals(COMMAND_EVENT)
                    || normalizedCommand.startsWith(COMMAND_EVENT + " ")) {
                return addEvent(normalizedCommand);
            } else if (normalizedCommand.equals("list")) {
                return printTasks();
            } else if (normalizedCommand.equals(COMMAND_MARK)
                    || normalizedCommand.startsWith(COMMAND_MARK + " ")) {
                return markTask(normalizedCommand);
            } else if (normalizedCommand.equals(COMMAND_UNMARK)
                    || normalizedCommand.startsWith(COMMAND_UNMARK + " ")) {
                return unmarkTask(normalizedCommand);
            } else if (normalizedCommand.equals(COMMAND_DELETE)
                    || normalizedCommand.startsWith(COMMAND_DELETE + " ")) {
                return deleteTask(normalizedCommand);
            } else if (normalizedCommand.equals(COMMAND_FIND)
                    || normalizedCommand.startsWith(COMMAND_FIND + " ")) {
                return findTasks(normalizedCommand);
            }
            throw new SevenSixException(
                    "I don't know that command yet. Try todo, deadline, event, list, mark, unmark, delete,"
                            + " or find.");
        } catch (SevenSixException exception) {
            return exception.getMessage();
        }
    }

    /**
     * Runs the original console interface.
     *
     * @param args command-line arguments, which are not used by this application.
     */
    public static void main(String[] args) {
        SevenSix chatbot = new SevenSix();
        System.out.println(SEPARATOR);
        System.out.println("Hello! I'm SevenSix.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            System.out.println(SEPARATOR);
            System.out.println(chatbot.getResponse(command));
            System.out.println(SEPARATOR);
            if (command.trim().equals("bye")) {
                return;
            }
        }
    }

    /**
     * Resolves the data-file path from the optional system property.
     *
     * @return the configured data-file path.
     */
    private static Path resolveDataFile() {
        String configuredPath = System.getProperty(DATA_FILE_PROPERTY);
        return configuredPath == null || configuredPath.isBlank()
                ? DEFAULT_DATA_FILE
                : Path.of(configuredPath);
    }

    /**
     * Joins response lines using the platform's line separator.
     *
     * @param lines the lines that make up the response.
     * @return the response with each line separated appropriately.
     */
    private static String joinResponseLines(String... lines) {
        return String.join(System.lineSeparator(), lines);
    }

    /**
     * Adds a to-do task and reports the updated number of stored tasks.
     *
     * @param command the complete to-do command.
     * @return the response for the added task.
     * @throws SevenSixException if the to-do description is empty.
     */
    private String addTodo(String command) throws SevenSixException {
        String description = command.substring(COMMAND_TODO.length()).trim();
        if (description.isBlank()) {
            throw new SevenSixException("a todo needs a description. Give it a little something to do!");
        }
        return addTask(new Todo(description));
    }

    /**
     * Adds a deadline task when its description and due time are separated by {@code /by}.
     *
     * @param command the complete deadline command.
     * @return the response for the added task.
     * @throws SevenSixException if the deadline format or its details are invalid.
     */
    private String addDeadline(String command) throws SevenSixException {
        String details = command.substring(COMMAND_DEADLINE.length()).trim();
        int byMarkerIndex = details.indexOf(" /by ");
        if (byMarkerIndex == -1) {
            throw new SevenSixException("deadline format is: deadline <description> /by <deadline>.");
        }

        String description = details.substring(0, byMarkerIndex).trim();
        String by = details.substring(byMarkerIndex + " /by ".length()).trim();
        if (description.isBlank() || by.isBlank()) {
            throw new SevenSixException("a deadline needs both a description and a due time.");
        }
        DateTimeParser.ParsedDateTime parsedBy = DateTimeParser.parse(by);
        return addTask(new Deadline(description, parsedBy.getDate(), parsedBy.getTime()));
    }

    /**
     * Adds an event task when its description, start, and end are separated by {@code /from} and
     * {@code /to}.
     *
     * @param command the complete event command.
     * @return the response for the added task.
     * @throws SevenSixException if the event format or its details are invalid.
     */
    private String addEvent(String command) throws SevenSixException {
        String details = command.substring(COMMAND_EVENT.length()).trim();
        int fromMarkerIndex = details.indexOf(" /from ");
        int toMarkerIndex = details.indexOf(" /to ", fromMarkerIndex + " /from ".length());
        if (fromMarkerIndex == -1 || toMarkerIndex == -1) {
            throw new SevenSixException("event format is: event <description> /from <start> /to <end>.");
        }

        String description = details.substring(0, fromMarkerIndex).trim();
        String from = details.substring(fromMarkerIndex + " /from ".length(), toMarkerIndex).trim();
        String to = details.substring(toMarkerIndex + " /to ".length()).trim();
        if (description.isBlank() || from.isBlank() || to.isBlank()) {
            throw new SevenSixException("an event needs a description, a start, and an end.");
        }
        DateTimeParser.ParsedDateTime parsedFrom = DateTimeParser.parse(from);
        DateTimeParser.ParsedDateTime parsedTo = DateTimeParser.parse(to);
        return addTask(new Event(description, parsedFrom.getDate(), parsedFrom.getTime(),
                parsedTo.getDate(), parsedTo.getTime()));
    }

    /**
     * Stores a task and reports the updated number of stored tasks.
     *
     * @param task the task to store.
     * @return the response for the added task.
     */
    private String addTask(Task task) {
        tasks.add(task);
        saveTasks();
        int numberOfTasks = tasks.size();
        return joinResponseLines(
                "Got it. I've added this task:",
                "  " + task,
                "Now you have " + numberOfTasks + " "
                        + getTaskCountDescription(numberOfTasks) + " in the list.");
    }

    /**
     * Returns a grammatically correct description of a number of tasks.
     *
     * @param numberOfTasks the number of stored tasks.
     * @return {@code task} for one task, or {@code tasks} otherwise.
     */
    private String getTaskCountDescription(int numberOfTasks) {
        return numberOfTasks == 1 ? "task" : "tasks";
    }

    /**
     * Returns every stored task with a one-based number.
     *
     * @return the formatted task list.
     */
    private String printTasks() {
        if (tasks.size() == 0) {
            return "There are no tasks in your list.";
        }

        StringBuilder response = new StringBuilder();
        for (int i = 0; i < tasks.size(); i++) {
            if (i > 0) {
                response.append(System.lineSeparator());
            }
            response.append(i + 1).append('.').append(tasks.get(i));
        }
        return response.toString();
    }

    /**
     * Marks a task as done and reports the task to the user.
     *
     * @param command the complete mark command.
     * @return the response for the marked task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String markTask(String command) throws SevenSixException {
        int taskNumber = parseTaskNumber(command, COMMAND_MARK);
        Task task = getTask(taskNumber);
        task.markAsDone();
        saveTasks();
        return joinResponseLines(
                "Nice! I've marked this task as done:", "  " + task);
    }

    /**
     * Marks a task as not done and reports the task to the user.
     *
     * @param command the complete unmark command.
     * @return the response for the unmarked task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String unmarkTask(String command) throws SevenSixException {
        int taskNumber = parseTaskNumber(command, COMMAND_UNMARK);
        Task task = getTask(taskNumber);
        task.markAsNotDone();
        saveTasks();
        return joinResponseLines(
                "OK, I've marked this task as not done yet:", "  " + task);
    }

    /**
     * Deletes a task by its one-based number and reports the updated task count.
     *
     * @param command the complete delete command.
     * @return the response for the removed task.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private String deleteTask(String command) throws SevenSixException {
        int taskNumber = parseTaskNumber(command, COMMAND_DELETE);
        Task removedTask = getTask(taskNumber);
        tasks.remove(taskNumber - 1);
        saveTasks();
        return joinResponseLines(
                "Noted. I've removed this task:",
                "  " + removedTask,
                "Now you have " + tasks.size() + " "
                        + getTaskCountDescription(tasks.size()) + " in the list.");
    }

    /**
     * Finds tasks whose descriptions contain the requested keyword.
     *
     * @param command the complete find command.
     * @return the matching tasks or a no-matches message.
     * @throws SevenSixException if the search keyword is empty.
     */
    private String findTasks(String command) throws SevenSixException {
        String keyword = command.substring(COMMAND_FIND.length()).trim();
        if (keyword.isBlank()) {
            throw new SevenSixException("a find command needs a keyword to search for.");
        }

        List<Task> matchingTasks = tasks.find(keyword);
        if (matchingTasks.isEmpty()) {
            return "There are no matching tasks in your list.";
        }

        StringBuilder response = new StringBuilder("Here are the matching tasks in your list:");
        for (int i = 0; i < matchingTasks.size(); i++) {
            response.append(System.lineSeparator())
                    .append(i + 1).append('.').append(matchingTasks.get(i));
        }
        return response.toString();
    }

    /**
     * Parses a one-based task number from a command.
     *
     * @param command the command containing a task number.
     * @param commandKeyword the keyword at the start of the command.
     * @return the parsed task number.
     * @throws SevenSixException if the task number is not an integer.
     */
    private int parseTaskNumber(String command, String commandKeyword) throws SevenSixException {
        try {
            return Integer.parseInt(command.substring(commandKeyword.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
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
            throw new SevenSixException("that task number is not in your list.");
        }
        return tasks.get(taskNumber - 1);
    }

    /**
     * Saves the task list after a command changes it.
     */
    private void saveTasks() {
        if (!storage.save(tasks)) {
            System.err.println("SevenSix could not save the task list to disk.");
        }
    }
}
