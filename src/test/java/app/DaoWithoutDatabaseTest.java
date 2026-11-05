package app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DaoWithoutDatabaseTest {

    @Test
    public void testResolveLocalizedName_prefersTranslatedValue() {
        assertEquals("Celsius",
                TemperatureUnitDAO.resolveLocalizedName("fi", "Celsius", "Celcius", "Default"));
    }

    @Test
    public void testResolveLocalizedName_fallsBackToEnglish() {
        assertEquals("Fahrenheit",
                TemperatureUnitDAO.resolveLocalizedName("fr", "", "Fahrenheit", "Default"));
    }

    @Test
    public void testResolveLocalizedName_fallsBackToDefaultName() {
        assertEquals("Kelvin",
                TemperatureUnitDAO.resolveLocalizedName("de", "", "", "Kelvin"));
    }

    @Test
    public void testResolveLocalizedName_returnsEmptyWhenNoNamesExist() {
        assertEquals("",
                TemperatureUnitDAO.resolveLocalizedName("ar", "", "", ""));
    }
}
