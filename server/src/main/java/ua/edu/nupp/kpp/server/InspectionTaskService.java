package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка інспекційних завдань: валідація, перевірка порушення, логування. */
public class InspectionTaskService {

    private static final Logger LOG = Logger.getLogger(InspectionTaskService.class.getName());

    private final InspectionTaskDAO dao = new InspectionTaskDAO();
    private final ViolationDAO violationDAO = new ViolationDAO();

    public List<InspectionTask> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(InspectionTask t) throws SQLException {
        validate(t);
        requireViolation(t.getViolationId());
        dao.add(t);
        LOG.info("Додано інспекційне завдання");
    }

    public void update(InspectionTask t) throws SQLException {
        validate(t);
        requireViolation(t.getViolationId());
        dao.update(t);
        LOG.info("Оновлено інспекційне завдання");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено інспекційне завдання");
    }

    static void validate(InspectionTask t) {
        if (t == null || t.getViolationId() <= 0
                || t.getDescription() == null || t.getDescription().isBlank()) {
            throw new IllegalArgumentException("Некоректні дані інспекційного завдання");
        }
    }

    private void requireViolation(long id) throws SQLException {
        if (violationDAO.getById(id) == null) {
            throw new IllegalArgumentException("Порушення не знайдено");
        }
    }
}
