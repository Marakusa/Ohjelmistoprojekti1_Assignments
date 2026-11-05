package app;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    //private static final String URL = "jdbc:mariadb://host.docker.internal:3306/temperature_converter_db";
    private static final String HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String URL = "jdbc:mariadb://" + HOST + ":3306/temperature_converter_db";
    private static final String USER = "appuser";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        ensureRequiredTablesExist(connection);
        return connection;
    }

    private static void ensureRequiredTablesExist(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        boolean hasUnitTable = false;
        boolean hasTempRecordTable = false;

        try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if ("unit".equalsIgnoreCase(tableName)) {
                    hasUnitTable = true;
                }
                if ("temp_record".equalsIgnoreCase(tableName)) {
                    hasTempRecordTable = true;
                }
            }
        }

        if (!hasUnitTable || !hasTempRecordTable) {
            connection.close();
            throw new SQLException("Required database tables are missing: unit and temp_record");
        }
    }
}