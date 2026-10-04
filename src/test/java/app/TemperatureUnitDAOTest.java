package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.SQLException;
import java.util.List;

/** Needs the database from schema.sql; skipped automatically when it is not available. */
public class TemperatureUnitDAOTest {

    private final TemperatureUnitDAO dao = new TemperatureUnitDAO();

    @BeforeEach
    public void requireDatabase() {
        assumeTrue(DbTestSupport.isDatabaseAvailable(),
                "MariaDB with temperature_converter_db is not available - skipping");
    }

    @Test
    public void testGetAllUnits_returnsNonEmptyList() throws SQLException {
        List<TemperatureUnit> units = dao.getAllUnits();
        assertNotNull(units);
        assertFalse(units.isEmpty());
    }

    @Test
    public void testGetAllUnits_containsDefaultUnits() throws SQLException {
        List<String> names = dao.getAllUnits().stream().map(TemperatureUnit::getUnitName).toList();
        assertTrue(names.contains("Celsius"));
        assertTrue(names.contains("Fahrenheit"));
        assertTrue(names.contains("Kelvin"));
    }

    @Test
    public void testGetAllUnits_idsArePositiveAndUnique() throws SQLException {
        List<TemperatureUnit> units = dao.getAllUnits();
        long distinctIds = units.stream().map(TemperatureUnit::getId).distinct().count();
        assertEquals(units.size(), distinctIds);
        for (TemperatureUnit unit : units) {
            assertTrue(unit.getId() > 0);
        }
    }
}
