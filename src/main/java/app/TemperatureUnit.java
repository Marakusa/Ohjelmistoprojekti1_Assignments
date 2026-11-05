package app;

public class TemperatureUnit {
    private final int id;
    private final String unitName;
    private final String keyName;

    public TemperatureUnit(int id, String unitName) {
        this(id, unitName, unitName);
    }

    public TemperatureUnit(int id, String unitName, String keyName) {
        this.id = id;
        this.unitName = unitName;
        this.keyName = keyName == null || keyName.isBlank() ? unitName : keyName;
    }

    public int getId() { return id; }
    public String getUnitName() { return unitName; }
    public String getKeyName() { return keyName; }

    @Override
    public String toString() { return unitName; } // shown in ComboBox
}
