package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TemperatureUnitDAO {

    public List<TemperatureUnit> getAllUnits() throws SQLException {
        List<TemperatureUnit> units = new ArrayList<>();
        String sql = "SELECT id, unit_name FROM temperature_unit";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                units.add(new TemperatureUnit(rs.getInt("id"), rs.getString("unit_name")));
            }
        }
        return units;
    }
}
