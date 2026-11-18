package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Предметна логіка порушень: розрахунок індексу ризику та чотиристанний життєвий цикл.
 * Користувач не вводить F1–F3 і riskIndex: сервер бере їх зі спостереження та ділянки.
 */
public class ViolationService {

    private static final Logger LOG = Logger.getLogger(ViolationService.class.getName());

    private final ViolationDAO dao = new ViolationDAO();
    private final ObservationDAO observationDAO = new ObservationDAO();
    private final ForestPlotDAO plotDAO = new ForestPlotDAO();

    public List<Violation> getAll() throws SQLException {
        return dao.getAll();
    }

    public Violation getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /** Нове порушення завжди починається зі стану DETECTED. */
    public void add(long observationId) throws SQLException {
        dao.add(fromObservation(null, observationId, ViolationState.DETECTED));
        LOG.info("Створено порушення");
    }

    /** Звичайне редагування: показники перераховуються, поточний стан зберігається. */
    public void update(long id, long observationId) throws SQLException {
        Violation old = dao.getById(id);
        if (old == null) {
            throw new IllegalArgumentException("Порушення не знайдено");
        }
        dao.update(fromObservation(id, observationId, old.getState()));
        LOG.info("Оновлено порушення");
    }

    /** Зміна стану життєвого циклу — окрема операція з перевіркою переходу. */
    public void changeState(long id, ViolationState next) throws SQLException {
        Violation current = dao.getById(id);
        if (current == null) {
            throw new IllegalArgumentException("Порушення не знайдено");
        }
        if (!isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан порушення: " + current.getState() + " -> " + next);
    }

    public void delete(long id) throws SQLException {
        dao.delete(id);
        LOG.info("Видалено порушення");
    }

    /** Дозволені лише три послідовні переходи; пропуски, повернення назад і повтори заборонені. */
    public static boolean isAllowedTransition(ViolationState from, ViolationState to) {
        return (from == ViolationState.DETECTED && to == ViolationState.VERIFIED)
                || (from == ViolationState.VERIFIED && to == ViolationState.ACTION_PLANNED)
                || (from == ViolationState.ACTION_PLANNED && to == ViolationState.CLOSED);
    }

    /** Спостереження -> ділянка -> F1, F2, F3 -> індекс ризику -> готовий Violation. */
    private Violation fromObservation(Long id, long observationId, ViolationState state)
            throws SQLException {
        Observation o = observationDAO.getById(observationId);
        if (o == null) {
            throw new IllegalArgumentException("Спостереження не знайдено");
        }
        ForestPlot plot = plotDAO.getById(o.getPlotId());
        if (plot == null) {
            throw new IllegalArgumentException("Лісову ділянку не знайдено");
        }
        double f1 = RiskCalculator.factor1(o.getAffectedAreaHa(), plot.getAreaHa());
        double f2 = o.getSeverityScore();
        double f3 = plot.getSensitivityScore();
        return new Violation(id, observationId, state, f1, f2, f3, RiskCalculator.calculate(f1, f2, f3));
    }
}
