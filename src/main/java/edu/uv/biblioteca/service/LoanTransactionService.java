package edu.uv.biblioteca.service;

import edu.uv.biblioteca.dao.SqlTrace;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/** Unidad transaccional para préstamos y devoluciones. No escribe en consola. */
public final class LoanTransactionService {
    // Establecemos el número máximo de préstamos abiertos por usuario.
    private static final int MAX_ACTIVE = 3;
    // Definimos cuántos días tendrá el usuario para devolver el libro.
    private static final int LOAN_DAYS = 14;
    private final Connection connection;

    public LoanTransactionService(Connection connection) { this.connection = Objects.requireNonNull(connection); }

    /** Registra un préstamo atómico y devuelve su identificador. */
    public int borrow(int userId, int bookId) throws SQLException {
        return borrow(userId, bookId, LOAN_DAYS);
    }

    // El plazo se captura antes de abrir la transacción y se valida en el servicio.
    public int borrow(int userId, int bookId, int days) throws SQLException {
        if (userId <= 0 || bookId <= 0) throw new IllegalArgumentException("Los identificadores deben ser positivos.");
        if (days < 1 || days > 365)
            throw new IllegalArgumentException("El plazo debe ser de 1 a 365 días.");
        return transaction(() -> borrowWithinTransaction(userId, bookId, days));
    }

    // Creamos el usuario y registramos el préstamo en una misma transacción.
    // Si el préstamo falla, tampoco se conserva el nuevo usuario.
    public int borrowNewUser(String name, String type, String career, int bookId, int days)
            throws SQLException {
        if (name == null || name.isBlank() || name.length() > 120 ||
                type == null || type.isBlank() || type.length() > 30 ||
                career == null || career.length() > 100)
            throw new IllegalArgumentException("Revise los datos del nuevo usuario.");
        if (days < 1 || days > 365) throw new IllegalArgumentException("El plazo debe ser de 1 a 365 días.");
        return transaction(() -> {
            int userId;
            try (PreparedStatement stmt = SqlTrace.prepare(connection, """
                    INSERT INTO usuarios (nombre, tipo_usuario, carrera, activo)
                    VALUES (?, ?, ?, TRUE) RETURNING id_usuario
                    """)) {
                stmt.setString(1, name.trim());
                stmt.setString(2, type.trim());
                stmt.setString(3, career.isBlank() ? null : career.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    rs.next();
                    userId = rs.getInt(1);
                }
            }
            return borrowWithinTransaction(userId, bookId, days);
        });
    }

    // Validamos al usuario y al libro antes de modificar las dos tablas.
    private int borrowWithinTransaction(int userId, int bookId, int days) throws SQLException {
            // Primero bloqueamos usuario, después libro: orden de bloqueo consistente.
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT activo FROM usuarios WHERE id_usuario = ? FOR UPDATE")) {
                s.setInt(1, userId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("El usuario no existe.");
                    if (!r.getBoolean(1)) throw new IllegalArgumentException("El usuario se encuentra inactivo.");
                }
            }
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT COUNT(*) FROM prestamos WHERE id_usuario = ? AND fecha_devolucion_real IS NULL")) {
                s.setInt(1, userId);
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    if (r.getInt(1) >= MAX_ACTIVE) throw new IllegalArgumentException(
                            "El usuario ya tiene " + MAX_ACTIVE + " préstamos activos.");
                }
            }
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT disponible FROM libros WHERE id_libro = ? FOR UPDATE")) {
                s.setInt(1, bookId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("El libro no existe.");
                    if (!r.getBoolean(1)) throw new IllegalArgumentException("El libro no está disponible.");
                }
            }
            // La restricción parcial UNIQUE de PostgreSQL es la última defensa.
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT 1 FROM prestamos WHERE id_libro = ? AND fecha_devolucion_real IS NULL")) {
                s.setInt(1, bookId);
                try (ResultSet r = s.executeQuery()) {
                    if (r.next()) throw new IllegalArgumentException("El libro tiene un préstamo activo.");
                }
            }
            LocalDate start = LocalDate.now();
            int loanId;
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "INSERT INTO prestamos (id_libro, id_usuario, fecha_prestamo, fecha_devolucion) " +
                    "VALUES (?, ?, ?, ?) RETURNING id_prestamo")) {
                s.setInt(1, bookId);
                s.setInt(2, userId);
                s.setDate(3, Date.valueOf(start));
                s.setDate(4, Date.valueOf(start.plusDays(days)));
                try (ResultSet r = s.executeQuery()) { r.next(); loanId = r.getInt(1); }
            }
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "UPDATE libros SET disponible = FALSE WHERE id_libro = ?")) {
                s.setInt(1, bookId);
                if (s.executeUpdate() != 1) throw new SQLException("No se actualizó la disponibilidad del libro.");
            }
            return loanId;
    }


    /** Registra una devolución únicamente si el préstamo sigue abierto. */
    public void returnBook(int loanId) throws SQLException {
        if (loanId <= 0) throw new IllegalArgumentException("El identificador debe ser positivo.");
        transaction(() -> {
            // Bloqueamos primero el préstamo, después su libro.
            int bookId;
            LocalDate start;
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT id_libro, fecha_prestamo, fecha_devolucion_real FROM prestamos " +
                    "WHERE id_prestamo = ? FOR UPDATE")) {
                s.setInt(1, loanId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("El préstamo no existe.");
                    if (r.getDate("fecha_devolucion_real") != null)
                        throw new IllegalArgumentException("Este préstamo ya fue devuelto.");
                    bookId = r.getInt("id_libro");
                    start = r.getDate("fecha_prestamo").toLocalDate();
                }
            }
            if (LocalDate.now().isBefore(start))
                throw new IllegalArgumentException("No es posible devolver un préstamo con fecha futura.");
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "SELECT id_libro FROM libros WHERE id_libro = ? FOR UPDATE")) {
                s.setInt(1, bookId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) throw new SQLException("Libro asociado no encontrado.");
                }
            }
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "UPDATE prestamos SET fecha_devolucion_real = ? " +
                    "WHERE id_prestamo = ? AND fecha_devolucion_real IS NULL")) {
                s.setDate(1, Date.valueOf(LocalDate.now()));
                s.setInt(2, loanId);
                if (s.executeUpdate() != 1) throw new SQLException("No se pudo completar la devolución.");
            }
            try (PreparedStatement s = SqlTrace.prepare(connection, 
                    "UPDATE libros SET disponible = TRUE WHERE id_libro = ?")) {
                s.setInt(1, bookId);
                if (s.executeUpdate() != 1) throw new SQLException("No se actualizó el libro.");
            }
            return null;
        });
    }

    // Verificamos que los cambios solo afecten la copia operativa.
    public void requireOperationalDatabase() throws SQLException {
        try (PreparedStatement stmt = SqlTrace.prepare(connection, "SELECT current_database()");
             ResultSet rs = stmt.executeQuery()) {
            if (!rs.next() || !"biblioteca_operativa".equals(rs.getString(1)))
                throw new IllegalArgumentException("Solo se puede modificar biblioteca_operativa.");
        }
    }

    // Modificamos el plazo sin cambiar los datos históricos del préstamo.
    public void changeDueDate(int loanId, LocalDate dueDate) throws SQLException {
        if (dueDate == null) throw new IllegalArgumentException("Seleccione una fecha válida.");
        transaction(() -> {
            String sql = """
                    UPDATE prestamos SET fecha_devolucion = ?
                    WHERE id_prestamo = ? AND fecha_devolucion_real IS NULL
                      AND fecha_prestamo <= ?
                    """;
            try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
                stmt.setDate(1, Date.valueOf(dueDate));
                stmt.setInt(2, loanId);
                stmt.setDate(3, Date.valueOf(dueDate));
                if (stmt.executeUpdate() != 1)
                    throw new IllegalArgumentException("El préstamo está cerrado o la fecha no es válida.");
            }
            return null;
        });
    }

    // Cancelamos un préstamo abierto y devolvemos el libro al catálogo.
    // No se permite borrar préstamos ya devueltos porque forman parte del historial.
    public void cancelLoan(int loanId) throws SQLException {
        transaction(() -> {
            int bookId;
            try (PreparedStatement stmt = SqlTrace.prepare(connection, """
                    SELECT id_libro FROM prestamos
                    WHERE id_prestamo = ? AND fecha_devolucion_real IS NULL FOR UPDATE
                    """)) {
                stmt.setInt(1, loanId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Solo pueden cancelarse préstamos activos.");
                    bookId = rs.getInt(1);
                }
            }
            try (PreparedStatement stmt = SqlTrace.prepare(connection,
                    "SELECT id_libro FROM libros WHERE id_libro = ? FOR UPDATE")) {
                stmt.setInt(1, bookId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) throw new SQLException("No se encontró el libro.");
                }
            }
            try (PreparedStatement stmt = SqlTrace.prepare(connection,
                    "DELETE FROM prestamos WHERE id_prestamo = ? AND fecha_devolucion_real IS NULL")) {
                stmt.setInt(1, loanId);
                if (stmt.executeUpdate() != 1) throw new SQLException("No se pudo cancelar el préstamo.");
            }
            try (PreparedStatement stmt = SqlTrace.prepare(connection,
                    "UPDATE libros SET disponible = TRUE WHERE id_libro = ?")) {
                stmt.setInt(1, bookId);
                if (stmt.executeUpdate() != 1) throw new SQLException("No se pudo liberar el libro.");
            }
            return null;
        });
    }

    private interface Work<T> { T run() throws SQLException; }

    private <T> T transaction(Work<T> work) throws SQLException {
        if (!connection.getAutoCommit()) throw new SQLException("Se requiere una conexión sin transacción previa.");
        try (PreparedStatement s = SqlTrace.prepare(connection, "SELECT current_database()");
             ResultSet r = s.executeQuery()) {
            r.next();
            if (!"biblioteca_operativa".equals(r.getString(1)))
                throw new IllegalArgumentException(
                        "Las modificaciones solo se permiten en biblioteca_operativa.");
        }
        // Desactivamos la confirmación automática para agrupar los cambios.
        connection.setAutoCommit(false);
        try {
            T answer = work.run();
            // Confirmamos los cambios cuando todas las operaciones terminan correctamente.
            connection.commit();
            return answer;
        } catch (SQLException | RuntimeException e) {
            // Revertimos toda la operación si ocurre un error.
            try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
