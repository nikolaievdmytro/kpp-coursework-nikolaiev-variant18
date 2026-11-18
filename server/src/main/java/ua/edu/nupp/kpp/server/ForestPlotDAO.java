package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Доступ до таблиці forest_plot. Предметної логіки тут немає. */
public class ForestPlotDAO {

    private static final String COLUMNS = "id, code, name, forestry, area_ha, sensitivity_score";

    public void add(ForestPlot p) throws SQLException {
        String sql = "INSERT INTO forest_plot(code, name, forestry, area_ha, sensitivity_score) "
                + "VALUES(?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.executeUpdate();
        }
    }

    public List<ForestPlot> getAll() throws SQLException {
        List<ForestPlot> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM forest_plot ORDER BY id";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ForestPlot getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM forest_plot WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(ForestPlot p) throws SQLException {
        String sql = "UPDATE forest_plot SET code=?, name=?, forestry=?, area_ha=?, sensitivity_score=? "
                + "WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setLong(6, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM forest_plot WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Java-об'єкт -> параметри SQL. */
    private void bind(PreparedStatement ps, ForestPlot p) throws SQLException {
        ps.setString(1, p.getCode());
        ps.setString(2, p.getName());
        ps.setString(3, p.getForestry());
        ps.setDouble(4, p.getAreaHa());
        ps.setDouble(5, p.getSensitivityScore());
    }

    /** Рядок ResultSet -> Java-об'єкт. */
    private ForestPlot map(ResultSet rs) throws SQLException {
        return new ForestPlot(rs.getLong("id"), rs.getString("code"), rs.getString("name"),
                rs.getString("forestry"), rs.getDouble("area_ha"), rs.getDouble("sensitivity_score"));
    }
}
