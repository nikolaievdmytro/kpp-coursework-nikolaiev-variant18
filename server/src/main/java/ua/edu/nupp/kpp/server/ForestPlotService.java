package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/** Предметна логіка для лісових ділянок: валідація та логування. */
public class ForestPlotService {

    private static final Logger LOG = Logger.getLogger(ForestPlotService.class.getName());

    private final ForestPlotDAO dao = new ForestPlotDAO();

    public List<ForestPlot> getAll() throws SQLException {
        return dao.getAll();
    }

    public void add(ForestPlot p) throws SQLException {
        validate(p);
        dao.add(p);
        LOG.info("Додано лісову ділянку");
    }

    public void update(ForestPlot p) throws SQLException {
        validate(p);
        dao.update(p);
        LOG.info("Оновлено лісову ділянку");
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено лісову ділянку");
    }

    static void validate(ForestPlot p) {
        if (p == null || blank(p.getCode()) || blank(p.getName()) || blank(p.getForestry())) {
            throw new IllegalArgumentException("Код, назва та лісництво ділянки є обов'язковими");
        }
        if (!(p.getAreaHa() > 0)) {
            throw new IllegalArgumentException("Площа ділянки має бути більшою за 0");
        }
        if (!RiskCalculator.isFactor(p.getSensitivityScore())) {
            throw new IllegalArgumentException("F3 має бути в діапазоні 0..100");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }
}
