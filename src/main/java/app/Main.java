package app;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main extends Application {

    private final TemperatureConverter converter = new TemperatureConverter();
    private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
    private final TempRecordDAO recordDAO = new TempRecordDAO();
    private final Map<Integer, String> unitNames = new HashMap<>();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TextField valueField;
    private ComboBox<TemperatureUnit> fromComboBox;
    private ComboBox<TemperatureUnit> toComboBox;
    private Label resultLabel;
    private TableView<TempRecord> tableView;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Temperature Converter");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        valueField = new TextField();
        fromComboBox = new ComboBox<>();
        toComboBox = new ComboBox<>();
        loadUnits();

        Button convertButton = new Button("Convert & Save");
        resultLabel = new Label();

        form.add(new Label("Temperature:"), 0, 0);
        form.add(valueField, 1, 0);
        form.add(new Label("From:"), 0, 1);
        form.add(fromComboBox, 1, 1);
        form.add(new Label("To:"), 0, 2);
        form.add(toComboBox, 1, 2);
        form.add(convertButton, 1, 3);
        form.add(resultLabel, 1, 4);

        tableView = buildTableView();
        loadRecords();

        convertButton.setOnAction(e -> handleConvertAndSave());
        valueField.setOnAction(e -> handleConvertAndSave());

        VBox root = new VBox(15, form, new Label("Saved Records:"), tableView);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);

        stage.setScene(new Scene(root, 600, 550));
        stage.show();
    }

    private void loadUnits() {
        try {
            List<TemperatureUnit> units = unitDAO.getAllUnits();
            for (TemperatureUnit unit : units) {
                unitNames.put(unit.getId(), unit.getUnitName());
            }
            fromComboBox.getItems().addAll(units);
            toComboBox.getItems().addAll(units);
            if (!units.isEmpty()) {
                fromComboBox.getSelectionModel().selectFirst();
                toComboBox.getSelectionModel().select(units.size() > 1 ? 1 : 0);
            }
        } catch (SQLException e) {
            showError("Failed to load temperature units: " + e.getMessage());
        }
    }

    private void handleConvertAndSave() {
        try {
            double value = Double.parseDouble(valueField.getText().trim().replace(',', '.'));
            TemperatureUnit from = fromComboBox.getValue();
            TemperatureUnit to = toComboBox.getValue();

            if (from == null || to == null) {
                showError("Please select both units.");
                return;
            }

            double result = converter.convert(value, from.getUnitName(), to.getUnitName());
            double celsius = converter.toCelsius(value, from.getUnitName());

            String text = String.format(Locale.ROOT, "%.2f %s = %.2f %s",
                    value, from.getUnitName(), result, to.getUnitName());
            if (converter.isExtremeTemperature(celsius)) {
                text += "  (extreme temperature!)";
            }
            resultLabel.setText(text);

            recordDAO.save(new TempRecord(value, result, from.getId(), to.getId()));

            loadRecords();
            valueField.clear();

        } catch (NumberFormatException ex) {
            showError("Temperature must be a number.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private TableView<TempRecord> buildTableView() {
        TableView<TempRecord> table = new TableView<>();

        TableColumn<TempRecord, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));

        TableColumn<TempRecord, Number> inputCol = new TableColumn<>("Input");
        inputCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getInputValue()));

        TableColumn<TempRecord, String> fromCol = new TableColumn<>("From");
        fromCol.setCellValueFactory(data -> new SimpleStringProperty(
                unitNames.getOrDefault(data.getValue().getFromUnitId(), "?")));

        TableColumn<TempRecord, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(
                unitNames.getOrDefault(data.getValue().getToUnitId(), "?")));

        TableColumn<TempRecord, Number> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getResultValue()));

        TableColumn<TempRecord, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getCreatedAt() == null ? "" : data.getValue().getCreatedAt().format(TIME_FORMAT)));
        timeCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, inputCol, fromCol, toCol, resultCol, timeCol);
        return table;
    }

    private void loadRecords() {
        try {
            List<TempRecord> records = recordDAO.getAllRecords();
            tableView.getItems().setAll(records);
        } catch (SQLException e) {
            showError("Failed to load records: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
