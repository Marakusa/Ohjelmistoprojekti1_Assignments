package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helpers for the tests that need a real MariaDB database.
 * Those tests skip themselves (JUnit assumptions) when the database
 * is not running or the required schema is not present.
 */
final class DbTestSupport {

    private DbTestSupport() { }

    /** True if the database accepts connections and both tables exist. */
    static boolean isDatabaseAvailable() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            return hasTable(st, "unit") || hasTable(st, "temperature_unit")
                    && hasTable(st, "temp_record");
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean hasTable(Statement st, String tableName) throws SQLException {
        try (ResultSet rs = st.executeQuery(
                "SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '" + tableName + "' LIMIT 1")) {
            return rs.next();
        }
    }

    /** Removes rows that a test inserted, identified by a unique marker input value. */
    static void deleteRecordsWithInputValue(double inputValue) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM temp_record WHERE input_value = ?")) {
            ps.setDouble(1, inputValue);
            ps.executeUpdate();
        }
    }
}
