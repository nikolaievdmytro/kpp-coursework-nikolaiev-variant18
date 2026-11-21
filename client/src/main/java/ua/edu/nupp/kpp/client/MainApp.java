package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Графічний інтерфейс. MainApp відповідає лише за введення даних і показ результатів:
 * усі операції виконує HttpForestInspectionClient, а після кожної зміни таблиця
 * повторно завантажується із сервера (а не змінюється локально).
 * JDBC і предметної логіки тут немає.
 */
public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private final HttpForestInspectionClient api = new HttpForestInspectionClient();

    private final TableView<ForestPlot> plots = new TableView<>();
    private final TableView<Observation> observations = new TableView<>();
    private final TableView<Violation> violations = new TableView<>();
    private final TableView<InspectionTask> tasks = new TableView<>();

    /** Дія, яка може завершитися винятком; помилки показуються користувачу у вікні. */
    private interface Action {
        void run() throws Exception;
    }

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Лісові ділянки", plotPane()),
                new Tab("Спостереження", observationPane()),
                new Tab("Порушення", violationPane()),
                new Tab("Інспекційні завдання", taskPane()));
        tabs.getTabs().forEach(t -> t.setClosable(false));
        stage.setScene(new Scene(tabs, 1100, 680));
        stage.setTitle("Інформаційна система лісових інспекцій");
        stage.show();
        refreshAll();
    }

    // ---------- Вкладка «Лісові ділянки» ----------

    private Pane plotPane() {
        plots.getColumns().setAll(col("ID", "id"), col("Код", "code"), col("Назва", "name"),
                col("Лісництво", "forestry"), col("Площа, га", "areaHa"),
                col("Чутливість (F3)", "sensitivityScore"));

        TextField code = new TextField();
        TextField name = new TextField();
        TextField forestry = new TextField();
        TextField area = new TextField();
        TextField sensitivity = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addPlot(readPlot(null, code, name, forestry, area, sensitivity));
            loadPlots();
        }));
        update.setOnAction(e -> safe(() -> {
            ForestPlot selected = selected(plots);
            api.updatePlot(readPlot(selected.getId(), code, name, forestry, area, sensitivity));
            loadPlots();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deletePlot(selected(plots).getId());
            loadPlots();
        }));
        refresh.setOnAction(e -> refreshPlots());

        plots.getSelectionModel().selectedItemProperty().addListener((o, oldValue, p) -> {
            if (p != null) {
                code.setText(p.getCode());
                name.setText(p.getName());
                forestry.setText(p.getForestry());
                area.setText(String.valueOf(p.getAreaHa()));
                sensitivity.setText(String.valueOf(p.getSensitivityScore()));
            }
        });

        return layout(plots, form(
                new Label("Код"), code,
                new Label("Назва"), name,
                new Label("Лісництво"), forestry,
                new Label("Площа, га"), area,
                new Label("Екологічна чутливість F3 (0–100)"), sensitivity,
                new HBox(8, add, update, delete, refresh)));
    }

    private ForestPlot readPlot(Long id, TextField code, TextField name, TextField forestry,
                                TextField area, TextField sensitivity) {
        return new ForestPlot(id, code.getText(), name.getText(), forestry.getText(),
                readDouble(area, "Площа, га"), readDouble(sensitivity, "Екологічна чутливість F3"));
    }

    // ---------- Вкладка «Спостереження» ----------

    private Pane observationPane() {
        observations.getColumns().setAll(col("ID", "id"), col("ID ділянки", "plotId"),
                col("Тип", "observationType"), col("Площа прояву, га", "affectedAreaHa"),
                col("Тяжкість (F2)", "severityScore"));

        TextField plotId = new TextField();
        TextField type = new TextField();
        TextField affected = new TextField();
        TextField severity = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        // ТИМЧАСОВО (початок CSV-імпорту): надсилає серверу один тестовий рядок із CSV-файлу.
        // Повний фоновий імпорт через Task з індикатором прогресу замінить цю кнопку пізніше.
        Button csvOne = new Button("CSV: надіслати перший рядок");
        Label csvStatus = new Label("CSV не використовувався");

        add.setOnAction(e -> safe(() -> {
            api.addObservation(readObservation(null, plotId, type, affected, severity));
            loadObservations();
        }));
        update.setOnAction(e -> safe(() -> {
            Observation selected = selected(observations);
            api.updateObservation(readObservation(selected.getId(), plotId, type, affected, severity));
            loadObservations();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteObservation(selected(observations).getId());
            loadObservations();
        }));
        refresh.setOnAction(e -> refreshObservations());
        csvOne.setOnAction(e -> safe(() -> {
            Observation fromCsv = ObservationCsvReader.readFirst(ObservationCsvReader.DEFAULT_PATH);
            api.addObservation(fromCsv);
            loadObservations();
            csvStatus.setText("Надіслано з CSV: ділянка " + fromCsv.getPlotId() + ", " + fromCsv.getObservationType());
        }));

        observations.getSelectionModel().selectedItemProperty().addListener((o, oldValue, v) -> {
            if (v != null) {
                plotId.setText(String.valueOf(v.getPlotId()));
                type.setText(v.getObservationType());
                affected.setText(String.valueOf(v.getAffectedAreaHa()));
                severity.setText(String.valueOf(v.getSeverityScore()));
            }
        });

        return layout(observations, form(
                new Label("ID ділянки"), plotId,
                new Label("Тип спостереження"), type,
                new Label("Площа прояву, га"), affected,
                new Label("Тяжкість F2 (0–100)"), severity,
                new HBox(8, add, update, delete, refresh),
                csvOne, csvStatus));
    }

    private Observation readObservation(Long id, TextField plotId, TextField type,
                                        TextField affected, TextField severity) {
        return new Observation(id, readLong(plotId, "ID ділянки"), type.getText(),
                readDouble(affected, "Площа прояву, га"), readDouble(severity, "Тяжкість F2"));
    }

    // ---------- Вкладка «Порушення» ----------

    private Pane violationPane() {
        violations.getColumns().setAll(col("ID", "id"), col("ID спостереження", "observationId"),
                col("Стан", "state"), col("F1", "f1"), col("F2", "f2"), col("F3", "f3"),
                col("Індекс ризику I", "riskIndex"));

        TextField observationId = new TextField();
        ComboBox<ViolationState> state = new ComboBox<>(FXCollections.observableArrayList(ViolationState.values()));
        Button add = new Button("Створити");
        Button update = new Button("Перерахувати");
        Button change = new Button("Змінити стан");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        // F1, F2, F3 та індекс ризику користувач не вводить: їх розраховує сервер.
        add.setOnAction(e -> safe(() -> {
            api.addViolation(readLong(observationId, "ID спостереження"));
            loadViolations();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateViolation(selected(violations).getId(), readLong(observationId, "ID спостереження"));
            loadViolations();
        }));
        // Клієнт лише передає вибраний стан; чи дозволений перехід, перевіряє сервер.
        change.setOnAction(e -> safe(() -> {
            Violation selected = selected(violations);
            ViolationState next = state.getValue();
            if (next == null) {
                throw new IllegalArgumentException("Виберіть новий стан");
            }
            api.changeViolationState(selected.getId(), next);
            loadViolations();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteViolation(selected(violations).getId());
            loadViolations();
        }));
        refresh.setOnAction(e -> refreshViolations());

        violations.getSelectionModel().selectedItemProperty().addListener((o, oldValue, v) -> {
            if (v != null) {
                observationId.setText(String.valueOf(v.getObservationId()));
            }
        });

        return layout(violations, form(
                new Label("ID спостереження"), observationId,
                new Label("Новий стан"), state,
                new HBox(8, add, update, change, delete, refresh)));
    }

    // ---------- Вкладка «Інспекційні завдання» ----------

    private Pane taskPane() {
        tasks.getColumns().setAll(col("ID", "id"), col("ID порушення", "violationId"),
                col("Опис", "description"), col("Запланована дата", "plannedDate"));

        TextField violationId = new TextField();
        TextField description = new TextField();
        TextField date = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addTask(readTask(null, violationId, description, date));
            loadTasks();
        }));
        update.setOnAction(e -> safe(() -> {
            InspectionTask selected = selected(tasks);
            api.updateTask(readTask(selected.getId(), violationId, description, date));
            loadTasks();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteTask(selected(tasks).getId());
            loadTasks();
        }));
        refresh.setOnAction(e -> refreshTasks());

        tasks.getSelectionModel().selectedItemProperty().addListener((o, oldValue, t) -> {
            if (t != null) {
                violationId.setText(String.valueOf(t.getViolationId()));
                description.setText(t.getDescription());
                date.setText(t.getPlannedDate() == null ? "" : t.getPlannedDate());
            }
        });

        return layout(tasks, form(
                new Label("ID порушення"), violationId,
                new Label("Опис"), description,
                new Label("Запланована дата (YYYY-MM-DD, необов'язково)"), date,
                new HBox(8, add, update, delete, refresh)));
    }

    private InspectionTask readTask(Long id, TextField violationId, TextField description, TextField date) {
        return new InspectionTask(id, readLong(violationId, "ID порушення"), description.getText(), date.getText());
    }

    // ---------- Оновлення таблиць: дані завжди беруться із сервера ----------

    private void loadPlots() {
        plots.setItems(FXCollections.observableArrayList(api.getPlots()));
    }

    private void loadObservations() {
        observations.setItems(FXCollections.observableArrayList(api.getObservations()));
    }

    private void loadViolations() {
        violations.setItems(FXCollections.observableArrayList(api.getViolations()));
    }

    private void loadTasks() {
        tasks.setItems(FXCollections.observableArrayList(api.getTasks()));
    }

    private void refreshPlots() {
        safe(this::loadPlots);
    }

    private void refreshObservations() {
        safe(this::loadObservations);
    }

    private void refreshViolations() {
        safe(this::loadViolations);
    }

    private void refreshTasks() {
        safe(this::loadTasks);
    }

    /** Усі чотири таблиці в одному блоці: якщо сервер недоступний, користувач бачить одну помилку. */
    private void refreshAll() {
        safe(() -> {
            loadPlots();
            loadObservations();
            loadViolations();
            loadTasks();
        });
    }

    // ---------- Допоміжні методи ----------

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис");
        }
        return value;
    }

    private long readLong(TextField field, String label) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    private double readDouble(TextField field, String label) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }

    private void safe(Action action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(String.valueOf(e.getMessage()));
            showError(e);
        }
    }

    private void showError(Throwable e) {
        String message = e == null || e.getMessage() == null ? "Невідома помилка" : e.getMessage();
        Alert dialog = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        dialog.setTitle("Помилка");
        dialog.setHeaderText(null);
        dialog.showAndWait();
    }

    private Pane form(Node... children) {
        VBox box = new VBox(8, children);
        box.setPadding(new Insets(0, 0, 0, 10));
        return box;
    }

    private Pane layout(TableView<?> table, Pane form) {
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.66);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
