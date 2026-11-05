package app;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class Main extends Application {

    private final ConversionService service = new ConversionService();
    private final TemperatureUnitDAO unitDAO = new TemperatureUnitDAO();
    private final TempRecordDAO recordDAO = new TempRecordDAO();
    private final Map<Integer, String> unitNames = new HashMap<>();
    private ResourceBundle bundle;

    private TextField valueField;
    private ComboBox<TemperatureUnit> fromComboBox;
    private ComboBox<TemperatureUnit> toComboBox;
    private Label resultLabel;
    private TableView<TempRecord> tableView;
    private Locale locale;

    @Override
    public void start(Stage stage) {
        locale = promptForLanguage();
        bundle = ResourceBundle.getBundle("MessagesBundle", locale);
        stage.setTitle(bundle.getString("title"));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(15));

        valueField = new TextField();
        fromComboBox = new ComboBox<>();
        toComboBox = new ComboBox<>();
        loadUnits();

        Button convertButton = new Button(bundle.getString("convert"));
        resultLabel = new Label();

        form.add(new Label(bundle.getString("temperature")), 0, 0);
        form.add(valueField, 1, 0);
        form.add(new Label(bundle.getString("from")), 0, 1);
        form.add(fromComboBox, 1, 1);
        form.add(new Label(bundle.getString("to")), 0, 2);
        form.add(toComboBox, 1, 2);
        form.add(convertButton, 1, 3);
        form.add(resultLabel, 1, 4);

        tableView = buildTableView();
        loadRecords();

        convertButton.setOnAction(e -> handleConvertAndSave());
        valueField.setOnAction(e -> handleConvertAndSave());

        VBox root = new VBox(15, form, new Label(bundle.getString("savedRecords")), tableView);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_LEFT);
        root.setNodeOrientation(ConversionService.isRightToLeft(locale)
                ? NodeOrientation.RIGHT_TO_LEFT
                : NodeOrientation.LEFT_TO_RIGHT);

        stage.setScene(new Scene(root, 600, 550));
        stage.show();
    }

    private Locale promptForLanguage() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("English", "English", "Finnish", "Swedish", "Japanese", "Arabic");
        dialog.setTitle("Language");
        dialog.setHeaderText("Select your preferred language");
        dialog.setContentText("Language:");

        Optional<String> choice = dialog.showAndWait();
        return ConversionService.localeForChoice(choice.orElse(null));
    }

    private void loadUnits() {
        try {
            List<TemperatureUnit> units = unitDAO.getUnitsForLanguage(locale.getLanguage());
            unitNames.clear();
            unitNames.putAll(ConversionService.buildUnitNames(units));
            fromComboBox.getItems().setAll(units);
            toComboBox.getItems().setAll(units);
            if (!units.isEmpty()) {
                fromComboBox.getSelectionModel().selectFirst();
                toComboBox.getSelectionModel().select(ConversionService.defaultToIndex(units.size()));
            }
        } catch (SQLException e) {
            showError("Failed to load temperature units: " + e.getMessage());
        }
    }

    private void handleConvertAndSave() {
        try {
            TemperatureUnit from = fromComboBox.getValue();
            TemperatureUnit to = toComboBox.getValue();

            ConversionService.ConversionResult conversion =
                    service.convert(valueField.getText(), from, to);
            resultLabel.setText(conversion.displayText());

            service.save(conversion, from, to, locale.getLanguage());
            loadRecords();
            valueField.clear();

        } catch (IllegalArgumentException ex) {
            // also covers NumberFormatException; the service supplies the message
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
                ConversionService.unitNameFor(unitNames, data.getValue().getFromUnitId())));

        TableColumn<TempRecord, String> toCol = new TableColumn<>("To");
        toCol.setCellValueFactory(data -> new SimpleStringProperty(
                ConversionService.unitNameFor(unitNames, data.getValue().getToUnitId())));

        TableColumn<TempRecord, Number> resultCol = new TableColumn<>("Result");
        resultCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getResultValue()));

        TableColumn<TempRecord, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(data -> new SimpleStringProperty(
                ConversionService.formatTimestamp(data.getValue().getCreatedAt())));
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
