package duke;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 * Reads and writes SevenSix tasks on the local hard disk.
 *
 * <p>Each line stores one task using a small pipe-separated format. Text values escape pipe and
 * backslash characters so that those characters can also appear in task details.</p>
 */
public class TaskStorage {
    /** The file used to persist the task list. */
    private final Path dataFile;

    /**
     * Creates storage for the supplied data file.
     *
     * @param dataFile the relative or absolute path of the task data file.
     */
    public TaskStorage(Path dataFile) {
        assert dataFile != null : "Storage needs a data-file path, which is resolved before construction";
        this.dataFile = dataFile;
    }

    /**
     * Loads all valid tasks from the data file.
     *
     * <p>A missing file is treated as an empty task list. Invalid lines are ignored so one
     * corrupted record does not prevent the chatbot from starting.</p>
     *
     * @return the tasks read from disk, or an empty list when the file is missing or unreadable.
     */
    public List<Task> load() {
        try {
            createParentDirectory();
            if (!Files.exists(dataFile)) {
                return new ArrayList<>();
            }

            return Files.readAllLines(dataFile, StandardCharsets.UTF_8).stream()
                    .map(this::parseTask)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException exception) {
            return new ArrayList<>();
        }
    }

    /**
     * Saves the current task list, creating its parent directory when necessary.
     *
     * @param tasks the tasks to save.
     * @return {@code true} when the task list was saved successfully, or {@code false} otherwise.
     */
    public boolean save(Iterable<Task> tasks) {
        try {
            createParentDirectory();
            List<String> lines = StreamSupport.stream(tasks.spliterator(), false)
                    .map(this::formatTask)
                    .toList();
            Files.write(dataFile, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            return true;
        } catch (IOException exception) {
            return false;
        }
    }

    /** Reads and parses all task records from the data file.
     *
     * @return the valid tasks read from disk.
     * @throws IOException if the data file cannot be read.
     */
    private List<Task> readTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
            Task task = parseTask(line);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /** Converts all tasks into storage lines.
     *
     * @param tasks the tasks to convert.
     * @return the serialized task lines.
     */
    private List<String> formatTasks(Iterable<Task> tasks) {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(formatTask(task));
        }
        return lines;
    }

    /** Writes storage lines to the data file, replacing its previous contents.
     *
     * @param lines the serialized task lines to write.
     * @throws IOException if the data file cannot be written.
     */
    private void writeTasks(List<String> lines) throws IOException {
        Files.write(dataFile, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    /**
     * Creates the folder containing the data file if the path has a parent folder.
     *
     * @throws IOException if the folder cannot be created.
     */
    private void createParentDirectory() throws IOException {
        Path parent = dataFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }

    /**
     * Converts a task into one line of the storage format.
     *
     * @param task the task to format.
     * @return the serialized task.
     */
    private String formatTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        if (task instanceof Deadline deadline) {
            return String.join(" | ", "D", status, escape(deadline.getDescription()),
                    escape(DateTimeParser.formatForStorage(deadline.getBy(), deadline.getByTime())));
        }
        if (task instanceof Event event) {
            return String.join(" | ", "E", status, escape(event.getDescription()),
                    escape(DateTimeParser.formatForStorage(event.getFrom(), event.getFromTime())),
                    escape(DateTimeParser.formatForStorage(event.getTo(), event.getToTime())));
        }
        return String.join(" | ", "T", status, escape(task.getDescription()));
    }

    /**
     * Converts one storage line into a task, returning {@code null} for malformed data.
     *
     * @param line the line read from disk.
     * @return the parsed task, or {@code null} when the line is invalid.
     */
    private Task parseTask(String line) {
        if (line.isBlank()) {
            return null;
        }

        List<String> fields = splitFields(line);
        if (!hasValidStatus(fields)) {
            return null;
        }

        Task task = parseTaskFields(fields);
        if (task == null) {
            return null;
        }
        restoreStatus(task, fields.get(1));
        return task;
    }

    /** Checks whether a storage record has a valid status field.
     *
     * @param fields the decoded storage fields.
     * @return {@code true} when the record contains a valid status.
     */
    private boolean hasValidStatus(List<String> fields) {
        return fields.size() >= 3 && (fields.get(1).equals("0") || fields.get(1).equals("1"));
    }

    /** Parses the task-specific fields in a storage record.
     *
     * @param fields the decoded storage fields.
     * @return the parsed task, or {@code null} when the type is invalid.
     */
    private Task parseTaskFields(List<String> fields) {
        switch (fields.get(0)) {
        case "T":
            return parseTodo(fields);
        case "D":
            return parseDeadline(fields);
        case "E":
            return parseEvent(fields);
        default:
            return null;
        }
    }

    /** Parses a to-do storage record.
     *
     * @param fields the decoded storage fields.
     * @return the parsed to-do, or {@code null} when the fields are invalid.
     */
    private Task parseTodo(List<String> fields) {
        if (fields.size() != 3 || fields.get(2).isBlank()) {
            return null;
        }
        return new Todo(fields.get(2));
    }

    /** Parses a deadline storage record.
     *
     * @param fields the decoded storage fields.
     * @return the parsed deadline, or {@code null} when the fields are invalid.
     */
    private Task parseDeadline(List<String> fields) {
        if (fields.size() != 4 || fields.get(2).isBlank() || fields.get(3).isBlank()) {
            return null;
        }
        try {
            DateTimeParser.ParsedDateTime parsedBy = DateTimeParser.parse(fields.get(3));
            return new Deadline(fields.get(2), parsedBy.getDate(), parsedBy.getTime());
        } catch (SevenSixException exception) {
            return null;
        }
    }

    /** Parses an event storage record.
     *
     * @param fields the decoded storage fields.
     * @return the parsed event, or {@code null} when the fields are invalid.
     */
    private Task parseEvent(List<String> fields) {
        if (fields.size() != 5 || fields.get(2).isBlank() || fields.get(3).isBlank()
                || fields.get(4).isBlank()) {
            return null;
        }
        try {
            DateTimeParser.ParsedDateTime parsedFrom = DateTimeParser.parse(fields.get(3));
            DateTimeParser.ParsedDateTime parsedTo = DateTimeParser.parse(fields.get(4));
            return new Event(fields.get(2), parsedFrom.getDate(), parsedFrom.getTime(),
                    parsedTo.getDate(), parsedTo.getTime());
        } catch (SevenSixException exception) {
            return null;
        }
    }

    /** Restores the completion status stored in a task record.
     *
     * @param task the task whose status is restored.
     * @param status the serialized completion status.
     */
    private void restoreStatus(Task task, String status) {
        if (status.equals("1")) {
            task.markAsDone();
        }
    }

    /**
     * Splits a storage line at unescaped pipe characters and removes field padding.
     *
     * @param line the serialized task line.
     * @return the decoded fields.
     */
    private List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                if (character == '|' || character == '\\') {
                    field.append(character);
                } else {
                    field.append('\\').append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        if (escaped) {
            field.append('\\');
        }
        fields.add(field.toString().trim());
        assert !fields.isEmpty() : "Splitting always yields at least one field, so callers may check field counts";
        return fields;
    }

    /**
     * Escapes characters that have a special meaning in the storage format.
     *
     * @param value the text to escape.
     * @return escaped text.
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("|", "\\|");
    }
}
