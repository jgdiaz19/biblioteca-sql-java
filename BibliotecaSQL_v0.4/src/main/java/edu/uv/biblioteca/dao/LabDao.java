package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.LabResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Conservamos las consultas del laboratorio en un catálogo de solo lectura.
public final class LabDao {
    private record Example(String title, String sql, String explanation) { }
    private static final List<Example> EXAMPLES = List.of(
        new Example("Funciones de texto (A7)", """
            SELECT id_libro, UPPER(titulo) AS titulo_mayusculas, LENGTH(titulo) AS caracteres
            FROM libros ORDER BY id_libro LIMIT 10
            """, "UPPER transforma el texto y LENGTH cuenta sus caracteres."),
        new Example("Agregación y HAVING (A7)", """
            SELECT categoria, COUNT(*) AS cantidad, ROUND(AVG(precio), 2) AS precio_promedio
            FROM libros GROUP BY categoria HAVING COUNT(*) >= 2
            ORDER BY cantidad DESC, categoria
            """, "GROUP BY forma grupos y HAVING filtra los grupos resultantes."),
        new Example("Serie de números (A7)", """
            SELECT generate_series(1, 10) AS numero
            """, "generate_series devuelve diez filas sin consultar una tabla."),
        new Example("Libros y autores (JOIN)", """
            SELECT l.id_libro, l.titulo, a.nombre AS autor
            FROM libros l JOIN autores a ON a.id_autor = l.id_autor
            ORDER BY l.id_libro LIMIT 12
            """, "JOIN relaciona cada libro con el autor indicado por la clave foránea."),
        new Example("Libros sin préstamo (NOT EXISTS)", """
            SELECT l.id_libro, l.titulo FROM libros l
            WHERE NOT EXISTS (SELECT 1 FROM prestamos p WHERE p.id_libro = l.id_libro)
            ORDER BY l.id_libro
            """, "NOT EXISTS selecciona los libros que no aparecen en ningún préstamo histórico."),
        new Example("Precio superior al promedio (subconsulta)", """
            SELECT id_libro, titulo, precio FROM libros
            WHERE precio > (SELECT AVG(precio) FROM libros)
            ORDER BY precio DESC, id_libro
            """, "La subconsulta obtiene un promedio que se utiliza en el filtro externo."),
        new Example("Usuarios sin préstamos activos (LEFT JOIN)", """
            SELECT u.id_usuario, u.nombre FROM usuarios u
            LEFT JOIN prestamos p ON p.id_usuario = u.id_usuario AND p.fecha_devolucion_real IS NULL
            WHERE p.id_prestamo IS NULL ORDER BY u.id_usuario
            """, "LEFT JOIN permite identificar usuarios sin una coincidencia activa.")
    );
    private final Connection connection;
    public LabDao(Connection connection) { this.connection = connection; }
    public int size() { return EXAMPLES.size(); }
    public List<String> titles() {
        List<String> titles = new ArrayList<>();
        for (Example example : EXAMPLES) titles.add(example.title());
        return titles;
    }
    public LabResult run(int number) throws SQLException {
        // Validamos que la opción corresponda a uno de los ejemplos disponibles.
        if (number < 1 || number > EXAMPLES.size()) throw new IllegalArgumentException("Consulta no disponible.");
        Example example = EXAMPLES.get(number - 1);
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        // Ejecutamos una consulta fija; el usuario no puede escribir SQL arbitrario.
        try (PreparedStatement statement = connection.prepareStatement(example.sql());
             ResultSet result = statement.executeQuery()) {
            ResultSetMetaData metadata = result.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) columns.add(metadata.getColumnLabel(i));
            while (result.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columns.size(); i++) row.add(String.valueOf(result.getObject(i)));
                rows.add(row);
            }
        }
        return new LabResult(example.title(), example.sql().strip(), example.explanation(), columns, rows);
    }
}
