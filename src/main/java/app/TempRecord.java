package app;

import java.time.LocalDateTime;

public class TempRecord {
    private int id;
    private double inputValue;
    private double resultValue;
    private int fromUnitId;
    private int toUnitId;
    private String languageCode;
    private LocalDateTime createdAt;

    public TempRecord(double inputValue, double resultValue, int fromUnitId, int toUnitId) {
        this(inputValue, resultValue, fromUnitId, toUnitId, "en");
    }

    public TempRecord(double inputValue, double resultValue, int fromUnitId, int toUnitId, String languageCode) {
        this.inputValue = inputValue;
        this.resultValue = resultValue;
        this.fromUnitId = fromUnitId;
        this.toUnitId = toUnitId;
        this.languageCode = languageCode == null || languageCode.isBlank() ? "en" : languageCode;
    }

    public TempRecord(int id, double inputValue, double resultValue,
                      int fromUnitId, int toUnitId, String languageCode, LocalDateTime createdAt) {
        this.id = id;
        this.inputValue = inputValue;
        this.resultValue = resultValue;
        this.fromUnitId = fromUnitId;
        this.toUnitId = toUnitId;
        this.languageCode = languageCode == null || languageCode.isBlank() ? "en" : languageCode;
        this.createdAt = createdAt;
    }

    public TempRecord(int id, double inputValue, double resultValue,
                      int fromUnitId, int toUnitId, LocalDateTime createdAt) {
        this(id, inputValue, resultValue, fromUnitId, toUnitId, "en", createdAt);
    }

    public int getId() { return id; }
    public double getInputValue() { return inputValue; }
    public double getResultValue() { return resultValue; }
    public int getFromUnitId() { return fromUnitId; }
    public int getToUnitId() { return toUnitId; }
    public String getLanguageCode() { return languageCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
