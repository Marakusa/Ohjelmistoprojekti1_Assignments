package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TemperatureUnitTest {

    @Test
    public void testConstructorAndGetters() {
        TemperatureUnit unit = new TemperatureUnit(1, "Celsius");
        assertEquals(1, unit.getId());
        assertEquals("Celsius", unit.getUnitName());
    }

    @Test
    public void testToString_returnsUnitName() {
        TemperatureUnit unit = new TemperatureUnit(2, "Fahrenheit");
        assertEquals("Fahrenheit", unit.toString());
    }
}
