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
    /** Defines the application name used by the graphical interface. */
    public static final String APPLICATION_NAME = "SevenSix";
    /** Defines the short product description shown beside the application name. */
    public static final String APPLICATION_SUBTITLE = "PRODUCTIVITY PARTNER";
    /** Defines the small label above the graphical interface introduction. */
    public static final String HEADER_EYEBROW = "COMMAND CENTER";
    /** Defines the graphical interface introduction title. */
    public static final String HEADER_TITLE = "Keep every deliverable moving.";
    /** Defines the graphical interface introduction subtitle. */
    public static final String HEADER_SUBTITLE = "Capture, review, and close out your work in one focused space.";
    /** Defines the readiness label shown in the graphical interface header. */
    public static final String READY_STATUS = "● READY";
    /** Defines the label shown above the graphical command field. */
    public static final String COMPOSER_LABEL = "NEW COMMAND";
    /** Defines the placeholder shown in the graphical command field. */
    public static final String INPUT_PROMPT = "Try: todo review proposal";
    /** Defines the hint shown below the graphical command field. */
    public static final String COMPOSER_HINT = "Press Enter to send  ·  Try list, find, or undo anytime";
    /** Defines the label used by the graphical command button. */
    public static final String SEND_BUTTON_LABEL = "Send command  →";

    /** Separates the chatbot's greeting, responses, and prompts. */
    private static final String SEPARATOR = "____________________________________________________________";
    /** Prefix used to make input errors recognizable in the user interface. */
    private static final String ERROR_PREFIX = "Flagging a blocker: ";

    /** Prevents instantiation of this helper class. */
    private Ui() {
    }

    /**
     * Returns the greeting shown when the chatbot starts.
     *
     * <p>The console and the graphical interface both read the greeting from here so that
     * the two interfaces always introduce the chatbot in the same words.
     *
     * @return the greeting, as one line per sentence.
     */
    public static String getGreeting() {
        return joinResponseLines(
                "Hello! I'm SevenSix, your productivity thought partner.",
                "Which deliverables are we unlocking today?");
    }

    /** Prints the greeting shown when the console application starts. */
    public static void printGreeting() {
        System.out.println(SEPARATOR);
        System.out.println(getGreeting());
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
     * Checks whether a chatbot response reports an input error, so that the graphical
     * interface can highlight it.
     *
     * @param response the response returned by the chatbot.
     * @return {@code true} when the response was produced by {@link #formatError(String)}.
     */
    public static boolean isErrorResponse(String response) {
        return response.startsWith(ERROR_PREFIX);
    }

    /**
     * Returns the message shown when the user says goodbye.
     *
     * @return the farewell message.
     */
    public static String getFarewellMessage() {
        return "Great sync. Let's touch base again soon!";
    }

    /**
     * Returns the message shown when the task list is empty.
     *
     * @return the empty-list message.
     */
    public static String getEmptyListMessage() {
        return "Your pipeline is empty. Nothing to action right now.";
    }

    /**
     * Returns the message shown when a search finds nothing.
     *
     * @return the no-matches message.
     */
    public static String getNoMatchesMessage() {
        return "Nothing in your pipeline matches that search.";
    }

    /**
     * Returns the message shown after a command is undone.
     *
     * @return the undo confirmation message.
     */
    public static String getUndoMessage() {
        return "Rolled back. Your pipeline is restored to its previous state.";
    }

    /**
     * Returns the message shown when the command keyword is not recognized.
     *
     * @return the unknown-command message.
     */
    public static String getUnknownCommandMessage() {
        return "that one is outside my wheelhouse. My core competencies are todo, deadline,"
                + " event, list, mark, unmark, delete, and find.";
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
                "Circling back on your ask. I've actioned this deliverable:",
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
                "Love to see it. This deliverable has shipped:", "  " + task);
    }

    /**
     * Formats the response shown after a task is marked as not done.
     *
     * @param task the task that was marked as not done.
     * @return the response for the unmarked task.
     */
    public static String formatUnmarkedTask(Task task) {
        return joinResponseLines(
                "Understood. I've moved this deliverable back into the pipeline:", "  " + task);
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
                "Noted. I've descoped this deliverable:",
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
                "Here is what surfaced in your pipeline:",
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
        return "Your pipeline now holds " + numberOfTasks + " "
                + getTaskCountDescription(numberOfTasks) + ".";
    }

    /**
     * Returns a grammatically correct description of a number of tasks.
     *
     * @param numberOfTasks the number of stored tasks.
     * @return {@code task} for one task, or {@code tasks} otherwise.
     */
    private static String getTaskCountDescription(int numberOfTasks) {
        return numberOfTasks == 1 ? "deliverable" : "deliverables";
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
