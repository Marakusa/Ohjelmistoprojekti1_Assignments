package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TemperatureConverterTest {

    private final TemperatureConverter converter = new TemperatureConverter();

    // ---------- fahrenheitToCelsius ----------

    @Test
    public void testFahrenheitToCelsius_freezingPoint() {
        assertEquals(0.0, converter.fahrenheitToCelsius(32), 0.0001);
    }

    @Test
    public void testFahrenheitToCelsius_boilingPoint() {
        assertEquals(100.0, converter.fahrenheitToCelsius(212), 0.0001);
    }

    @Test
    public void testFahrenheitToCelsius_minus40IsSameInBothScales() {
        assertEquals(-40.0, converter.fahrenheitToCelsius(-40), 0.0001);
    }

    // ---------- celsiusToFahrenheit ----------

    @Test
    public void testCelsiusToFahrenheit_freezingPoint() {
        assertEquals(32.0, converter.celsiusToFahrenheit(0), 0.0001);
    }

    @Test
    public void testCelsiusToFahrenheit_boilingPoint() {
        assertEquals(212.0, converter.celsiusToFahrenheit(100), 0.0001);
    }

    @Test
    public void testCelsiusToFahrenheit_minus40IsSameInBothScales() {
        assertEquals(-40.0, converter.celsiusToFahrenheit(-40), 0.0001);
    }

    // ---------- kelvinToCelsius ----------

    @Test
    public void testKelvinToCelsius_freezingPoint() {
        assertEquals(0.0, converter.kelvinToCelsius(273.15), 0.0001);
    }

    @Test
    public void testKelvinToCelsius_absoluteZero() {
        assertEquals(-273.15, converter.kelvinToCelsius(0), 0.0001);
    }

    // ---------- celsiusToKelvin ----------

    @Test
    public void testCelsiusToKelvin_freezingPoint() {
        assertEquals(273.15, converter.celsiusToKelvin(0), 0.0001);
    }

    @Test
    public void testCelsiusToKelvin_absoluteZero() {
        assertEquals(0.0, converter.celsiusToKelvin(-273.15), 0.0001);
    }

    // ---------- isExtremeTemperature ----------

    @Test
    public void testIsExtreme_normalTemperature_false() {
        assertFalse(converter.isExtremeTemperature(20));
    }

    @Test
    public void testIsExtreme_lowerBoundary_notExtreme() {
        assertFalse(converter.isExtremeTemperature(-40));
    }

    @Test
    public void testIsExtreme_belowLowerBoundary_extreme() {
        assertTrue(converter.isExtremeTemperature(-40.1));
    }

    @Test
    public void testIsExtreme_upperBoundary_notExtreme() {
        assertFalse(converter.isExtremeTemperature(50));
    }

    @Test
    public void testIsExtreme_aboveUpperBoundary_extreme() {
        assertTrue(converter.isExtremeTemperature(50.1));
    }

    // ---------- toCelsius ----------

    @Test
    public void testToCelsius_fromCelsius_unchanged() {
        assertEquals(25.0, converter.toCelsius(25, "Celsius"), 0.0001);
    }

    @Test
    public void testToCelsius_fromFahrenheit() {
        assertEquals(100.0, converter.toCelsius(212, "Fahrenheit"), 0.0001);
    }

    @Test
    public void testToCelsius_fromKelvin() {
        assertEquals(0.0, converter.toCelsius(273.15, "Kelvin"), 0.0001);
    }

    @Test
    public void testToCelsius_unitNameIsCaseInsensitive() {
        assertEquals(100.0, converter.toCelsius(212, "FAHRENHEIT"), 0.0001);
        assertEquals(0.0, converter.toCelsius(273.15, "kelvin"), 0.0001);
    }

    @Test
    public void testToCelsius_unknownUnit_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.toCelsius(10, "Rankine"));
    }

    @Test
    public void testToCelsius_absoluteZeroInKelvin_isAllowed() {
        assertEquals(-273.15, converter.toCelsius(0, "Kelvin"), 0.0001);
    }

    @Test
    public void testToCelsius_belowAbsoluteZeroKelvin_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.toCelsius(-1, "Kelvin"));
    }

    @Test
    public void testToCelsius_belowAbsoluteZeroCelsius_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.toCelsius(-300, "Celsius"));
    }

    @Test
    public void testToCelsius_belowAbsoluteZeroFahrenheit_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.toCelsius(-500, "Fahrenheit"));
    }

    // ---------- convert ----------

    @Test
    public void testConvert_celsiusToFahrenheit() {
        assertEquals(212.0, converter.convert(100, "Celsius", "Fahrenheit"), 0.0001);
    }

    @Test
    public void testConvert_fahrenheitToKelvin() {
        assertEquals(273.15, converter.convert(32, "Fahrenheit", "Kelvin"), 0.0001);
    }

    @Test
    public void testConvert_kelvinToCelsius() {
        assertEquals(0.0, converter.convert(273.15, "Kelvin", "Celsius"), 0.0001);
    }

    @Test
    public void testConvert_sameUnit_returnsSameValue() {
        assertEquals(36.6, converter.convert(36.6, "Celsius", "Celsius"), 0.0001);
        assertEquals(300.0, converter.convert(300, "Kelvin", "Kelvin"), 0.0001);
    }

    @Test
    public void testConvert_roundTrip_returnsOriginalValue() {
        double fahrenheit = converter.convert(37.5, "Celsius", "Fahrenheit");
        assertEquals(37.5, converter.convert(fahrenheit, "Fahrenheit", "Celsius"), 0.0001);
    }

    @Test
    public void testConvert_unknownFromUnit_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(10, "Rankine", "Celsius"));
    }

    @Test
    public void testConvert_unknownToUnit_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(10, "Celsius", "Rankine"));
    }

    @Test
    public void testConvert_belowAbsoluteZero_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> converter.convert(-1, "Kelvin", "Celsius"));
    }
}
