package app;

public class TemperatureUnit {
    private int id;
    private String unitName;

    public TemperatureUnit(int id, String unitName) {
        this.id = id;
        this.unitName = unitName;
    }

    public int getId() { return id; }
    public String getUnitName() { return unitName; }

    @Override
    public String toString() { return unitName; } // shown in ComboBox
}
