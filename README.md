# Temperature Converter (JavaFX + MariaDB)

A JavaFX desktop application that converts temperatures between Celsius, Fahrenheit and Kelvin, and stores every conversion in a MariaDB database so the history can be viewed in the app.

---

## 1. Assignment Description

**Problem statement.** Build a Java application that converts temperatures between units, with a graphical user interface and a database backend that persists the conversion history. The code should be tested automatically and be buildable and runnable in a reproducible way.

**Key requirements addressed**

- Convert between **Celsius, Fahrenheit and Kelvin** in any direction.
- Reject invalid input (non-numeric text, unknown unit, temperatures below absolute zero).
- Flag **extreme temperatures** (below -40 °C or above 50 °C).
- Read the available units from, and save each conversion to, a **MariaDB** database through a **DAO layer** (`TempRecordDAO`, `TemperatureUnitDAO`) and entity classes (`TempRecord`, `TemperatureUnit`).
- Show the saved conversions in a table in the GUI (newest first).
- Verify the solution with **JUnit 5** tests and a **JaCoCo** coverage report.
- Provide a **Jenkins pipeline** and a **Dockerfile** for automated build, test and packaging.

**Deliverables**

| Deliverable | Location |
|---|---|
| Application source code | `src/main/java/app/` |
| Automated tests | `src/test/java/app/` |
| Maven build (JavaFX, JUnit, JaCoCo, fat JAR) | `pom.xml` |
| CI/CD pipeline | `Jenkinsfile` |
| Container image definition | `Dockerfile` |
| Documentation | this `README.md` |

---

## 2. Technologies & Tools Used

| Area | Tool / Library | Version |
|---|---|---|
| Language | Java | 21 |
| GUI | JavaFX (`javafx-controls`, `javafx-fxml`) | 21.0.6 |
| Database | MariaDB | any recent 10.x / 11.x |
| DB access | JDBC with `mariadb-java-client` | 3.5.9 |
| Build | Apache Maven | 3.9.x |
| Unit testing | JUnit 5 (`junit-jupiter`) | 5.10.2 |
| Code coverage | JaCoCo Maven plugin | 0.8.11 |
| Packaging | `maven-jar-plugin` 3.4.2, `maven-shade-plugin` 3.6.0 (fat JAR) | |
| Running the GUI | `javafx-maven-plugin` | 0.0.8 |
| CI/CD | Jenkins (Maven, JUnit report, JaCoCo, Docker Hub push) | |
| Containers | Docker (base image `maven:3.9.6-eclipse-temurin-21`) | |

---

## 3. Design Approach & Implementation Method

### 3.1 Architecture

The code is split into small classes with one responsibility each, all in package `app`:

```
                     +-----------------------+
                     |  Main  (JavaFX GUI)   |
                     +-----------+-----------+
          uses logic |           | uses persistence
          +----------+           +-----------+
          v                                  v
 +----------------------+        +------------------------+
 | TemperatureConverter |        | TemperatureUnitDAO     |
 | (pure logic, no I/O) |        | TempRecordDAO          |
 +----------------------+        +-----------+------------+
                                              | uses
                                  +-----------v------------+
                                  | DatabaseConnection     |
                                  +-----------+------------+
                                              v
                                       MariaDB (JDBC)

 Entities: TemperatureUnit (id, unitName), TempRecord (id, input, result, fromUnitId, toUnitId, createdAt)
```

| Class | Responsibility |
|---|---|
| `Main` | JavaFX `Application`. Builds the form (value field, two unit drop-downs, *Convert & Save* button, result label) and the history `TableView`; handles user input and shows errors in an alert dialog. |
| `TemperatureConverter` | All conversion rules. Any conversion is done **via Celsius** (`toCelsius` then convert out), so only 2 formulas per unit are needed instead of one per unit pair. Also contains the extreme-temperature check. |
| `TemperatureUnit` | Entity for a row in `temperature_unit`; `toString()` returns the unit name so it displays correctly in the `ComboBox`. |
| `TempRecord` | Entity for a row in `temp_record`. Has two constructors: one for new records (id and timestamp are assigned by the database) and one for records read back from the database. |
| `TemperatureUnitDAO` | `getAllUnits()` - loads the selectable units. |
| `TempRecordDAO` | `save(record)` and `getAllRecords()` (newest first). |
| `DatabaseConnection` | Creates JDBC connections via `DriverManager`. |

### 3.2 Database design

Two tables, linked by foreign keys. The schema is reconstructed from the DAO queries; keep it in the repository as `schema.sql`, which the tests refer to:

```sql
CREATE DATABASE IF NOT EXISTS temperature_converter_db;
USE temperature_converter_db;

CREATE TABLE IF NOT EXISTS temperature_unit (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    unit_name VARCHAR(20) NOT NULL UNIQUE
);

INSERT IGNORE INTO temperature_unit (unit_name)
VALUES ('Celsius'), ('Fahrenheit'), ('Kelvin');

CREATE TABLE IF NOT EXISTS temp_record (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    input_value  DOUBLE NOT NULL,
    result_value DOUBLE NOT NULL,
    from_unit_id INT NOT NULL,
    to_unit_id   INT NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_unit_id) REFERENCES temperature_unit(id),
    FOREIGN KEY (to_unit_id)   REFERENCES temperature_unit(id)
);

CREATE USER IF NOT EXISTS 'appuser'@'%' IDENTIFIED BY 'password';
GRANT SELECT, INSERT, DELETE ON temperature_converter_db.* TO 'appuser'@'%';
FLUSH PRIVILEGES;
```

Units live in their own table (normalised) so a record stores only a unit **id**, and the GUI maps ids back to names for display.

### 3.3 Key decisions

- **Convert through Celsius.** Adding a new unit later needs only two new formulas.
- **Validation in the logic layer.** `toCelsius` throws `IllegalArgumentException` for unknown units and for values below absolute zero (-273.15 °C), so the rule is enforced and tested independently of the GUI.
- **Case-insensitive unit names**, so the logic does not depend on how the unit is capitalised in the database.
- **DAO pattern with `PreparedStatement` and try-with-resources.** Queries are parameterised (no SQL injection) and connections are always closed.
- **Database sets `id` and `created_at`.** The application never fabricates them; the "new record" constructor leaves them unset.
- **GUI input handling.** A decimal comma is accepted (`36,6` is read as `36.6`), pressing Enter in the field triggers the conversion, and `NumberFormatException`, `IllegalArgumentException` and `SQLException` each produce a specific, readable error dialog instead of a crash.
- **Result formatting with `Locale.ROOT`**, so output always uses a decimal point regardless of the computer's language settings.
- **Configurable DB host.** `DatabaseConnection` reads the `DB_HOST` environment variable (default `localhost`), which lets the same code run on the host and inside Docker (`host.docker.internal`).
- **Tests that need the database skip themselves** (JUnit assumptions) when MariaDB is not available, so `mvn test` also works on a machine or build server without a database.

---

## 4. Testing & Quality Assurance Steps

### 4.1 Automated tests (JUnit 5)

The suite has **58 test methods in 8 test classes**. Run it with `mvn test`; with `mvn verify`, JaCoCo also writes a coverage report to `target/site/jacoco/index.html`.

| Test class | Tests | What it verifies | Needs DB |
|---|---:|---|:---:|
| `TemperatureConverterTest` | 32 | All conversion formulas with known reference points (0 °C = 32 °F = 273.15 K, 100 °C = 212 °F, -40 is equal in °C and °F, absolute zero), extreme-temperature boundaries (-40 and 50 are *not* extreme; just beyond them are), case-insensitive unit names, unknown units, below-absolute-zero in every unit, same-unit conversion, round-trip conversion | No |
| `TempRecordTest` | 4 | Both constructors, default `id` = 0 and `createdAt` = null for new records, negative values | No |
| `TemperatureUnitTest` | 2 | Constructor/getters, `toString()` returns the unit name | No |
| `MainTest` | 4 | `Main` is a JavaFX `Application`, can be created without starting the toolkit, has a public static `main`, overrides `start` (no window is opened) | No |
| `DatabaseConnectionTest` | 4 | JDBC URL points to `temperature_converter_db`, credentials are configured, constants are `static final`; live connection is open and valid | 1 of 4 |
| `TemperatureUnitDAOTest` | 3 | Units list is non-empty, contains Celsius/Fahrenheit/Kelvin, ids are positive and unique | Yes |
| `TempRecordDAOTest` | 5 | Save then read back, `created_at` set by the database, two saves create two rows, invalid unit id raises `SQLException` (foreign key), results ordered newest first | Yes |
| `DaoWithoutDatabaseTest` | 4 | With the database **down**, the connection and every DAO method throw `SQLException` rather than failing silently | Runs only when DB is down |

Test design notes:

- Database tests insert rows with a unique marker value (`987654.321`) and delete them in `@AfterEach`, so the history table is left unchanged.
- The "database available" and "database down" tests are complementary: with MariaDB running, the DAO tests run and `DaoWithoutDatabaseTest` is skipped; without it, the opposite happens. Together they cover both situations.

### 4.2 Test cases and expected results

| # | Scenario (input) | Expected result |
|---|---|---|
| 1 | 100 Celsius to Fahrenheit | `100.00 Celsius = 212.00 Fahrenheit` |
| 2 | 32 Fahrenheit to Kelvin | `32.00 Fahrenheit = 273.15 Kelvin` |
| 3 | 273.15 Kelvin to Celsius | `273.15 Kelvin = 0.00 Celsius` |
| 4 | -40 Celsius to Fahrenheit | `-40.00 Celsius = -40.00 Fahrenheit` |
| 5 | 36.6 Celsius to Celsius (same unit) | Value unchanged |
| 6 | 37.5 C to F, then that result F to C | Returns 37.5 (round trip) |
| 7 | 60 Celsius to Fahrenheit | Result shown with `(extreme temperature!)` |
| 8 | 50 Celsius to Kelvin | No extreme warning (boundary) |
| 9 | -41 Celsius to Kelvin | Extreme warning |
| 10 | `36,6` (decimal comma) | Accepted as 36.6 |
| 11 | `abc` or empty field | Error dialog: "Temperature must be a number." |
| 12 | -300 Celsius, -1 Kelvin, -500 Fahrenheit | Error dialog: "Temperature is below absolute zero." |
| 13 | Press Enter in the value field | Conversion runs, same as clicking the button |
| 14 | Successful conversion | New row appears at the top of the table; field is cleared |
| 15 | Close and restart the app | Previous records are still listed (persisted in MariaDB) |
| 16 | Stop MariaDB, start the app | Error dialog "Failed to load temperature units: ..." instead of a crash |

Cases 1-9 and 12 are covered by the automated converter tests; cases 10, 11 and 13-16 involve the GUI and database and are checked manually by running the application (`mvn javafx:run`).

### 4.3 Results

> **Fill in before submitting:** run `mvn clean verify` once with MariaDB running and once without, and enter your real numbers here.

| Run | Command | Tests run | Failures | Skipped |
|---|---|---:|---:|---:|
| With MariaDB running | `mvn clean verify` | _ | _ | _ |
| Without MariaDB | `mvn clean verify` | _ | _ | _ |

JaCoCo coverage (from `target/site/jacoco/index.html`): _ %

Manual GUI test cases 10, 11 and 13-16 above: _ (pass / fail)

### 4.4 Continuous integration

The `Jenkinsfile` runs these stages on every build: checkout, `mvn clean install`, `mvn test`, publish JUnit results, JaCoCo coverage report, Docker image build, and push to Docker Hub.

---

## 5. How to Run

### Prerequisites

- **JDK 21**
- **Apache Maven 3.9+** (downloads JavaFX and the JDBC driver automatically)
- **MariaDB** running on `localhost:3306`
- Docker (optional, only for the container route)

### Steps

1. **Create the database** (using the SQL from section 3.2, saved as `schema.sql`):

   ```bash
   mariadb -u root -p < schema.sql
   ```

   The app connects as user `appuser` with password `password` to `temperature_converter_db` (see `DatabaseConnection.java`). If the database is on another machine, set the `DB_HOST` environment variable.

2. **Compile and run the tests**

   ```bash
   mvn clean test
   ```

   Database tests are skipped automatically if MariaDB is not reachable.

3. **Run the application**

   ```bash
   mvn clean javafx:run
   ```

4. **Use it:** enter a temperature, pick *From* and *To* units, and press **Convert & Save** (or Enter). The result appears under the button and the record is added to the table.

### Optional: coverage report

```bash
mvn clean verify
# open target/site/jacoco/index.html
```

### Optional: fat JAR

```bash
mvn clean package
java --module-path /path/to/javafx-sdk-21/lib \
     --add-modules javafx.controls,javafx.fxml \
     -jar target/temperature_converter.jar
```

### Optional: Docker (Windows host with Xming as X server)

1. Start Xming (XLaunch: *Multiple windows*, display 0, tick *No Access Control*).
2. Make sure MariaDB is running on the Windows host.
3. Build and run:

   ```bash
   docker build -t marakusa/temperature-converter .
   docker run --rm marakusa/temperature-converter
   ```

   The container reaches the host's database through `DB_HOST=host.docker.internal` and shows the GUI on the host's X server.

---

## Project Structure

```
.
├── pom.xml
├── Jenkinsfile
├── Dockerfile
├── schema.sql                      (database script, see section 3.2)
└── src
    ├── main/java/app
    │   ├── Main.java
    │   ├── TemperatureConverter.java
    │   ├── TemperatureUnit.java
    │   ├── TemperatureUnitDAO.java
    │   ├── TempRecord.java
    │   ├── TempRecordDAO.java
    │   └── DatabaseConnection.java
    └── test/java/app
        ├── TemperatureConverterTest.java
        ├── TempRecordTest.java
        ├── TemperatureUnitTest.java
        ├── MainTest.java
        ├── DatabaseConnectionTest.java
        ├── TemperatureUnitDAOTest.java
        ├── TempRecordDAOTest.java
        ├── DaoWithoutDatabaseTest.java
        └── DbTestSupport.java
```
