package app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.sql.SQLException;

/**
 * Checks that the DAOs report a missing database as SQLException instead of
 * failing silently. Skipped when the database is running.
 */
public class DaoWithoutDatabaseTest {

    @BeforeEach
    public void requireNoDatabase() {
        assumeFalse(DbTestSupport.isDatabaseAvailable(),
                "Database is available - skipping the 'database down' checks");
    }

    @Test
    public void testGetConnection_withoutDatabase_throwsSQLException() {
        assertThrows(SQLException.class, DatabaseConnection::getConnection);
    }

    @Test
    public void testUnitDao_getAllUnits_withoutDatabase_throwsSQLException() {
        assertThrows(SQLException.class, () -> new TemperatureUnitDAO().getAllUnits());
    }

    @Test
    public void testRecordDao_save_withoutDatabase_throwsSQLException() {
        assertThrows(SQLException.class,
                () -> new TempRecordDAO().save(new TempRecord(1.0, 2.0, 1, 2)));
    }

    @Test
    public void testRecordDao_getAllRecords_withoutDatabase_throwsSQLException() {
        assertThrows(SQLException.class, () -> new TempRecordDAO().getAllRecords());
    }
}
