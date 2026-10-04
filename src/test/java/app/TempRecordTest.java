package app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TempRecordTest {

    @Test
    public void testNewRecordConstructor_setsFields() {
        TempRecord record = new TempRecord(100.0, 212.0, 1, 2);
        assertEquals(100.0, record.getInputValue(), 0.0001);
        assertEquals(212.0, record.getResultValue(), 0.0001);
        assertEquals(1, record.getFromUnitId());
        assertEquals(2, record.getToUnitId());
    }

    @Test
    public void testNewRecordConstructor_idAndCreatedAtNotSet() {
        TempRecord record = new TempRecord(0.0, 32.0, 1, 2);
        assertEquals(0, record.getId());
        assertNull(record.getCreatedAt());
    }

    @Test
    public void testFullConstructor_setsAllFields() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 4, 19, 0);
        TempRecord record = new TempRecord(7, 273.15, 0.0, 3, 1, now);
        assertEquals(7, record.getId());
        assertEquals(273.15, record.getInputValue(), 0.0001);
        assertEquals(0.0, record.getResultValue(), 0.0001);
        assertEquals(3, record.getFromUnitId());
        assertEquals(1, record.getToUnitId());
        assertEquals(now, record.getCreatedAt());
    }

    @Test
    public void testNegativeValuesAreStoredAsGiven() {
        TempRecord record = new TempRecord(-40.0, -40.0, 1, 2);
        assertEquals(-40.0, record.getInputValue(), 0.0001);
        assertEquals(-40.0, record.getResultValue(), 0.0001);
    }
}
