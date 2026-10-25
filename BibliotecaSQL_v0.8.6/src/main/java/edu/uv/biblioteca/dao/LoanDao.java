package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.Loan;
import edu.uv.biblioteca.model.LibraryUser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Consultamos los préstamos que todavía no tienen una fecha de devolución real.
public final class LoanDao {
    public static final String ACTIVE_SQL = """
            SELECT p.id_prestamo, l.titulo, u.nombre, p.fecha_devolucion
            FROM prestamos p
            JOIN libros l ON l.id_libro = p.id_libro
            JOIN usuarios u ON u.id_usuario = p.id_usuario
            WHERE p.fecha_devolucion_real IS NULL
            ORDER BY p.fecha_devolucion, p.id_prestamo
            """;

    // Guardamos la conexión recibida desde Main.
    private final Connection connection;
    public LoanDao(Connection connection) { this.connection = connection; }
    public List<Loan> active() throws SQLException {
        // Creamos la lista donde almacenaremos las filas devueltas.
        List<Loan> loans = new ArrayList<>();
        try (PreparedStatement statement = SqlTrace.prepare(connection, ACTIVE_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) loans.add(new Loan(rs.getInt("id_prestamo"),rs.getString("titulo"),
                    rs.getString("nombre"),rs.getDate("fecha_devolucion").toLocalDate()));
        }
        return loans;
    }
    // Consultamos únicamente los usuarios habilitados para solicitar libros.
    public List<LibraryUser> users() throws SQLException {
        List<LibraryUser> result = new ArrayList<>();
        try (PreparedStatement stmt = SqlTrace.prepare(connection,
                "SELECT id_usuario, nombre FROM usuarios WHERE activo = TRUE ORDER BY nombre");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.add(new LibraryUser(rs.getInt(1), rs.getString(2)));
        }
        return result;
    }
}
