package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Tests the logic extracted from Main. No database and no JavaFX needed. */
public class ConversionServiceTest {

    private final TemperatureUnit celsius = new TemperatureUnit(1, "Celsius", "celsius");
    private final TemperatureUnit fahrenheit = new TemperatureUnit(2, "Fahrenheit", "fahrenheit");
    private final TemperatureUnit kelvin = new TemperatureUnit(3, "Kelvin", "kelvin");
    private final TemperatureUnit speed = new TemperatureUnit(4, "Kilometre per hour", "speed_kmh");

    private List<TempRecord> saved;
    private ConversionService service;

    @BeforeEach
    public void setUp() {
        saved = new ArrayList<>();
        service = new ConversionService(new TemperatureConverter(), saved::add);
    }

    // parseValue

    @Test
    public void testParseValue_plainNumber() {
        assertEquals(25.5, ConversionService.parseValue("25.5"), 1e-9);
    }

    @Test
    public void testParseValue_acceptsDecimalCommaAndSpaces() {
        assertEquals(-3.25, ConversionService.parseValue("  -3,25 "), 1e-9);
    }

    @Test
    public void testParseValue_textThrowsWithUserMessage() {
        NumberFormatException ex = assertThrows(NumberFormatException.class,
                () -> ConversionService.parseValue("abc"));
        assertEquals(ConversionService.MSG_NOT_A_NUMBER, ex.getMessage());
    }

    @Test
    public void testParseValue_emptyThrows() {
        assertThrows(NumberFormatException.class, () -> ConversionService.parseValue("   "));
    }

    @Test
    public void testParseValue_nullThrows() {
        NumberFormatException ex = assertThrows(NumberFormatException.class,
                () -> ConversionService.parseValue(null));
        assertEquals(ConversionService.MSG_NOT_A_NUMBER, ex.getMessage());
    }

    // convert

    @Test
    public void testConvert_celsiusToFahrenheit() {
        ConversionService.ConversionResult r = service.convert("20", celsius, fahrenheit);
        assertEquals(20.0, r.value(), 1e-9);
        assertEquals(68.0, r.result(), 1e-9);
        assertEquals("20.00 Celsius = 68.00 Fahrenheit", r.displayText());
    }

    @Test
    public void testConvert_decimalCommaInput() {
        ConversionService.ConversionResult r = service.convert("0,5", celsius, kelvin);
        assertEquals(273.65, r.result(), 1e-9);
    }

    @Test
    public void testConvert_usesDisplayNamesInText() {
        TemperatureUnit finnish = new TemperatureUnit(1, "Celsius", "celsius");
        TemperatureUnit swedishKelvin = new TemperatureUnit(3, "Kelvin", "kelvin");
        String text = service.convert("0", finnish, swedishKelvin).displayText();
        assertEquals("0.00 Celsius = 273.15 Kelvin", text);
    }

    @Test
    public void testConvert_extremeHotAddsWarning() {
        String text = service.convert("60", celsius, fahrenheit).displayText();
        assertTrue(text.endsWith(ConversionService.EXTREME_SUFFIX));
    }

    @Test
    public void testConvert_extremeColdAddsWarning() {
        String text = service.convert("-50", celsius, kelvin).displayText();
        assertTrue(text.endsWith(ConversionService.EXTREME_SUFFIX));
    }

    @Test
    public void testConvert_normalTemperatureHasNoWarning() {
        String text = service.convert("20", celsius, fahrenheit).displayText();
        assertFalse(text.contains("extreme"));
    }

    @Test
    public void testConvert_extremeIsJudgedInCelsiusNotInputUnit() {
        // 100 F is only about 37.8 C, so it is not extreme
        assertFalse(service.convert("100", fahrenheit, celsius).displayText().contains("extreme"));
        // 400 K is about 126.85 C, so it is extreme
        assertTrue(service.convert("400", kelvin, celsius).displayText().contains("extreme"));
    }

    @Test
    public void testConvert_nonNumericInputThrowsNumberFormat() {
        assertThrows(NumberFormatException.class, () -> service.convert("hello", celsius, kelvin));
    }

    @Test
    public void testConvert_missingFromUnitThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.convert("10", null, kelvin));
        assertEquals(ConversionService.MSG_SELECT_UNITS, ex.getMessage());
    }

    @Test
    public void testConvert_missingToUnitThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.convert("10", celsius, null));
        assertEquals(ConversionService.MSG_SELECT_UNITS, ex.getMessage());
    }

    @Test
    public void testConvert_numberIsCheckedBeforeUnits() {
        // same order as the old Main: bad number wins over missing unit
        assertThrows(NumberFormatException.class, () -> service.convert("x", null, null));
    }

    @Test
    public void testConvert_belowAbsoluteZeroThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.convert("-300", celsius, kelvin));
        assertTrue(ex.getMessage().contains("absolute zero"));
    }

    @Test
    public void testConvert_unsupportedUnitThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.convert("10", celsius, speed));
    }

    @Test
    public void testConvert_doesNotSave() {
        service.convert("10", celsius, kelvin);
        assertTrue(saved.isEmpty());
    }

    // save

    @Test
    public void testSave_passesCorrectRecordToSaver() throws SQLException {
        ConversionService.ConversionResult r = service.convert("100", celsius, fahrenheit);
        service.save(r, celsius, fahrenheit, "fi");

        assertEquals(1, saved.size());
        TempRecord rec = saved.get(0);
        assertEquals(100.0, rec.getInputValue(), 1e-9);
        assertEquals(212.0, rec.getResultValue(), 1e-9);
        assertEquals(1, rec.getFromUnitId());
        assertEquals(2, rec.getToUnitId());
        assertEquals("fi", rec.getLanguageCode());
    }

    @Test
    public void testSave_blankLanguageFallsBackToEnglish() throws SQLException {
        service.save(service.convert("1", celsius, kelvin), celsius, kelvin, "");
        assertEquals("en", saved.get(0).getLanguageCode());
    }

    @Test
    public void testSave_propagatesSqlException() {
        ConversionService failing = new ConversionService(new TemperatureConverter(), r -> {
            throw new SQLException("db down");
        });
        ConversionService.ConversionResult r = failing.convert("1", celsius, kelvin);
        SQLException ex = assertThrows(SQLException.class,
                () -> failing.save(r, celsius, kelvin, "en"));
        assertEquals("db down", ex.getMessage());
    }

    // language choice

    @Test
    public void testLocaleForChoice_allLanguages() {
        assertEquals("fi", ConversionService.localeForChoice("Finnish").getLanguage());
        assertEquals("sv", ConversionService.localeForChoice("Swedish").getLanguage());
        assertEquals("ja", ConversionService.localeForChoice("Japanese").getLanguage());
        assertEquals("ar", ConversionService.localeForChoice("Arabic").getLanguage());
        assertEquals(Locale.US, ConversionService.localeForChoice("English"));
    }

    @Test
    public void testLocaleForChoice_nullAndUnknownDefaultToEnglish() {
        assertEquals(Locale.US, ConversionService.localeForChoice(null));
        assertEquals(Locale.US, ConversionService.localeForChoice("Klingon"));
    }

    @Test
    public void testIsRightToLeft() {
        assertTrue(ConversionService.isRightToLeft(Locale.forLanguageTag("ar-SA")));
        assertFalse(ConversionService.isRightToLeft(Locale.US));
        assertFalse(ConversionService.isRightToLeft(null));
    }

    // display helpers

    @Test
    public void testBuildUnitNames_mapsIdToName() {
        Map<Integer, String> names = ConversionService.buildUnitNames(List.of(celsius, fahrenheit));
        assertEquals(2, names.size());
        assertEquals("Celsius", names.get(1));
        assertEquals("Fahrenheit", names.get(2));
    }

    @Test
    public void testBuildUnitNames_emptyList() {
        assertTrue(ConversionService.buildUnitNames(List.of()).isEmpty());
    }

    @Test
    public void testUnitNameFor_knownAndUnknown() {
        Map<Integer, String> names = ConversionService.buildUnitNames(List.of(celsius));
        assertEquals("Celsius", ConversionService.unitNameFor(names, 1));
        assertEquals("?", ConversionService.unitNameFor(names, 99));
    }

    @Test
    public void testDefaultToIndex() {
        assertEquals(0, ConversionService.defaultToIndex(0));
        assertEquals(0, ConversionService.defaultToIndex(1));
        assertEquals(1, ConversionService.defaultToIndex(2));
        assertEquals(1, ConversionService.defaultToIndex(6));
    }

    @Test
    public void testFormatTimestamp() {
        assertEquals("2026-11-05 11:17:09",
                ConversionService.formatTimestamp(LocalDateTime.of(2026, 11, 5, 11, 17, 9)));
        assertEquals("", ConversionService.formatTimestamp(null));
    }
}
