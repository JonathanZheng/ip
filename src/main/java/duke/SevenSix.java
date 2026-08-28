package duke;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Starts the SevenSix application and responds to commands entered by the user.
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
    /** Default relative path for persisted tasks. */
    private static final Path DEFAULT_DATA_FILE = Path.of("data", "duke.txt");
    /** System property that overrides the default data-file path during automated runs. */
    private static final String DATA_FILE_PROPERTY = "sevensix.data.file";

    /**
     * Creates an application entry point.
     */
    public SevenSix() {
    }

    /**
     * Welcomes the user, stores entered tasks, lists them on request, and ends when the user enters
     * {@code bye}.
     *
     * @param args command-line arguments, which are not used by this application.
     */
    public static void main(String[] args) {
        TaskStorage storage = createTaskStorage();
        TaskList tasks = new TaskList(storage.load());

        System.out.println(SEPARATOR);
        System.out.println("Hello! I'm SevenSix.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            System.out.println(SEPARATOR);

            try {
                if (command.equals("bye")) {
                    System.out.println("Bye. Hope to see you again soon!");
                    System.out.println(SEPARATOR);
                    return;
                }

                if (command.equals(COMMAND_TODO) || command.startsWith(COMMAND_TODO + " ")) {
                    addTodo(command, tasks, storage);
                } else if (command.equals(COMMAND_DEADLINE)
                        || command.startsWith(COMMAND_DEADLINE + " ")) {
                    addDeadline(command, tasks, storage);
                } else if (command.equals(COMMAND_EVENT) || command.startsWith(COMMAND_EVENT + " ")) {
                    addEvent(command, tasks, storage);
                } else if (command.equals("list")) {
                    printTasks(tasks);
                } else if (command.equals(COMMAND_MARK) || command.startsWith(COMMAND_MARK + " ")) {
                    markTask(command, tasks, storage);
                } else if (command.equals(COMMAND_UNMARK)
                        || command.startsWith(COMMAND_UNMARK + " ")) {
                    unmarkTask(command, tasks, storage);
                } else if (command.equals(COMMAND_DELETE)
                        || command.startsWith(COMMAND_DELETE + " ")) {
                    deleteTask(command, tasks, storage);
                } else {
                    throw new SevenSixException(
                            "I don't know that command yet. Try todo, deadline, event, list, mark, unmark, or delete.");
                }
            } catch (SevenSixException exception) {
                printError(exception.getMessage());
            }

            System.out.println(SEPARATOR);
        }
    }

    /**
     * Creates the task storage used by this run.
     *
     * <p>The optional system property makes automated tests independent from a user's normal
     * task file. In ordinary use, tasks are stored in {@code ./data/duke.txt}.</p>
     *
     * @return the configured task storage.
     */
    private static TaskStorage createTaskStorage() {
        String configuredPath = System.getProperty(DATA_FILE_PROPERTY);
        Path dataFile = configuredPath == null || configuredPath.isBlank()
                ? DEFAULT_DATA_FILE
                : Path.of(configuredPath);
        return new TaskStorage(dataFile);
    }

    /**
     * Adds a to-do task and reports the updated number of stored tasks.
     *
     * @param command the complete to-do command, such as {@code todo borrow book}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the to-do description is empty.
     */
    private static void addTodo(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
        String description = command.substring(COMMAND_TODO.length()).trim();
        if (description.isBlank()) {
            throw new SevenSixException("a todo needs a description. Give it a little something to do!");
        }
        addTask(new Todo(description), tasks, storage);
    }

    /**
     * Adds a deadline task when its description and due time are separated by {@code /by}.
     *
     * @param command the complete deadline command, such as {@code deadline return book /by Sunday}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the deadline format or its details are invalid.
     */
    private static void addDeadline(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
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
        addTask(new Deadline(description, parsedBy.getDate(), parsedBy.getTime()), tasks, storage);
    }

    /**
     * Adds an event task when its description, start, and end are separated by {@code /from} and
     * {@code /to}.
     *
     * @param command the complete event command, such as {@code event team meeting /from Mon 2pm /to 4pm}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the event format or its details are invalid.
     */
    private static void addEvent(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
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
        addTask(new Event(description, parsedFrom.getDate(), parsedFrom.getTime(),
                parsedTo.getDate(), parsedTo.getTime()), tasks, storage);
    }

    /**
     * Prints a themed message for an input exception.
     *
     * @param message the explanation of the input error.
     */
    private static void printError(String message) {
        System.out.println("676767!!! " + message);
    }

    /**
     * Stores a task in the collection and reports the updated number of stored tasks.
     *
     * @param task the task to store.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     */
    private static void addTask(Task task, TaskList tasks, TaskStorage storage) {
        tasks.add(task);
        saveTasks(tasks, storage);
        int numberOfTasks = tasks.size();

        System.out.println("Got it. I've added this task:");
        System.out.println("  " + task);
        System.out.println("Now you have " + numberOfTasks + " "
                + getTaskCountDescription(numberOfTasks) + " in the list.");
    }

    /**
     * Returns a grammatically correct description of a number of tasks.
     *
     * @param numberOfTasks the number of stored tasks.
     * @return {@code task} for one task, or {@code tasks} otherwise.
     */
    private static String getTaskCountDescription(int numberOfTasks) {
        return numberOfTasks == 1 ? "task" : "tasks";
    }

    /**
     * Prints every stored task with a one-based number.
     *
     * @param tasks the collection that holds the tasks.
     */
    private static void printTasks(TaskList tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Marks a task as done and reports the task to the user.
     *
     * @param command the complete mark command, such as {@code mark 2}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private static void markTask(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(COMMAND_MARK.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SevenSixException("that task number is not in your list.");
        }

        int taskIndex = taskNumber - 1;
        tasks.get(taskIndex).markAsDone();
        saveTasks(tasks, storage);
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks.get(taskIndex));
    }

    /**
     * Marks a task as not done and reports the task to the user.
     *
     * @param command the complete unmark command, such as {@code unmark 2}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private static void unmarkTask(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(COMMAND_UNMARK.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SevenSixException("that task number is not in your list.");
        }

        int taskIndex = taskNumber - 1;
        tasks.get(taskIndex).markAsNotDone();
        saveTasks(tasks, storage);
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + tasks.get(taskIndex));
    }

    /**
     * Deletes a task by its one-based number and reports the updated task count.
     *
     * @param command the complete delete command, such as {@code delete 3}.
     * @param tasks the collection that holds the tasks.
     * @param storage the storage destination.
     * @throws SevenSixException if the task number is invalid or not in the list.
     */
    private static void deleteTask(String command, TaskList tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(COMMAND_DELETE.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new SevenSixException("that task number is not in your list.");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        saveTasks(tasks, storage);
        System.out.println("Noted. I've removed this task:");
        System.out.println("  " + removedTask);
        System.out.println("Now you have " + tasks.size() + " "
                + getTaskCountDescription(tasks.size()) + " in the list.");
    }

    /**
     * Saves the task list after a command changes it.
     *
     * @param tasks the changed task list.
     * @param storage the storage destination.
     */
    private static void saveTasks(TaskList tasks, TaskStorage storage) {
        if (!storage.save(tasks)) {
            System.err.println("SevenSix could not save the task list to disk.");
        }
    }
}
