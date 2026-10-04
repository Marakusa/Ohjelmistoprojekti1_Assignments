package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TempRecordDAO {

    public void save(TempRecord record) throws SQLException {
        String sql = "INSERT INTO temp_record (input_value, result_value, from_unit_id, to_unit_id) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, record.getInputValue());
            ps.setDouble(2, record.getResultValue());
            ps.setInt(3, record.getFromUnitId());
            ps.setInt(4, record.getToUnitId());
            ps.executeUpdate();
        }
    }

    public List<TempRecord> getAllRecords() throws SQLException {
        List<TempRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM temp_record ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                records.add(new TempRecord(
                        rs.getInt("id"),
                        rs.getDouble("input_value"),
                        rs.getDouble("result_value"),
                        rs.getInt("from_unit_id"),
                        rs.getInt("to_unit_id"),
                        createdAt
                ));
            }
        }
        return records;
    }
}
