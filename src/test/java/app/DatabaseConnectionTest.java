package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectionTest {

    private static String readConstant(String name) throws Exception {
        Field field = DatabaseConnection.class.getDeclaredField(name);
        field.setAccessible(true);
        return (String) field.get(null);
    }

    @Test
    public void testUrl_pointsToTemperatureConverterMariaDbDatabase() throws Exception {
        String url = readConstant("URL");
        assertTrue(url.startsWith("jdbc:mariadb://"));
        assertTrue(url.endsWith("/temperature_converter_db"));
    }

    @Test
    public void testCredentials_areConfigured() throws Exception {
        assertEquals("appuser", readConstant("USER"));
        assertFalse(readConstant("PASSWORD").isEmpty());
    }

    @Test
    public void testConnectionSettings_areStaticFinalConstants() throws Exception {
        for (String name : new String[]{"URL", "USER", "PASSWORD"}) {
            int modifiers = DatabaseConnection.class.getDeclaredField(name).getModifiers();
            assertTrue(Modifier.isStatic(modifiers), name + " should be static");
            assertTrue(Modifier.isFinal(modifiers), name + " should be final");
        }
    }

    @Test
    public void testGetConnection_returnsOpenValidConnection() throws SQLException {
        assumeTrue(DbTestSupport.isDatabaseAvailable(),
                "MariaDB with temperature_converter_db is not available - skipping");

        try (Connection conn = DatabaseConnection.getConnection()) {
            assertNotNull(conn);
            assertFalse(conn.isClosed());
            assertTrue(conn.isValid(2));
        }
    }
}
