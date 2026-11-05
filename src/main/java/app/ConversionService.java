package app;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The application logic that used to live inside the JavaFX Main class:
 * parsing the input, converting, building the result text, saving the record
 * and a few display helpers. It does not use JavaFX, so it can be unit tested
 * without opening a window.
 */
public class ConversionService {

    public static final String MSG_NOT_A_NUMBER = "Temperature must be a number.";
    public static final String MSG_SELECT_UNITS = "Please select both units.";
    public static final String EXTREME_SUFFIX = "  (extreme temperature!)";

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Something that can store a record (normally TempRecordDAO::save). */
    @FunctionalInterface
    public interface RecordSaver {
        void save(TempRecord record) throws SQLException;
    }

    /** The outcome of one conversion: the numbers and the text shown to the user. */
    public record ConversionResult(double value, double result, String displayText) { }

    private final TemperatureConverter converter;
    private final RecordSaver saver;

    public ConversionService() {
        this(new TemperatureConverter(), new TempRecordDAO()::save);
    }

    public ConversionService(TemperatureConverter converter, RecordSaver saver) {
        this.converter = converter;
        this.saver = saver;
    }

    /** Parses user input. Accepts a decimal comma and surrounding spaces. */
    public static double parseValue(String text) {
        if (text == null) {
            throw new NumberFormatException(MSG_NOT_A_NUMBER);
        }
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new NumberFormatException(MSG_NOT_A_NUMBER);
        }
    }

    /**
     * Parses the input and converts it between the two units.
     *
     * @throws NumberFormatException    if the input is not a number
     * @throws IllegalArgumentException if a unit is missing or unknown, or the
     *                                  temperature is below absolute zero
     */
    public ConversionResult convert(String input, TemperatureUnit from, TemperatureUnit to) {
        double value = parseValue(input);

        if (from == null || to == null) {
            throw new IllegalArgumentException(MSG_SELECT_UNITS);
        }

        double result = converter.convert(value, from.getKeyName(), to.getKeyName());
        double celsius = converter.toCelsius(value, from.getKeyName());

        String text = String.format(Locale.ROOT, "%.2f %s = %.2f %s",
                value, from.getUnitName(), result, to.getUnitName());
        if (converter.isExtremeTemperature(celsius)) {
            text += EXTREME_SUFFIX;
        }
        return new ConversionResult(value, result, text);
    }

    /** Saves a finished conversion through the configured saver. */
    public void save(ConversionResult conversion, TemperatureUnit from, TemperatureUnit to,
                     String languageCode) throws SQLException {
        saver.save(new TempRecord(conversion.value(), conversion.result(),
                from.getId(), to.getId(), languageCode));
    }

    /** Maps the language names in the language dialog to a Locale (English by default). */
    public static Locale localeForChoice(String choice) {
        if (choice == null) {
            return Locale.US;
        }
        return switch (choice) {
            case "Finnish" -> Locale.forLanguageTag("fi-FI");
            case "Swedish" -> Locale.forLanguageTag("sv-SE");
            case "Japanese" -> Locale.forLanguageTag("ja-JP");
            case "Arabic" -> Locale.forLanguageTag("ar-SA");
            default -> Locale.US;
        };
    }

    /** Arabic is shown right-to-left. */
    public static boolean isRightToLeft(Locale locale) {
        return locale != null && "ar".equals(locale.getLanguage());
    }

    /** Unit id to display name, used by the records table. */
    public static Map<Integer, String> buildUnitNames(List<TemperatureUnit> units) {
        Map<Integer, String> names = new HashMap<>();
        for (TemperatureUnit unit : units) {
            names.put(unit.getId(), unit.getUnitName());
        }
        return names;
    }

    /** Name for a unit id, or "?" when the id is not known. */
    public static String unitNameFor(Map<Integer, String> unitNames, int unitId) {
        return unitNames.getOrDefault(unitId, "?");
    }

    /** Index of the unit preselected in the "to" box: the second unit if there is one. */
    public static int defaultToIndex(int unitCount) {
        return unitCount > 1 ? 1 : 0;
    }

    /** Timestamp text for the records table; empty if there is no timestamp. */
    public static String formatTimestamp(LocalDateTime time) {
        return time == null ? "" : time.format(TIME_FORMAT);
    }
}
