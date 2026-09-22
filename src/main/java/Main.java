import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TemperatureConverter converter = new TemperatureConverter();
        Scanner scanner = new Scanner(System.in);
        boolean quit = false;

        while (!quit){
            System.out.println("Choose a function:");
            System.out.println("1. Fahrenheit to Celsius");
            System.out.println("2. Celsius to Fahrenheit");
            System.out.println("3. Kelvin to Celsius");
            System.out.println("4. Check if temperature is extreme (Celsius)");
            System.out.println("5. Quit");

            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println("Enter temperature in Fahrenheit:");
                    double fahrenheit = scanner.nextDouble();
                    System.out.println("Temperature in Celsius: " + converter.fahrenheitToCelsius(fahrenheit));
                    break;
                case 2:
                    System.out.println("Enter temperature in Celsius:");
                    double celsius = scanner.nextDouble();
                    System.out.println("Temperature in Fahrenheit: " + converter.celsiusToFahrenheit(celsius));
                    break;
                case 3:
                    System.out.println("Enter temperature in Kelvin:");
                    double kelvin = scanner.nextDouble();
                    System.out.println("Temperature in Celsius: " + converter.kelvinToCelsius(kelvin));
                    break;
                case 4:
                    System.out.println("Enter temperature in Celsius:");
                    double temp = scanner.nextDouble();
                    if (converter.isExtremeTemperature(temp)) {
                        System.out.println("The temperature is extreme.");
                    } else {
                        System.out.println("The temperature is not extreme.");
                    }
                    break;
                case 5:
                    quit = true;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
