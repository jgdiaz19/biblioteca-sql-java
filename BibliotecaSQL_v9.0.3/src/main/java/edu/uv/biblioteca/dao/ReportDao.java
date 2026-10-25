package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.OverdueLoan;
import edu.uv.biblioteca.model.UserLoanStat;
import edu.uv.biblioteca.model.LoanHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reunimos las consultas necesarias para los reportes de préstamos.
// Separamos los reportes del código de la interfaz para poder reutilizarlos.
public final class ReportDao {
    public static final String OVERDUE_SQL = """
        SELECT p.id_prestamo, l.titulo, u.nombre, p.fecha_prestamo, p.fecha_devolucion,
               CURRENT_DATE - p.fecha_devolucion AS dias_retraso
        FROM prestamos p
        JOIN libros l ON l.id_libro = p.id_libro
        JOIN usuarios u ON u.id_usuario = p.id_usuario
        WHERE p.fecha_devolucion_real IS NULL
          AND p.fecha_devolucion < CURRENT_DATE
        ORDER BY dias_retraso DESC, p.id_prestamo
        """;
    public static final String USER_COUNTS_SQL = """
        SELECT u.id_usuario, u.nombre,
               COUNT(p.id_prestamo) AS total,
               COUNT(p.id_prestamo) FILTER (WHERE p.fecha_devolucion_real IS NULL) AS activos,
               COUNT(p.id_prestamo) FILTER (WHERE p.fecha_devolucion_real IS NOT NULL) AS devueltos
        FROM usuarios u
        LEFT JOIN prestamos p ON p.id_usuario = u.id_usuario
        GROUP BY u.id_usuario, u.nombre
        ORDER BY total DESC, u.nombre
        """;
    public static final String HISTORY_SQL = """
        SELECT p.id_prestamo, p.id_usuario, u.nombre, p.id_libro, l.titulo,
               p.fecha_prestamo, p.fecha_devolucion, p.fecha_devolucion_real
        FROM prestamos p
        JOIN usuarios u ON u.id_usuario = p.id_usuario
        JOIN libros l ON l.id_libro = p.id_libro
        WHERE p.id_usuario = ?
        ORDER BY p.fecha_prestamo DESC, p.id_prestamo DESC
        """;
    private final Connection connection;
    public ReportDao(Connection connection) { this.connection = connection; }

    // Consultamos únicamente los préstamos abiertos cuya fecha límite ya pasó.
    public List<OverdueLoan> overdue() throws SQLException {
        List<OverdueLoan> rows = new ArrayList<>();
        try (PreparedStatement statement = SqlTrace.prepare(connection, OVERDUE_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new OverdueLoan(result.getInt("id_prestamo"), result.getString("titulo"),
                    result.getString("nombre"), result.getDate("fecha_prestamo").toLocalDate(),
                    result.getDate("fecha_devolucion").toLocalDate(),
                    result.getLong("dias_retraso")));
            }
        }
        return rows;
    }

    // Usamos LEFT JOIN para incluir también a quienes no tienen préstamos activos.
    public List<UserLoanStat> userCounts() throws SQLException {
        List<UserLoanStat> rows = new ArrayList<>();
        try (PreparedStatement statement = SqlTrace.prepare(connection, USER_COUNTS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new UserLoanStat(result.getInt("id_usuario"), result.getString("nombre"),
                    result.getLong("total"), result.getLong("activos"), result.getLong("devueltos")));
            }
        }
        return rows;
    }
    // Obtenemos el historial completo del usuario que se seleccionó en la tabla.
    public List<LoanHistory> history(int userId) throws SQLException {
        List<LoanHistory> loans = new ArrayList<>();
        try (PreparedStatement stmt = SqlTrace.prepare(connection, HISTORY_SQL)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date returned = rs.getDate("fecha_devolucion_real");
                    loans.add(new LoanHistory(rs.getInt("id_prestamo"), rs.getInt("id_usuario"),
                        rs.getString("nombre"), rs.getInt("id_libro"), rs.getString("titulo"),
                        rs.getDate("fecha_prestamo").toLocalDate(),
                        rs.getDate("fecha_devolucion").toLocalDate(),
                        returned == null ? null : returned.toLocalDate()));
                }
            }
        }
        return loans;
    }
}
