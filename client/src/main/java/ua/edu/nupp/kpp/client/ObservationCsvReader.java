package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

/**
 * Початковий (послідовний) сценарій CSV-імпорту — Етап III: читається один тестовий рядок.
 * Формат: plot_id;observation_type;affected_area_ha;severity_score (перший рядок — заголовок).
 * Фоновий імпорт усього файлу через JavaFX Task буде реалізовано пізніше.
 * Предметну валідацію виконує сервер; тут лише технічний розбір рядка.
 */
public final class ObservationCsvReader {

    /** Відносний шлях: клієнт запускається з каталогу client (mvn javafx:run). */
    public static final Path DEFAULT_PATH = Path.of("sample-data", "observations.csv");

    private static final String SEPARATOR = ";";
    private static final int COLUMNS = 4;

    private ObservationCsvReader() {
    }

    /** Пропускає заголовок і повертає перший рядок даних як клієнтську модель Observation. */
    public static Observation readFirst(Path path) throws IOException {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw new IllegalArgumentException("CSV-файл не знайдено: " + path
                    + ". Запускайте клієнт із каталогу client (mvn javafx:run).");
        }
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                return parseLine(lines.get(i));
            }
        }
        throw new IllegalArgumentException("У CSV-файлі немає рядків із даними");
    }

    /** Розбирає один рядок CSV. Якщо формат неправильний, модель не створюється. */
    public static Observation parseLine(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        if (parts.length != COLUMNS) {
            throw new IllegalArgumentException("Рядок CSV має містити " + COLUMNS
                    + " поля, а знайдено " + parts.length + ": " + line);
        }
        try {
            long plotId = Long.parseLong(parts[0].trim());
            String type = parts[1].trim();
            double affectedAreaHa = Double.parseDouble(parts[2].trim().replace(',', '.'));
            double severityScore = Double.parseDouble(parts[3].trim().replace(',', '.'));
            return new Observation(null, plotId, type, affectedAreaHa, severityScore);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректне число у рядку CSV: " + line);
        }
    }
}
