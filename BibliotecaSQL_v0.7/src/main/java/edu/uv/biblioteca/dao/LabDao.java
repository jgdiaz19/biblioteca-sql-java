package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.LabResult;
import java.sql.*;
import java.util.*;

// Reunimos las consultas del segundo parcial para ejecutarlas sin modificar datos.
public final class LabDao {
    private record Example(int activity, String title, String sql, String explanation) { }
    private static final List<Example> EXAMPLES = List.of(
        new Example(7, "Texto: UPPER y LENGTH", """
            SELECT id_libro, UPPER(titulo) AS mayusculas, LENGTH(titulo) AS longitud
            FROM libros ORDER BY id_libro LIMIT 10
            """, "Transformamos títulos y calculamos su longitud sin cambiar los registros."),
        new Example(7, "Texto: TRIM y CONCAT", """
            SELECT id_autor, TRIM(nombre) AS nombre_limpio,
                   CONCAT('Autor: ', nombre) AS etiqueta
            FROM autores ORDER BY id_autor LIMIT 10
            """, "TRIM elimina espacios exteriores y CONCAT construye un texto."),
        new Example(7, "Fechas: EXTRACT", """
            SELECT id_prestamo, fecha_prestamo,
                   EXTRACT(YEAR FROM fecha_prestamo) AS anio,
                   EXTRACT(MONTH FROM fecha_prestamo) AS mes
            FROM prestamos ORDER BY id_prestamo LIMIT 12
            """, "EXTRACT obtiene partes de una fecha."),
        new Example(7, "Agregación por categoría", """
            SELECT categoria, COUNT(*) AS libros, ROUND(AVG(precio), 2) AS promedio
            FROM libros GROUP BY categoria ORDER BY libros DESC, categoria
            """, "COUNT y AVG resumen grupos construidos mediante GROUP BY."),
        new Example(7, "Filtrado con HAVING", """
            SELECT categoria, COUNT(*) AS total
            FROM libros GROUP BY categoria HAVING COUNT(*) >= 2 ORDER BY total DESC
            """, "HAVING selecciona grupos una vez calculada su cantidad."),
        new Example(7, "Serie de valores", """
            SELECT generate_series(1, 12) AS mes
            """, "generate_series produce filas que no dependen de una tabla."),
        new Example(7, "Resumen de precios", """
            SELECT COUNT(*) AS total, MIN(precio) AS minimo,
                   MAX(precio) AS maximo, ROUND(AVG(precio),2) AS promedio,
                   SUM(precio) AS suma
            FROM libros
            """, "Comparamos cinco funciones de agregación sobre el catálogo."),
        new Example(8, "INNER JOIN: libro y autor", """
            SELECT l.id_libro, l.titulo, a.nombre AS autor
            FROM libros l INNER JOIN autores a ON a.id_autor = l.id_autor
            ORDER BY l.id_libro LIMIT 12
            """, "Unimos filas cuya clave foránea corresponde a una clave primaria."),
        new Example(8, "JOIN: historial de préstamos", """
            SELECT p.id_prestamo, l.titulo, u.nombre AS usuario
            FROM prestamos p INNER JOIN libros l ON l.id_libro = p.id_libro
            INNER JOIN usuarios u ON u.id_usuario = p.id_usuario
            ORDER BY p.id_prestamo LIMIT 15
            """, "Relacionamos tres tablas para mostrar datos descriptivos."),
        new Example(8, "LEFT JOIN: autores sin libros", """
            SELECT a.id_autor, a.nombre
            FROM autores a LEFT JOIN libros l ON l.id_autor = a.id_autor
            WHERE l.id_libro IS NULL ORDER BY a.id_autor
            """, "Conservamos los autores y detectamos los que no tienen libros."),
        new Example(8, "LEFT JOIN: usuarios y préstamos", """
            SELECT u.id_usuario, u.nombre, COUNT(p.id_prestamo) AS prestamos
            FROM usuarios u LEFT JOIN prestamos p ON p.id_usuario = u.id_usuario
            GROUP BY u.id_usuario, u.nombre ORDER BY u.id_usuario
            """, "Contamos incluso usuarios que carecen de préstamos históricos."),
        new Example(8, "RIGHT JOIN: autores y libros", """
            SELECT a.nombre AS autor, l.titulo
            FROM libros l RIGHT JOIN autores a ON a.id_autor = l.id_autor
            ORDER BY a.nombre, l.titulo LIMIT 15
            """, "Conservamos todas las filas del lado derecho: autores."),
        new Example(8, "JOIN y agregación", """
            SELECT a.nombre, COUNT(l.id_libro) AS titulos
            FROM autores a LEFT JOIN libros l ON l.id_autor = a.id_autor
            GROUP BY a.id_autor, a.nombre ORDER BY titulos DESC, a.nombre
            """, "Agrupamos resultados después de relacionar tablas."),
        new Example(8, "JOIN: préstamos sin devolver", """
            SELECT p.id_prestamo, u.nombre, l.titulo, p.fecha_devolucion
            FROM prestamos p JOIN usuarios u ON u.id_usuario = p.id_usuario
            JOIN libros l ON l.id_libro = p.id_libro
            WHERE p.fecha_devolucion_real IS NULL ORDER BY p.id_prestamo
            """, "Filtramos una relación entre tres tablas por estado del préstamo."),
        new Example(9, "Subconsulta escalar: precio medio", """
            SELECT id_libro, titulo, precio FROM libros
            WHERE precio > (SELECT AVG(precio) FROM libros)
            ORDER BY precio DESC, id_libro
            """, "La consulta interior produce el promedio usado por la exterior."),
        new Example(9, "IN: autores con libros", """
            SELECT id_autor, nombre FROM autores
            WHERE id_autor IN (SELECT id_autor FROM libros)
            ORDER BY id_autor
            """, "IN compara cada valor con el conjunto de la subconsulta."),
        new Example(9, "NOT EXISTS: libros sin historial", """
            SELECT l.id_libro, l.titulo FROM libros l
            WHERE NOT EXISTS (SELECT 1 FROM prestamos p WHERE p.id_libro=l.id_libro)
            ORDER BY l.id_libro
            """, "La subconsulta correlacionada consulta los préstamos de cada libro."),
        new Example(9, "EXISTS: usuarios con préstamos", """
            SELECT u.id_usuario, u.nombre FROM usuarios u
            WHERE EXISTS (SELECT 1 FROM prestamos p WHERE p.id_usuario=u.id_usuario)
            ORDER BY u.id_usuario
            """, "EXISTS comprueba la existencia de al menos una fila relacionada."),
        new Example(9, "ALL: libros más caros", """
            SELECT id_libro, titulo, precio FROM libros
            WHERE precio >= ALL (SELECT precio FROM libros)
            ORDER BY id_libro
            """, "ALL contrasta el precio con todos los elementos de una subconsulta."),
        new Example(9, "Subconsulta en FROM", """
            SELECT resumen.categoria, resumen.cantidad FROM (
                SELECT categoria, COUNT(*) AS cantidad FROM libros GROUP BY categoria
            ) AS resumen WHERE resumen.cantidad >= 2 ORDER BY resumen.cantidad DESC
            """, "La subconsulta genera una tabla derivada para filtrar posteriormente."),
        new Example(9, "Correlacionada: precio frente a categoría", """
            SELECT l.id_libro, l.titulo, l.categoria, l.precio FROM libros l
            WHERE l.precio > (SELECT AVG(l2.precio) FROM libros l2
                              WHERE l2.categoria = l.categoria)
            ORDER BY l.categoria, l.precio DESC
            """, "Calculamos un promedio diferente para la categoría de cada libro.")
    );
    private final Connection connection;
    public LabDao(Connection connection) { this.connection = connection; }
    public int size() { return EXAMPLES.size(); }
    public List<String> titles() {
        List<String> titles = new ArrayList<>();
        for (Example example : EXAMPLES) titles.add(example.title());
        return titles;
    }
    public List<String> titles(int activity) {
        // Filtramos el catálogo para que cada actividad tenga su propio menú.
        List<String> titles = new ArrayList<>();
        for (Example example : EXAMPLES) if (example.activity() == activity) titles.add(example.title());
        return titles;
    }
    public LabResult run(int number) throws SQLException { return execute(number); }
    public LabResult run(int activity, int number) throws SQLException {
        if (activity < 7 || activity > 9 || number < 1 || number > 7)
            throw new IllegalArgumentException("Seleccione una consulta válida.");
        return execute((activity - 7) * 7 + number);
    }
    private LabResult execute(int number) throws SQLException {
        if (number < 1 || number > EXAMPLES.size()) throw new IllegalArgumentException("Consulta no disponible.");
        Example example = EXAMPLES.get(number - 1);
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();
        // Ejecutamos consultas predefinidas de solo lectura contra PostgreSQL.
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
        return new LabResult("A" + example.activity() + " · " + example.title(),
            example.sql().strip(), example.explanation(), columns, rows);
    }
}
