package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Єдиний клас, який знає про HTTP. JavaFX викликає лише його методи й отримує готові Java-об'єкти.
 * GET читає дані, POST виконує add / update / delete / transition (application/x-www-form-urlencoded).
 * Тут немає JDBC і предметної логіки: клієнт лише передає значення серверу.
 */
public class HttpForestInspectionClient {

    /** Має збігатися з finalName у server/pom.xml та назвою WAR. */
    private static final String BASE = "http://localhost:8080/forest-inspection-server";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- Лісові ділянки ----------

    public List<ForestPlot> getPlots() {
        return Arrays.asList(read(get("/plots"), ForestPlot[].class));
    }

    public void addPlot(ForestPlot p) {
        post("/plots", plotForm("add", p));
    }

    public void updatePlot(ForestPlot p) {
        post("/plots", plotForm("update", p) + "&id=" + p.getId());
    }

    public void deletePlot(long id) {
        post("/plots", "action=delete&id=" + id);
    }

    // ---------- Спостереження ----------

    public List<Observation> getObservations() {
        return Arrays.asList(read(get("/observations"), Observation[].class));
    }

    public void addObservation(Observation o) {
        post("/observations", observationForm("add", o));
    }

    public void updateObservation(Observation o) {
        post("/observations", observationForm("update", o) + "&id=" + o.getId());
    }

    public void deleteObservation(long id) {
        post("/observations", "action=delete&id=" + id);
    }

    // ---------- Порушення ----------

    public List<Violation> getViolations() {
        return Arrays.asList(read(get("/violations"), Violation[].class));
    }

    public void addViolation(long observationId) {
        post("/violations", "action=add&observationId=" + observationId);
    }

    public void updateViolation(long id, long observationId) {
        post("/violations", "action=update&id=" + id + "&observationId=" + observationId);
    }

    /** Клієнт лише передає бажаний стан; чи дозволений перехід, вирішує сервер. */
    public void changeViolationState(long id, ViolationState state) {
        post("/violations", "action=transition&id=" + id + "&state=" + state.name());
    }

    public void deleteViolation(long id) {
        post("/violations", "action=delete&id=" + id);
    }

    // ---------- Інспекційні завдання ----------

    public List<InspectionTask> getTasks() {
        return Arrays.asList(read(get("/tasks"), InspectionTask[].class));
    }

    public void addTask(InspectionTask t) {
        post("/tasks", taskForm("add", t));
    }

    public void updateTask(InspectionTask t) {
        post("/tasks", taskForm("update", t) + "&id=" + t.getId());
    }

    public void deleteTask(long id) {
        post("/tasks", "action=delete&id=" + id);
    }

    // ---------- Формування параметрів POST ----------

    private String plotForm(String action, ForestPlot p) {
        return "action=" + action
                + "&code=" + enc(p.getCode())
                + "&name=" + enc(p.getName())
                + "&forestry=" + enc(p.getForestry())
                + "&areaHa=" + p.getAreaHa()
                + "&sensitivityScore=" + p.getSensitivityScore();
    }

    private String observationForm(String action, Observation o) {
        return "action=" + action
                + "&plotId=" + o.getPlotId()
                + "&observationType=" + enc(o.getObservationType())
                + "&affectedAreaHa=" + o.getAffectedAreaHa()
                + "&severityScore=" + o.getSeverityScore();
    }

    private String taskForm(String action, InspectionTask t) {
        return "action=" + action
                + "&violationId=" + t.getViolationId()
                + "&description=" + enc(t.getDescription())
                + "&plannedDate=" + enc(t.getPlannedDate());
    }

    // ---------- HTTP ----------

    private String get(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return send(request);
    }

    private String post(String path, String form) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        return send(request);
    }

    private String send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запит до сервера перервано", e);
        } catch (IOException e) {
            throw new RuntimeException("Не вдалося зв'язатися із сервером " + BASE
                    + ". Перевірте, чи запущено Tomcat.", e);
        }
        if (response.statusCode() >= 400) {
            throw new RuntimeException(errorMessage(response));
        }
        return response.body();
    }

    /** Дістає текст із JSON {"error": "..."}; якщо відповідь не JSON (наприклад, 404 від Tomcat) — загальний текст. */
    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode node = mapper.readTree(response.body());
            if (node != null && node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (IOException ignored) {
            // тіло не є JSON — використовуємо загальне повідомлення нижче
        }
        return "Сервер повернув помилку HTTP " + response.statusCode();
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Помилка JSON: відповідь сервера має неочікуваний формат", e);
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
