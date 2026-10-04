package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    //private static final String URL = "jdbc:mariadb://host.docker.internal:3306/temperature_converter_db";
    private static final String URL = "jdbc:mariadb://localhost:3306/temperature_converter_db";
    private static final String USER = "appuser";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}