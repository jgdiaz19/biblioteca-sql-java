package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.AuthorDetails;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Reunimos las operaciones SQL del catálogo de autores.
public final class AuthorDao {
    private final Connection connection;

    public AuthorDao(Connection connection) {
        this.connection = connection;
    }

    public List<AuthorDetails> list() throws SQLException {
        String sql = """
                SELECT id_autor, nombre, nacionalidad
                FROM autores
                ORDER BY nombre, id_autor
                """;
        List<AuthorDetails> result = new ArrayList<>();
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql);
             ResultSet rows = statement.executeQuery()) {
            while (rows.next()) {
                result.add(new AuthorDetails(rows.getInt("id_autor"),
                        rows.getString("nombre"), rows.getString("nacionalidad")));
            }
        }
        return result;
    }

    // Comprobamos duplicados antes de registrar o renombrar un autor.
    private void checkName(String name, int exceptId) throws SQLException {
        String sql = """
                SELECT 1 FROM autores
                WHERE LOWER(BTRIM(nombre)) = LOWER(BTRIM(?))
                  AND id_autor <> ?
                LIMIT 1
                """;
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
            statement.setString(1, name);
            statement.setInt(2, exceptId);
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next()) {
                    throw new IllegalArgumentException(
                            "Ya existe un autor con ese nombre. Selecciónelo del catálogo.");
                }
            }
        }
    }

    public int insert(String name, String nationality) throws SQLException {
        checkName(name, -1);
        String sql = """
                INSERT INTO autores (nombre, nacionalidad)
                VALUES (?, ?) RETURNING id_autor
                """;
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
            statement.setString(1, name);
            statement.setString(2, nationality);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) throw new SQLException("No se generó el ID del autor.");
                return rows.getInt(1);
            }
        }
    }

    public void update(AuthorDetails author) throws SQLException {
        checkName(author.name(), author.id());
        String sql = """
                UPDATE autores SET nombre = ?, nacionalidad = ?
                WHERE id_autor = ?
                """;
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
            statement.setString(1, author.name());
            statement.setString(2, author.nationality());
            statement.setInt(3, author.id());
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("El autor ya no existe.");
            }
        }
    }

    public void delete(int authorId) throws SQLException {
        // La clave foránea de libros impide borrar autores con libros asociados.
        String sql = "DELETE FROM autores WHERE id_autor = ?";
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
            statement.setInt(1, authorId);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException("El autor ya no existe.");
            }
        }
    }
}
