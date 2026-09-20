import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {

    private final TemperatureConverter converter = new TemperatureConverter();

    @org.junit.jupiter.api.Test
    void fahrenheitToCelsius() {
        assertEquals(0.0, converter.fahrenheitToCelsius(32), 0.001);
        assertEquals(100.0, converter.fahrenheitToCelsius(212), 0.001);
        assertEquals(-40.0, converter.fahrenheitToCelsius(-40), 0.001);
        assertEquals(37.7778, converter.fahrenheitToCelsius(100), 0.001);
        assertEquals(-17.7778, converter.fahrenheitToCelsius(0), 0.001);
    }

    @org.junit.jupiter.api.Test
    void celsiusToFahrenheit() {
        assertEquals(32.0, converter.celsiusToFahrenheit(0), 0.001);
        assertEquals(212.0, converter.celsiusToFahrenheit(100), 0.001);
        assertEquals(-40.0, converter.celsiusToFahrenheit(-40), 0.001);
        assertEquals(98.0, converter.celsiusToFahrenheit(36.6667), 0.001);
        assertEquals(50.0, converter.celsiusToFahrenheit(10), 0.001);
    }

    @org.junit.jupiter.api.Test
    void kelvinToCelsius() {
        assertEquals(0.0, converter.kelvinToCelsius(273.15), 0.001);
        assertEquals(100.0, converter.kelvinToCelsius(373.15), 0.001);
        assertEquals(-273.15, converter.kelvinToCelsius(0), 0.001);
        assertEquals(-40.0, converter.kelvinToCelsius(233.15), 0.001);
        assertEquals(20.0, converter.kelvinToCelsius(293.15), 0.001);
    }

    @org.junit.jupiter.api.Test
    void isExtremeTemperature() {
        // Normal temperatures
        assertFalse(converter.isExtremeTemperature(0));
        assertFalse(converter.isExtremeTemperature(20));
        assertFalse(converter.isExtremeTemperature(-20));
        assertFalse(converter.isExtremeTemperature(49));

        // Edge values
        assertFalse(converter.isExtremeTemperature(-40));
        assertFalse(converter.isExtremeTemperature(50));

        // Barely outside the boundaries
        assertTrue(converter.isExtremeTemperature(-40.1));
        assertTrue(converter.isExtremeTemperature(50.1));

        // Extreme temperatures
        assertTrue(converter.isExtremeTemperature(-100));
        assertTrue(converter.isExtremeTemperature(100));
    }
}