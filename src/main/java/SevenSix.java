import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

/**
 * Starts the SevenSix application and responds to commands entered by the user.
 */
public class SevenSix {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";
    private static final String DELETE_COMMAND = "delete";
    private static final Path DEFAULT_DATA_FILE = Path.of("data", "duke.txt");
    private static final String DATA_FILE_PROPERTY = "sevensix.data.file";

    /**
     * Welcomes the user, stores entered tasks, lists them on request, and ends when the user enters
     * {@code bye}.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        TaskStorage storage = createTaskStorage();
        List<Task> tasks = storage.load();

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

                if (command.equals(TODO_COMMAND) || command.startsWith(TODO_COMMAND + " ")) {
                    addTodo(command, tasks, storage);
                } else if (command.equals(DEADLINE_COMMAND)
                        || command.startsWith(DEADLINE_COMMAND + " ")) {
                    addDeadline(command, tasks, storage);
                } else if (command.equals(EVENT_COMMAND) || command.startsWith(EVENT_COMMAND + " ")) {
                    addEvent(command, tasks, storage);
                } else if (command.equals("list")) {
                    printTasks(tasks);
                } else if (command.equals(MARK_COMMAND) || command.startsWith(MARK_COMMAND + " ")) {
                    markTask(command, tasks, storage);
                } else if (command.equals(UNMARK_COMMAND)
                        || command.startsWith(UNMARK_COMMAND + " ")) {
                    unmarkTask(command, tasks, storage);
                } else if (command.equals(DELETE_COMMAND)
                        || command.startsWith(DELETE_COMMAND + " ")) {
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
     * @return the configured task storage
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
     * @param command the complete to-do command, such as {@code todo borrow book}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the to-do description is empty
     */
    private static void addTodo(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        String description = command.substring(TODO_COMMAND.length()).trim();
        if (description.isBlank()) {
            throw new SevenSixException("a todo needs a description. Give it a little something to do!");
        }
        addTask(new Todo(description), tasks, storage);
    }

    /**
     * Adds a deadline task when its description and due time are separated by {@code /by}.
     *
     * @param command the complete deadline command, such as {@code deadline return book /by Sunday}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the deadline format or its details are invalid
     */
    private static void addDeadline(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        String details = command.substring(DEADLINE_COMMAND.length()).trim();
        int byMarkerIndex = details.indexOf(" /by ");
        if (byMarkerIndex == -1) {
            throw new SevenSixException("deadline format is: deadline <description> /by <deadline>.");
        }

        String description = details.substring(0, byMarkerIndex).trim();
        String by = details.substring(byMarkerIndex + " /by ".length()).trim();
        if (description.isBlank() || by.isBlank()) {
            throw new SevenSixException("a deadline needs both a description and a due time.");
        }
        addTask(new Deadline(description, by), tasks, storage);
    }

    /**
     * Adds an event task when its description, start, and end are separated by {@code /from} and
     * {@code /to}.
     *
     * @param command the complete event command, such as {@code event team meeting /from Mon 2pm /to 4pm}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the event format or its details are invalid
     */
    private static void addEvent(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        String details = command.substring(EVENT_COMMAND.length()).trim();
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
        addTask(new Event(description, from, to), tasks, storage);
    }

    /**
     * Prints a themed message for an input exception.
     *
     * @param message the explanation of the input error
     */
    private static void printError(String message) {
        System.out.println("676767!!! " + message);
    }

    /**
     * Stores a task in the collection and reports the updated number of stored tasks.
     *
     * @param task the task to store
     * @param tasks the collection that holds the tasks
     */
    private static void addTask(Task task, List<Task> tasks, TaskStorage storage) {
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
     * @param numberOfTasks the number of stored tasks
     * @return {@code task} for one task, or {@code tasks} otherwise
     */
    private static String getTaskCountDescription(int numberOfTasks) {
        return numberOfTasks == 1 ? "task" : "tasks";
    }

    /**
     * Prints every stored task with a one-based number.
     *
     * @param tasks the collection that holds the tasks
     */
    private static void printTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Marks a task as done and reports the task to the user.
     *
     * @param command the complete mark command, such as {@code mark 2}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the task number is invalid or not in the list
     */
    private static void markTask(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(MARK_COMMAND.length()).trim());
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
     * @param command the complete unmark command, such as {@code unmark 2}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the task number is invalid or not in the list
     */
    private static void unmarkTask(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(UNMARK_COMMAND.length()).trim());
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
     * @param command the complete delete command, such as {@code delete 3}
     * @param tasks the collection that holds the tasks
     * @throws SevenSixException if the task number is invalid or not in the list
     */
    private static void deleteTask(String command, List<Task> tasks, TaskStorage storage)
            throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(DELETE_COMMAND.length()).trim());
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
     * @param tasks the changed task list
     * @param storage the storage destination
     */
    private static void saveTasks(List<Task> tasks, TaskStorage storage) {
        if (!storage.save(tasks)) {
            System.err.println("SevenSix could not save the task list to disk.");
        }
    }
}
