package duke;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Parses the date and time text used by deadline and event commands.
 */
public final class DateTimeParser {
    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter STORAGE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            strictFormatter("uuuu-MM-dd HHmm"),
            strictFormatter("uuuu-MM-dd HH:mm"),
            strictFormatter("d/M/uuuu HHmm"),
            strictFormatter("d/M/uuuu HH:mm"));
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            strictFormatter("d/M/uuuu"));

    /**
     * Prevents construction of this utility class.
     */
    private DateTimeParser() {
    }

    /**
     * Parses a date or date-time value supplied in a command.
     *
     * @param text the date or date-time text
     * @return the parsed date and optional time
     * @throws SevenSixException if the text does not use a supported format
     */
    public static ParsedDateTime parse(String text) throws SevenSixException {
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
                return new ParsedDateTime(dateTime.toLocalDate(), dateTime.toLocalTime());
            } catch (DateTimeParseException exception) {
                // Try the next supported format.
            }
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return new ParsedDateTime(LocalDate.parse(text, formatter), null);
            } catch (DateTimeParseException exception) {
                // Try the next supported format.
            }
        }

        throw new SevenSixException(
                "use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.");
    }

    /**
     * Formats a typed date and optional time for the task display.
     *
     * @param date the date to format
     * @param time the optional time to format
     * @return a readable date, with a time when one was supplied
     */
    public static String formatForDisplay(LocalDate date, LocalTime time) {
        String formattedDate = date.format(DISPLAY_DATE_FORMATTER);
        if (time == null) {
            return formattedDate;
        }
        return formattedDate + " " + time.format(DISPLAY_TIME_FORMATTER);
    }

    /**
     * Formats a typed date and optional time for the storage file.
     *
     * @param date the date to format
     * @param time the optional time to format
     * @return an unambiguous machine-readable date or date-time
     */
    public static String formatForStorage(LocalDate date, LocalTime time) {
        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        if (time == null) {
            return formattedDate;
        }
        return formattedDate + "T" + time.format(STORAGE_TIME_FORMATTER);
    }

    /**
     * Creates a strict formatter for patterns containing a proleptic year.
     *
     * @param pattern the date or date-time pattern
     * @return a strict formatter using English symbols
     */
    private static DateTimeFormatter strictFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    /**
     * Holds a parsed date and an optional time without converting a date-only value into midnight.
     */
    public static final class ParsedDateTime {
        private final LocalDate date;
        private final LocalTime time;

        /**
         * Creates a parsed date and optional time.
         *
         * @param date the parsed date
         * @param time the parsed time, or {@code null} for a date-only value
         */
        private ParsedDateTime(LocalDate date, LocalTime time) {
            this.date = date;
            this.time = time;
        }

        /**
         * Returns the parsed date.
         *
         * @return the parsed date
         */
        public LocalDate getDate() {
            return date;
        }

        /**
         * Returns the parsed time, when one was supplied.
         *
         * @return the parsed time, or {@code null} for a date-only value
         */
        public LocalTime getTime() {
            return time;
        }
    }
}
