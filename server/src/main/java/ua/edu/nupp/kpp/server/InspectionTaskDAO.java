package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Доступ до таблиці inspection_task. Предметної логіки тут немає. */
public class InspectionTaskDAO {

    private static final String COLUMNS = "id, violation_id, description, planned_date";

    public void add(InspectionTask t) throws SQLException {
        String sql = "INSERT INTO inspection_task(violation_id, description, planned_date) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.executeUpdate();
        }
    }

    public List<InspectionTask> getAll() throws SQLException {
        List<InspectionTask> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM inspection_task ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public InspectionTask getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM inspection_task WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(InspectionTask t) throws SQLException {
        String sql = "UPDATE inspection_task SET violation_id=?, description=?, planned_date=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.setLong(4, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM inspection_task WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, InspectionTask t) throws SQLException {
        ps.setLong(1, t.getViolationId());
        ps.setString(2, t.getDescription());
        ps.setString(3, t.getPlannedDate());
    }

    private InspectionTask map(ResultSet rs) throws SQLException {
        return new InspectionTask(rs.getLong("id"), rs.getLong("violation_id"),
                rs.getString("description"), rs.getString("planned_date"));
    }
}
