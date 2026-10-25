package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.OverdueLoan;
import edu.uv.biblioteca.model.UserLoanStat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reunimos las consultas necesarias para los reportes de préstamos.
public final class ReportDao {
    public static final String OVERDUE_SQL = """
        SELECT p.id_prestamo, l.titulo, u.nombre, p.fecha_devolucion,
               CURRENT_DATE - p.fecha_devolucion AS dias_retraso
        FROM prestamos p
        JOIN libros l ON l.id_libro = p.id_libro
        JOIN usuarios u ON u.id_usuario = p.id_usuario
        WHERE p.fecha_devolucion_real IS NULL
          AND p.fecha_devolucion < CURRENT_DATE
        ORDER BY dias_retraso DESC, p.id_prestamo
        """;
    public static final String USER_COUNTS_SQL = """
        SELECT u.id_usuario, u.nombre, COUNT(p.id_prestamo) AS prestamos_activos
        FROM usuarios u
        LEFT JOIN prestamos p ON p.id_usuario = u.id_usuario
             AND p.fecha_devolucion_real IS NULL
        GROUP BY u.id_usuario, u.nombre
        ORDER BY prestamos_activos DESC, u.nombre
        """;
    private final Connection connection;
    public ReportDao(Connection connection) { this.connection = connection; }

    // Consultamos únicamente los préstamos abiertos cuya fecha límite ya pasó.
    public List<OverdueLoan> overdue() throws SQLException {
        List<OverdueLoan> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(OVERDUE_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new OverdueLoan(result.getInt("id_prestamo"), result.getString("titulo"),
                    result.getString("nombre"), result.getDate("fecha_devolucion").toLocalDate(),
                    result.getLong("dias_retraso")));
            }
        }
        return rows;
    }

    // Usamos LEFT JOIN para incluir también a quienes no tienen préstamos activos.
    public List<UserLoanStat> userCounts() throws SQLException {
        List<UserLoanStat> rows = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(USER_COUNTS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new UserLoanStat(result.getInt("id_usuario"), result.getString("nombre"),
                    result.getLong("prestamos_activos")));
            }
        }
        return rows;
    }
}
