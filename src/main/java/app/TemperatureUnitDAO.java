package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TemperatureUnitDAO {

    public List<TemperatureUnit> getAllUnits() throws SQLException {
        return getUnitsForLanguage(Locale.getDefault().getLanguage());
    }

    public List<TemperatureUnit> getUnitsForLanguage(String languageCode) throws SQLException {
        List<TemperatureUnit> units = new ArrayList<>();
        String sql = "SELECT u.id, u.key_name, COALESCE(t.name, en_t.name, u.default_name) AS unit_name " +
                "FROM unit u " +
                "LEFT JOIN unit_translation t ON t.unit_id = u.id AND t.lang_code = ? " +
                "LEFT JOIN unit_translation en_t ON en_t.unit_id = u.id AND en_t.lang_code = 'en' " +
                "ORDER BY u.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, languageCode == null || languageCode.isBlank() ? "en" : languageCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    units.add(new TemperatureUnit(
                            rs.getInt("id"),
                            rs.getString("unit_name"),
                            rs.getString("key_name")
                    ));
                }
            }
        }
        return units;
    }
}
