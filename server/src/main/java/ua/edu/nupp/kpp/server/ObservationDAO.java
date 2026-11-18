package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Доступ до таблиці observation. Предметної логіки тут немає. */
public class ObservationDAO {

    private static final String COLUMNS = "id, plot_id, observation_type, affected_area_ha, severity_score";

    public void add(Observation o) throws SQLException {
        String sql = "INSERT INTO observation(plot_id, observation_type, affected_area_ha, severity_score) "
                + "VALUES(?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.executeUpdate();
        }
    }

    public List<Observation> getAll() throws SQLException {
        List<Observation> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM observation ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Observation getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM observation WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Observation o) throws SQLException {
        String sql = "UPDATE observation SET plot_id=?, observation_type=?, affected_area_ha=?, "
                + "severity_score=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, o);
            ps.setLong(5, o.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM observation WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Observation o) throws SQLException {
        ps.setLong(1, o.getPlotId());
        ps.setString(2, o.getObservationType());
        ps.setDouble(3, o.getAffectedAreaHa());
        ps.setDouble(4, o.getSeverityScore());
    }

    private Observation map(ResultSet rs) throws SQLException {
        return new Observation(rs.getLong("id"), rs.getLong("plot_id"), rs.getString("observation_type"),
                rs.getDouble("affected_area_ha"), rs.getDouble("severity_score"));
    }
}
