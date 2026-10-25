package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.Loan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class LoanDao {
    public static final String ACTIVE_SQL = "SELECT p.id_prestamo, l.titulo, u.nombre, p.fecha_devolucion "
          + "FROM prestamos p JOIN libros l ON l.id_libro = p.id_libro "
          + "JOIN usuarios u ON u.id_usuario = p.id_usuario "
          + "WHERE p.fecha_devolucion_real IS NULL ORDER BY p.fecha_devolucion, p.id_prestamo";
    private final Connection connection;
    public LoanDao(Connection connection) { this.connection = connection; }
    public List<Loan> active() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(ACTIVE_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) loans.add(new Loan(rs.getInt("id_prestamo"),rs.getString("titulo"),
                    rs.getString("nombre"),rs.getDate("fecha_devolucion").toLocalDate()));
        }
        return loans;
    }
}
