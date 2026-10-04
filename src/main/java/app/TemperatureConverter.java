package app;

public class TemperatureConverter {
    public double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    public double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    public boolean isExtremeTemperature(double celsius) {
        return celsius < -40 || celsius > 50;
    }

    /**
     * Converts a value to Celsius from the given unit name
     * (Celsius, Fahrenheit or Kelvin, case-insensitive).
     */
    public double toCelsius(double value, String unit) {
        double celsius = switch (unit.toLowerCase()) {
            case "celsius" -> value;
            case "fahrenheit" -> fahrenheitToCelsius(value);
            case "kelvin" -> kelvinToCelsius(value);
            default -> throw new IllegalArgumentException("Unknown temperature unit: " + unit);
        };
        if (celsius < -273.15) {
            throw new IllegalArgumentException("Temperature is below absolute zero.");
        }
        return celsius;
    }

    /** Converts a value between any two supported units, going through Celsius. */
    public double convert(double value, String fromUnit, String toUnit) {
        double celsius = toCelsius(value, fromUnit);
        return switch (toUnit.toLowerCase()) {
            case "celsius" -> celsius;
            case "fahrenheit" -> celsiusToFahrenheit(celsius);
            case "kelvin" -> celsiusToKelvin(celsius);
            default -> throw new IllegalArgumentException("Unknown temperature unit: " + toUnit);
        };
    }
}
