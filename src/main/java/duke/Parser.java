package duke;

/**
 * Recognizes command keywords and turns command text into the values the chatbot acts on.
 *
 * <p>Every rule about the shape of a command lives here, so the chatbot itself only has to
 * decide what to do once a command has been understood. The class is a collection of static
 * helpers because it holds no state of its own.
 */
public class Parser {
    /** Command keyword for ending the conversation. */
    public static final String COMMAND_BYE = "bye";
    /** Command keyword for listing every stored task. */
    public static final String COMMAND_LIST = "list";
    /** Command keyword for creating a to-do task. */
    public static final String COMMAND_TODO = "todo";
    /** Command keyword for creating a deadline task. */
    public static final String COMMAND_DEADLINE = "deadline";
    /** Command keyword for creating an event task. */
    public static final String COMMAND_EVENT = "event";
    /** Command keyword for marking a task as done. */
    public static final String COMMAND_MARK = "mark";
    /** Command keyword for marking a task as not done. */
    public static final String COMMAND_UNMARK = "unmark";
    /** Command keyword for deleting a task. */
    public static final String COMMAND_DELETE = "delete";
    /** Command keyword for searching task descriptions. */
    public static final String COMMAND_FIND = "find";
    /** Command keyword for undoing the most recent task-changing command. */
    public static final String COMMAND_UNDO = "undo";

    /** Marker separating a deadline's description from its due time. */
    private static final String DEADLINE_BY_MARKER = " /by ";
    /** Marker separating an event's description from its start. */
    private static final String EVENT_FROM_MARKER = " /from ";
    /** Marker separating an event's start from its end. */
    private static final String EVENT_TO_MARKER = " /to ";

    /** Prevents instantiation of this helper class. */
    private Parser() {
    }

    /**
     * Checks whether a command is exactly a keyword or starts with that keyword and a space.
     *
     * @param command the normalized command to inspect.
     * @param commandKeyword the command keyword to match.
     * @return {@code true} when the command uses the supplied keyword.
     */
    public static boolean isCommand(String command, String commandKeyword) {
        return command.equals(commandKeyword) || command.startsWith(commandKeyword + " ");
    }

    /**
     * Checks whether a command is exactly a keyword, with no details after it.
     *
     * @param command the normalized command to inspect.
     * @param commandKeyword the command keyword to match.
     * @return {@code true} when the command is the keyword on its own.
     */
    public static boolean isExactCommand(String command, String commandKeyword) {
        return command.equals(commandKeyword);
    }

    /**
     * Parses the description of a to-do command.
     *
     * @param command the complete to-do command.
     * @return the to-do description.
     * @throws SevenSixException if the to-do description is empty.
     */
    public static String parseTodoDescription(String command) throws SevenSixException {
        assert isCommand(command, COMMAND_TODO) : "getResponse routes only todo commands here, so the cut is safe";
        String description = removeKeyword(command, COMMAND_TODO);
        if (description.isBlank()) {
            throw new SevenSixException("a todo needs a description. Give it a little something to do!");
        }
        return description;
    }

    /**
     * Parses a deadline command into a deadline task.
     *
     * @param command the complete deadline command.
     * @return the parsed deadline task.
     * @throws SevenSixException if the deadline format or its details are invalid.
     */
    public static Deadline parseDeadline(String command) throws SevenSixException {
        assert isCommand(command, COMMAND_DEADLINE)
                : "getResponse routes only deadline commands here, so the cut is safe";
        String details = removeKeyword(command, COMMAND_DEADLINE);
        int byMarkerIndex = details.indexOf(DEADLINE_BY_MARKER);
        if (byMarkerIndex == -1) {
            throw new SevenSixException("deadline format is: deadline <description> /by <deadline>.");
        }

        String description = details.substring(0, byMarkerIndex).trim();
        String by = details.substring(byMarkerIndex + DEADLINE_BY_MARKER.length()).trim();
        if (description.isBlank() || by.isBlank()) {
            throw new SevenSixException("a deadline needs both a description and a due time.");
        }
        DateTimeParser.ParsedDateTime parsedBy = DateTimeParser.parse(by);
        return new Deadline(description, parsedBy.getDate(), parsedBy.getTime());
    }

    /**
     * Parses an event command into an event task.
     *
     * @param command the complete event command.
     * @return the parsed event task.
     * @throws SevenSixException if the event format or its details are invalid.
     */
    public static Event parseEvent(String command) throws SevenSixException {
        assert isCommand(command, COMMAND_EVENT) : "getResponse routes only event commands here, so the cut is safe";
        String details = removeKeyword(command, COMMAND_EVENT);
        int fromMarkerIndex = details.indexOf(EVENT_FROM_MARKER);
        int toMarkerIndex = details.indexOf(EVENT_TO_MARKER, fromMarkerIndex + EVENT_FROM_MARKER.length());
        if (fromMarkerIndex == -1 || toMarkerIndex == -1) {
            throw new SevenSixException("event format is: event <description> /from <start> /to <end>.");
        }

        assert toMarkerIndex > fromMarkerIndex : "The /to marker is searched for only after the /from marker";
        String description = details.substring(0, fromMarkerIndex).trim();
        String from = details.substring(fromMarkerIndex + EVENT_FROM_MARKER.length(), toMarkerIndex).trim();
        String to = details.substring(toMarkerIndex + EVENT_TO_MARKER.length()).trim();
        if (description.isBlank() || from.isBlank() || to.isBlank()) {
            throw new SevenSixException("an event needs a description, a start, and an end.");
        }
        DateTimeParser.ParsedDateTime parsedFrom = DateTimeParser.parse(from);
        DateTimeParser.ParsedDateTime parsedTo = DateTimeParser.parse(to);
        return new Event(description, parsedFrom.getDate(), parsedFrom.getTime(),
                parsedTo.getDate(), parsedTo.getTime());
    }

    /**
     * Parses the search keyword of a find command.
     *
     * @param command the complete find command.
     * @return the keyword to search for.
     * @throws SevenSixException if the search keyword is empty.
     */
    public static String parseFindKeyword(String command) throws SevenSixException {
        assert isCommand(command, COMMAND_FIND) : "getResponse routes only find commands here, so the cut is safe";
        String keyword = removeKeyword(command, COMMAND_FIND);
        if (keyword.isBlank()) {
            throw new SevenSixException("a find command needs a keyword to search for.");
        }
        return keyword;
    }

    /**
     * Parses a one-based task number from a command.
     *
     * @param command the command containing a task number.
     * @param commandKeyword the keyword at the start of the command.
     * @return the parsed task number.
     * @throws SevenSixException if the task number is not an integer.
     */
    public static int parseTaskNumber(String command, String commandKeyword) throws SevenSixException {
        assert isCommand(command, commandKeyword) : "Each caller passes the keyword that its command starts with";
        try {
            return Integer.parseInt(removeKeyword(command, commandKeyword));
        } catch (NumberFormatException exception) {
            throw new SevenSixException("please specify a valid task number.");
        }
    }

    /**
     * Removes a command's keyword, leaving the command details.
     *
     * @param command the complete command.
     * @param commandKeyword the keyword at the start of the command.
     * @return the command details without surrounding spaces.
     */
    private static String removeKeyword(String command, String commandKeyword) {
        return command.substring(commandKeyword.length()).trim();
    }
}
