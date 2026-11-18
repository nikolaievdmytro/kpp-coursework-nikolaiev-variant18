package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Доступ до таблиці violation. Стан зберігається текстом (name() / valueOf()).
 * DAO лише записує стан; допустимість переходу перевіряє Service.
 */
public class ViolationDAO {

    private static final String COLUMNS = "id, observation_id, state, f1, f2, f3, risk_index";

    public void add(Violation v) throws SQLException {
        String sql = "INSERT INTO violation(observation_id, state, f1, f2, f3, risk_index) "
                + "VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, v);
            ps.executeUpdate();
        }
    }

    public List<Violation> getAll() throws SQLException {
        List<Violation> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM violation ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Violation getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM violation WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Violation v) throws SQLException {
        String sql = "UPDATE violation SET observation_id=?, state=?, f1=?, f2=?, f3=?, risk_index=? "
                + "WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, v);
            ps.setLong(7, v.getId());
            ps.executeUpdate();
        }
    }

    /** Змінює лише стан. Чи дозволений перехід — вирішує Service, не DAO. */
    public void updateState(long id, ViolationState state) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE violation SET state=? WHERE id=?")) {
            ps.setString(1, state.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM violation WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Violation v) throws SQLException {
        ps.setLong(1, v.getObservationId());
        ps.setString(2, v.getState().name());
        ps.setDouble(3, v.getF1());
        ps.setDouble(4, v.getF2());
        ps.setDouble(5, v.getF3());
        ps.setDouble(6, v.getRiskIndex());
    }

    private Violation map(ResultSet rs) throws SQLException {
        return new Violation(rs.getLong("id"), rs.getLong("observation_id"),
                ViolationState.valueOf(rs.getString("state")),
                rs.getDouble("f1"), rs.getDouble("f2"), rs.getDouble("f3"), rs.getDouble("risk_index"));
    }
}
