package app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.SQLException;
import java.util.List;

/**
 * Needs the database from schema.sql; skipped automatically when it is not available.
 * Rows created by these tests use a unique marker input value and are deleted afterwards.
 */
public class TempRecordDAOTest {

    private static final double MARKER = 987654.321;

    private final TempRecordDAO recordDAO = new TempRecordDAO();
    private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
    private int celsiusId;
    private int fahrenheitId;

    @BeforeEach
    public void setUp() throws SQLException {
        assumeTrue(DbTestSupport.isDatabaseAvailable(),
                "MariaDB with temperature_converter_db is not available - skipping");
        DbTestSupport.deleteRecordsWithInputValue(MARKER);

        for (TemperatureUnit unit : unitDAO.getAllUnits()) {
            if (unit.getUnitName().equals("Celsius")) celsiusId = unit.getId();
            if (unit.getUnitName().equals("Fahrenheit")) fahrenheitId = unit.getId();
        }
        assumeTrue(celsiusId > 0 && fahrenheitId > 0,
                "Celsius and Fahrenheit rows are missing from temperature_unit - skipping");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (DbTestSupport.isDatabaseAvailable()) {
            DbTestSupport.deleteRecordsWithInputValue(MARKER);
        }
    }

    private TempRecord findMarkerRecord() throws SQLException {
        for (TempRecord record : recordDAO.getAllRecords()) {
            if (Math.abs(record.getInputValue() - MARKER) < 0.0001) {
                return record;
            }
        }
        return null;
    }

    @Test
    public void testSave_thenGetAllRecords_returnsSavedRecord() throws SQLException {
        recordDAO.save(new TempRecord(MARKER, 1.5, celsiusId, fahrenheitId));

        TempRecord saved = findMarkerRecord();
        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
        assertEquals(MARKER, saved.getInputValue(), 0.0001);
        assertEquals(1.5, saved.getResultValue(), 0.0001);
        assertEquals(celsiusId, saved.getFromUnitId());
        assertEquals(fahrenheitId, saved.getToUnitId());
    }

    @Test
    public void testSave_setsCreatedAtFromDatabase() throws SQLException {
        recordDAO.save(new TempRecord(MARKER, 2.5, celsiusId, fahrenheitId));

        TempRecord saved = findMarkerRecord();
        assertNotNull(saved);
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    public void testSave_twice_createsTwoRows() throws SQLException {
        recordDAO.save(new TempRecord(MARKER, 1.0, celsiusId, fahrenheitId));
        recordDAO.save(new TempRecord(MARKER, 2.0, celsiusId, fahrenheitId));

        long count = recordDAO.getAllRecords().stream()
                .filter(r -> Math.abs(r.getInputValue() - MARKER) < 0.0001)
                .count();
        assertEquals(2, count);
    }

    @Test
    public void testSave_unknownUnitId_throwsSQLException() {
        TempRecord invalid = new TempRecord(MARKER, 1.0, -1, -1);
        assertThrows(SQLException.class, () -> recordDAO.save(invalid));
    }

    @Test
    public void testGetAllRecords_returnsNewestFirst() throws SQLException {
        recordDAO.save(new TempRecord(MARKER, 1.0, celsiusId, fahrenheitId));

        List<TempRecord> records = recordDAO.getAllRecords();
        assertNotNull(records);
        for (int i = 1; i < records.size(); i++) {
            assertFalse(records.get(i - 1).getCreatedAt().isBefore(records.get(i).getCreatedAt()),
                    "records should be ordered by created_at descending");
        }
    }
}
