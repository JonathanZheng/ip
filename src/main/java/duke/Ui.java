package duke;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Builds the text shown to the user and drives the console conversation.
 *
 * <p>Every user-visible message is produced here, so the console loop and the graphical
 * interface show the same wording. The class is a collection of static helpers because it
 * holds no state of its own.
 */
public class Ui {
    /** Separates the chatbot's greeting, responses, and prompts. */
    private static final String SEPARATOR = "____________________________________________________________";
    /** Prefix used to make input errors recognizable in the user interface. */
    private static final String ERROR_PREFIX = "676767!!! ";

    /** Prevents instantiation of this helper class. */
    private Ui() {
    }

    /** Prints the greeting shown when the console application starts. */
    public static void printGreeting() {
        System.out.println(SEPARATOR);
        System.out.println("Hello! I'm SevenSix.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);
    }

    /**
     * Processes console input until the input ends or the user says goodbye.
     *
     * @param chatbot the command processor used for each input line.
     */
    public static void runConsoleLoop(SevenSix chatbot) {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            printCommandResponse(chatbot, command);
            if (isExitCommand(command)) {
                return;
            }
        }
    }

    /**
     * Prints one command response with the console separators.
     *
     * @param chatbot the command processor used to produce the response.
     * @param command the command entered by the user.
     */
    private static void printCommandResponse(SevenSix chatbot, String command) {
        System.out.println(SEPARATOR);
        System.out.println(chatbot.getResponse(command));
        System.out.println(SEPARATOR);
    }

    /**
     * Checks whether a console command ends the application.
     *
     * @param command the command entered by the user.
     * @return {@code true} when the command is {@code bye}.
     */
    private static boolean isExitCommand(String command) {
        return Parser.isExactCommand(command.trim(), Parser.COMMAND_BYE);
    }

    /**
     * Marks a message as an input error so that the user can recognize it.
     *
     * @param message the error description.
     * @return the error message with its prefix.
     */
    public static String formatError(String message) {
        return ERROR_PREFIX + message;
    }

    /**
     * Returns the message shown when the user says goodbye.
     *
     * @return the farewell message.
     */
    public static String getFarewellMessage() {
        return "Bye. Hope to see you again soon!";
    }

    /**
     * Returns the message shown when the task list is empty.
     *
     * @return the empty-list message.
     */
    public static String getEmptyListMessage() {
        return "There are no tasks in your list.";
    }

    /**
     * Returns the message shown when a search finds nothing.
     *
     * @return the no-matches message.
     */
    public static String getNoMatchesMessage() {
        return "There are no matching tasks in your list.";
    }

    /**
     * Returns the message shown after a command is undone.
     *
     * @return the undo confirmation message.
     */
    public static String getUndoMessage() {
        return "OK, I've undone the last command.";
    }

    /**
     * Returns the message shown when the command keyword is not recognized.
     *
     * @return the unknown-command message.
     */
    public static String getUnknownCommandMessage() {
        return "I don't know that command yet. Try todo, deadline, event, list, mark, unmark, delete,"
                + " or find.";
    }

    /**
     * Formats the response shown after a task is added.
     *
     * @param task the task that was added.
     * @param numberOfTasks the number of tasks now in the list.
     * @return the response for the added task.
     */
    public static String formatAddedTask(Task task, int numberOfTasks) {
        return joinResponseLines(
                "Got it. I've added this task:",
                "  " + task,
                formatTaskCount(numberOfTasks));
    }

    /**
     * Formats the response shown after a task is marked as done.
     *
     * @param task the task that was marked as done.
     * @return the response for the marked task.
     */
    public static String formatMarkedTask(Task task) {
        return joinResponseLines(
                "Nice! I've marked this task as done:", "  " + task);
    }

    /**
     * Formats the response shown after a task is marked as not done.
     *
     * @param task the task that was marked as not done.
     * @return the response for the unmarked task.
     */
    public static String formatUnmarkedTask(Task task) {
        return joinResponseLines(
                "OK, I've marked this task as not done yet:", "  " + task);
    }

    /**
     * Formats the response shown after a task is deleted.
     *
     * @param removedTask the task that was deleted.
     * @param numberOfTasks the number of tasks left in the list.
     * @return the response for the deleted task.
     */
    public static String formatDeletedTask(Task removedTask, int numberOfTasks) {
        return joinResponseLines(
                "Noted. I've removed this task:",
                "  " + removedTask,
                formatTaskCount(numberOfTasks));
    }

    /**
     * Formats every task with its one-based list number.
     *
     * @param taskCollection the tasks to format.
     * @return the numbered task list.
     */
    public static String formatTaskList(Iterable<Task> taskCollection) {
        List<Task> taskSnapshot = StreamSupport.stream(taskCollection.spliterator(), false)
                .collect(Collectors.toList());
        return IntStream.range(0, taskSnapshot.size())
                .mapToObj(index -> (index + 1) + "." + taskSnapshot.get(index))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Formats the tasks that matched a search, under an introductory line.
     *
     * @param matchingTasks the tasks that matched the search keyword.
     * @return the introduction followed by the numbered matching tasks.
     */
    public static String formatMatchingTasks(List<Task> matchingTasks) {
        return joinResponseLines(
                "Here are the matching tasks in your list:",
                formatTaskList(matchingTasks));
    }

    /**
     * Formats the sentence reporting how many tasks the list now holds.
     *
     * @param numberOfTasks the number of stored tasks.
     * @return the task-count sentence.
     */
    private static String formatTaskCount(int numberOfTasks) {
        assert numberOfTasks >= 0 : "A task count is reported only after the list size is read";
        return "Now you have " + numberOfTasks + " "
                + getTaskCountDescription(numberOfTasks) + " in the list.";
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
     * Joins response lines using the platform's line separator.
     *
     * @param lines the lines that make up the response.
     * @return the response with each line separated appropriately.
     */
    private static String joinResponseLines(String... lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
