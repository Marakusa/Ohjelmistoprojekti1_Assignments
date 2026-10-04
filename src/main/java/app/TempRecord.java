package app;

import java.time.LocalDateTime;

public class TempRecord {
    private int id;
    private double inputValue;
    private double resultValue;
    private int fromUnitId;
    private int toUnitId;
    private LocalDateTime createdAt;

    // Used when creating a new record (id and createdAt are set by the database)
    public TempRecord(double inputValue, double resultValue, int fromUnitId, int toUnitId) {
        this.inputValue = inputValue;
        this.resultValue = resultValue;
        this.fromUnitId = fromUnitId;
        this.toUnitId = toUnitId;
    }

    // Used when reading a record from the database
    public TempRecord(int id, double inputValue, double resultValue,
                      int fromUnitId, int toUnitId, LocalDateTime createdAt) {
        this.id = id;
        this.inputValue = inputValue;
        this.resultValue = resultValue;
        this.fromUnitId = fromUnitId;
        this.toUnitId = toUnitId;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public double getInputValue() { return inputValue; }
    public double getResultValue() { return resultValue; }
    public int getFromUnitId() { return fromUnitId; }
    public int getToUnitId() { return toUnitId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
