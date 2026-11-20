package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/forest_inspection";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        // Tomcat не реєструє драйвери з WEB-INF/lib автоматично через DriverManager,
        // тому драйвер MySQL завантажується явно (без цього: "No suitable driver found")
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Драйвер MySQL не знайдено у classpath", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
