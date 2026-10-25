package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.Book;
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
}
