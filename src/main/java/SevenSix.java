import java.util.Scanner;

/**
 * Starts the SevenSix application and responds to commands entered by the user.
 */
public class SevenSix {
    private static final int MAX_TASKS = 100;
    private static final String SEPARATOR = "____________________________________________________________";

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
            String command = scanner.nextLine();
            System.out.println(SEPARATOR);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(SEPARATOR);
                return;
            }

            if (command.startsWith("todo ")) {
                numberOfTasks = addTodo(command, tasks, numberOfTasks);
            } else if (command.equals("list")) {
                printTasks(tasks, numberOfTasks);
            } else if (command.startsWith("mark ")) {
                markTask(command, tasks, numberOfTasks);
            } else if (command.startsWith("unmark ")) {
                unmarkTask(command, tasks, numberOfTasks);
            } else {
                tasks[numberOfTasks] = new Task(command);
                numberOfTasks++;
                System.out.println("added: " + command);
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
     */
    private static int addTodo(String command, Task[] tasks, int numberOfTasks) {
        String description = command.substring("todo ".length());
        tasks[numberOfTasks] = new Todo(description);
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
     */
    private static void markTask(String command, Task[] tasks, int numberOfTasks) {
        try {
            int taskNumber = Integer.parseInt(command.substring("mark ".length()).trim());
            if (taskNumber < 1 || taskNumber > numberOfTasks) {
                System.out.println("That task number is not in your list.");
                return;
            }

            int taskIndex = taskNumber - 1;
            tasks[taskIndex].markAsDone();
            System.out.println("Nice! I've marked this task as done:");
            System.out.println("  " + tasks[taskIndex]);
        } catch (NumberFormatException exception) {
            System.out.println("Please specify a valid task number.");
        }
    }

    /**
     * Marks a task as not done and reports the task to the user.
     *
     * @param command the complete unmark command, such as {@code unmark 2}
     * @param tasks the array that holds the tasks
     * @param numberOfTasks the number of entries in {@code tasks} that have been used
     */
    private static void unmarkTask(String command, Task[] tasks, int numberOfTasks) {
        try {
            int taskNumber = Integer.parseInt(command.substring("unmark ".length()).trim());
            if (taskNumber < 1 || taskNumber > numberOfTasks) {
                System.out.println("That task number is not in your list.");
                return;
            }

            int taskIndex = taskNumber - 1;
            tasks[taskIndex].markAsNotDone();
            System.out.println("OK, I've marked this task as not done yet:");
            System.out.println("  " + tasks[taskIndex]);
        } catch (NumberFormatException exception) {
            System.out.println("Please specify a valid task number.");
        }
    }
}
