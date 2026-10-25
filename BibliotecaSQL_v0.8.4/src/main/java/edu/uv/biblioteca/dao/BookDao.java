package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.BookDetails;
import edu.uv.biblioteca.model.Author;
import java.math.BigDecimal;
import edu.uv.biblioteca.model.CategoryStat;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** DAO: solo consultas SQL, no imprime ni solicita información al usuario. */
public final class BookDao {
    public static final String LIST_SQL = """
            SELECT id_libro, titulo, categoria, disponible
            FROM libros
            ORDER BY id_libro
            """;

    public static final String SEARCH_SQL = """
            SELECT id_libro, titulo, categoria, disponible
            FROM libros
            WHERE CONCAT_WS(' ', id_libro::text, titulo, categoria) ILIKE ?
            ORDER BY titulo
            """;

    public static final String STATS_SQL = """
            SELECT categoria,
                   COUNT(*) AS total,
                   COUNT(*) FILTER (WHERE disponible) AS disponibles
            FROM libros
            GROUP BY categoria
            ORDER BY categoria
            """;

    // Conservamos la conexión que utilizaremos para ejecutar las consultas.
    private final Connection connection;
    public BookDao(Connection connection) { this.connection = connection; }

    public List<Book> list() throws SQLException { return query(LIST_SQL, null); }
    public List<Book> search(String term) throws SQLException { return query(SEARCH_SQL, "%" + term + "%"); }

    private List<Book> query(String sql, String value) throws SQLException {
        List<Book> books = new ArrayList<>();
        // Preparamos la sentencia y dejamos que JDBC cierre sus recursos.
        try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) books.add(new Book(rs.getInt("id_libro"),rs.getString("titulo"),
                            rs.getString("categoria"),rs.getBoolean("disponible")));
            }
        }
        return books;
    }

    public List<CategoryStat> stats() throws SQLException {
        List<CategoryStat> result = new ArrayList<>();
        try (PreparedStatement statement = SqlTrace.prepare(connection, STATS_SQL);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) result.add(new CategoryStat(rs.getString("categoria"),
                    rs.getLong("total"),rs.getLong("disponibles")));
        }
        return result;
    }
    // Recuperamos todos los campos de un libro para completar el formulario.
    public BookDetails details(int id) throws SQLException {
        String sql = """
                SELECT id_libro, titulo, isbn, anio_publicacion, categoria,
                       editorial, numero_paginas, precio, disponible, id_autor
                FROM libros WHERE id_libro = ?
                """;
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("El libro ya no existe.");
                return new BookDetails(rs.getInt("id_libro"), rs.getString("titulo"),
                        rs.getString("isbn"), rs.getInt("anio_publicacion"),
                        rs.getString("categoria"), rs.getString("editorial"),
                        rs.getInt("numero_paginas"), rs.getBigDecimal("precio"),
                        rs.getBoolean("disponible"), rs.getInt("id_autor"));
            }
        }
    }

    // Consultamos autores para que no sea necesario memorizar sus identificadores.
    public List<Author> authors() throws SQLException {
        List<Author> result = new ArrayList<>();
        String sql = "SELECT id_autor, nombre FROM autores ORDER BY nombre";
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.add(new Author(rs.getInt(1), rs.getString(2)));
        }
        return result;
    }

    public int insert(BookDetails b) throws SQLException {
        String sql = """
                INSERT INTO libros (titulo, isbn, anio_publicacion, categoria,
                    editorial, numero_paginas, precio, disponible, id_autor)
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?) RETURNING id_libro
                """;
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            values(stmt, b);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó el identificador del libro.");
                return rs.getInt(1);
            }
        }
    }

    public void update(BookDetails b) throws SQLException {
        // La disponibilidad se controla desde préstamos; no se edita en el formulario.
        String sql = """
                UPDATE libros
                SET titulo = ?, isbn = ?, anio_publicacion = ?, categoria = ?,
                    editorial = ?, numero_paginas = ?, precio = ?, id_autor = ?
                WHERE id_libro = ?
                """;
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            values(stmt, b);
            stmt.setInt(9, b.id());
            if (stmt.executeUpdate() != 1) throw new IllegalArgumentException("El libro ya no existe.");
        }
    }

    public void delete(int id) throws SQLException {
        // PostgreSQL impedirá borrar un libro relacionado con préstamos históricos.
        try (PreparedStatement stmt = SqlTrace.prepare(connection,
                "DELETE FROM libros WHERE id_libro = ?")) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new IllegalArgumentException("El libro ya no existe.");
        }
    }

    private void values(PreparedStatement stmt, BookDetails b) throws SQLException {
        stmt.setString(1, b.title());
        stmt.setString(2, b.isbn());
        stmt.setInt(3, b.year());
        stmt.setString(4, b.category());
        stmt.setString(5, b.publisher());
        stmt.setInt(6, b.pages());
        stmt.setBigDecimal(7, b.price());
        stmt.setInt(8, b.authorId());
    }
}
