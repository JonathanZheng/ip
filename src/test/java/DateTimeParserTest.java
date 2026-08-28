import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

/**
 * Verifies the supported date and time formats used by deadline and event commands.
 */
class DateTimeParserTest {
    /**
     * A date-only value should preserve its date and have no time component.
     */
    @Test
    void parse_dateOnly_returnsDateWithoutTime() throws SevenSixException {
        DateTimeParser.ParsedDateTime parsed = DateTimeParser.parse("2019-06-06");

        assertEquals(LocalDate.of(2019, 6, 6), parsed.getDate());
        assertNull(parsed.getTime());
    }

    /**
     * The compact day/month/year format should also parse an explicit time.
     */
    @Test
    void parse_dateAndCompactTime_returnsDateAndTime() throws SevenSixException {
        DateTimeParser.ParsedDateTime parsed = DateTimeParser.parse("2/12/2019 1800");

        assertEquals(LocalDate.of(2019, 12, 2), parsed.getDate());
        assertEquals(LocalTime.of(18, 0), parsed.getTime());
    }

    /**
     * Unsupported text should produce the application-specific input exception.
     */
    @Test
    void parse_invalidValue_throwsSevenSixException() {
        SevenSixException exception = assertThrows(SevenSixException.class,
                () -> DateTimeParser.parse("not-a-date"));

        assertEquals("use yyyy-MM-dd, yyyy-MM-dd HHmm, or d/M/yyyy HHmm for dates and times.",
                exception.getMessage());
    }

    /**
     * Display and storage formatting should use different, predictable representations.
     */
    @Test
    void format_dateTime_returnsDisplayAndStorageFormats() {
        LocalDate date = LocalDate.of(2019, 12, 2);
        LocalTime time = LocalTime.of(18, 0);

        assertEquals("Dec 02 2019 6:00 PM", DateTimeParser.formatForDisplay(date, time));
        assertEquals("2019-12-02T18:00", DateTimeParser.formatForStorage(date, time));
    }
}
