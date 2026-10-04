package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Helpers for the tests that need a real MariaDB database.
 * Those tests skip themselves (JUnit assumptions) when the database
 * is not running or schema.sql has not been applied.
 */
final class DbTestSupport {

    private DbTestSupport() { }

    /** True if the database accepts connections and both tables exist. */
    static boolean isDatabaseAvailable() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.executeQuery("SELECT 1 FROM temperature_unit LIMIT 1").close();
            st.executeQuery("SELECT 1 FROM temp_record LIMIT 1").close();
            return true;
        } catch (SQLException e) {
            return false;
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
