package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка для спостережень: валідація, перевірка ділянки, логування. */
public class ObservationService {

    private static final Logger LOG = Logger.getLogger(ObservationService.class.getName());

    private final ObservationDAO dao = new ObservationDAO();
    private final ForestPlotDAO plotDAO = new ForestPlotDAO();

    public List<Observation> getAll() throws SQLException {
        return dao.getAll();
    }

    public Observation getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Observation o) throws SQLException {
        validate(o);
        requirePlot(o.getPlotId());
        dao.add(o);
        LOG.info("Додано спостереження");
    }

    public void update(Observation o) throws SQLException {
        validate(o);
        requirePlot(o.getPlotId());
        dao.update(o);
        LOG.info("Оновлено спостереження");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено спостереження");
    }

    static void validate(Observation o) {
        if (o == null || o.getPlotId() <= 0 || o.getObservationType() == null
                || o.getObservationType().isBlank()) {
            throw new IllegalArgumentException("Некоректні основні дані спостереження");
        }
        if (!(o.getAffectedAreaHa() >= 0)) {
            throw new IllegalArgumentException("Некоректна площа прояву спостереження");
        }
        if (!RiskCalculator.isFactor(o.getSeverityScore())) {
            throw new IllegalArgumentException("F2 має бути в діапазоні 0..100");
        }
    }

    private void requirePlot(long id) throws SQLException {
        if (plotDAO.getById(id) == null) {
            throw new IllegalArgumentException("Лісову ділянку не знайдено");
        }
    }
}
