import java.util.Scanner;

/**
 * Starts the SevenSix application and responds to commands entered by the user.
 */
public class SevenSix {
    private static final int MAX_TASKS = 100;
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String MARK_COMMAND = "mark";
    private static final String UNMARK_COMMAND = "unmark";

    /**
     * Welcomes the user, stores entered tasks, lists them on request, and ends when the user enters
     * {@code bye}.
     *
     * @param args command-line arguments, which are not used by this application
     */
    public static void main(String[] args) {
        Task[] tasks = new Task[MAX_TASKS];
        int numberOfTasks = 0;

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
                    numberOfTasks = addTodo(command, tasks, numberOfTasks);
                } else if (command.equals(DEADLINE_COMMAND)
                        || command.startsWith(DEADLINE_COMMAND + " ")) {
                    numberOfTasks = addDeadline(command, tasks, numberOfTasks);
                } else if (command.equals(EVENT_COMMAND) || command.startsWith(EVENT_COMMAND + " ")) {
                    numberOfTasks = addEvent(command, tasks, numberOfTasks);
                } else if (command.equals("list")) {
                    printTasks(tasks, numberOfTasks);
                } else if (command.equals(MARK_COMMAND) || command.startsWith(MARK_COMMAND + " ")) {
                    markTask(command, tasks, numberOfTasks);
                } else if (command.equals(UNMARK_COMMAND)
                        || command.startsWith(UNMARK_COMMAND + " ")) {
                    unmarkTask(command, tasks, numberOfTasks);
                } else {
                    throw new SevenSixException(
                            "I don't know that command yet. Try todo, deadline, event, list, mark, or unmark.");
                }
            } catch (SevenSixException exception) {
                printError(exception.getMessage());
            }

            System.out.println(SEPARATOR);
        }
    }

    /**
     * Adds a to-do task and reports the updated number of stored tasks.
     *
     * @param command the complete to-do command, such as {@code todo borrow book}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @return the number of stored tasks after the to-do has been added
     * @throws SevenSixException if the to-do description is empty
     */
    private static int addTodo(String command, Task[] tasks, int numberOfTasks) throws SevenSixException {
        String description = command.substring(TODO_COMMAND.length()).trim();
        if (description.isBlank()) {
            throw new SevenSixException("a todo needs a description. Give it a little something to do!");
        }
        return addTask(new Todo(description), tasks, numberOfTasks);
    }

    /**
     * Adds a deadline task when its description and due time are separated by {@code /by}.
     *
     * @param command the complete deadline command, such as {@code deadline return book /by Sunday}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @return the number of stored tasks after handling the command
     * @throws SevenSixException if the deadline format or its details are invalid
     */
    private static int addDeadline(String command, Task[] tasks, int numberOfTasks) throws SevenSixException {
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
        return addTask(new Deadline(description, by), tasks, numberOfTasks);
    }

    /**
     * Adds an event task when its description, start, and end are separated by {@code /from} and
     * {@code /to}.
     *
     * @param command the complete event command, such as {@code event team meeting /from Mon 2pm /to 4pm}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @return the number of stored tasks after handling the command
     * @throws SevenSixException if the event format or its details are invalid
     */
    private static int addEvent(String command, Task[] tasks, int numberOfTasks) throws SevenSixException {
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
        return addTask(new Event(description, from, to), tasks, numberOfTasks);
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
     * Stores a task and reports the updated number of stored tasks.
     *
     * @param task the task to store
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @return the number of stored tasks after the task has been added
     * @throws SevenSixException if the task list is full
     */
    private static int addTask(Task task, Task[] tasks, int numberOfTasks) throws SevenSixException {
        if (numberOfTasks >= MAX_TASKS) {
            throw new SevenSixException("your task list is full. 676767!!! Try completing or removing a task first.");
        }

        tasks[numberOfTasks] = task;
        int updatedNumberOfTasks = numberOfTasks + 1;

        System.out.println("Got it. I've added this task:");
        System.out.println("  " + tasks[numberOfTasks]);
        System.out.println("Now you have " + updatedNumberOfTasks + " "
                + getTaskCountDescription(updatedNumberOfTasks) + " in the list.");
        return updatedNumberOfTasks;
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
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     */
    private static void printTasks(Task[] tasks, int numberOfTasks) {
        for (int i = 0; i < numberOfTasks; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
    }

    /**
     * Marks a task as done and reports the task to the user.
     *
     * @param command the complete mark command, such as {@code mark 2}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @throws SevenSixException if the task number is invalid or not in the list
     */
    private static void markTask(String command, Task[] tasks, int numberOfTasks) throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(MARK_COMMAND.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
        if (taskNumber < 1 || taskNumber > numberOfTasks) {
            throw new SevenSixException("that task number is not in your list.");
        }

        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsDone();
        System.out.println("Nice! I've marked this task as done:");
        System.out.println("  " + tasks[taskIndex]);
    }

    /**
     * Marks a task as not done and reports the task to the user.
     *
     * @param command the complete unmark command, such as {@code unmark 2}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     * @throws SevenSixException if the task number is invalid or not in the list
     */
    private static void unmarkTask(String command, Task[] tasks, int numberOfTasks) throws SevenSixException {
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(command.substring(UNMARK_COMMAND.length()).trim());
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
        if (taskNumber < 1 || taskNumber > numberOfTasks) {
            throw new SevenSixException("that task number is not in your list.");
        }

        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsNotDone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println("  " + tasks[taskIndex]);
    }
}
