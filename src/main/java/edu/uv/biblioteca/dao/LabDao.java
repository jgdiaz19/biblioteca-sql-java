package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.LabResult;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Reúne consultas de lectura para practicar A7, A8 y A9. */
public final class LabDao {
    private record Example(int activity, String title, String sql, String explanation) { }

    // Mantenemos el SQL junto con una explicación específica para cada ejercicio.
    private static final List<Example> EXAMPLES = List.of(
        new Example(7, "Texto: UPPER y LENGTH", """
                SELECT id_libro, UPPER(titulo) AS mayusculas, LENGTH(titulo) AS longitud
                FROM libros_consulta ORDER BY id_libro LIMIT 10
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
                SELECT categoria, COUNT(*) AS total_libros, ROUND(AVG(precio), 2) AS promedio
                FROM libros_consulta GROUP BY categoria ORDER BY total_libros DESC, categoria
                """, "COUNT y AVG resumen grupos construidos mediante GROUP BY."),
        new Example(7, "Filtrado con HAVING", """
                SELECT categoria, COUNT(*) AS total
                FROM libros_consulta GROUP BY categoria HAVING COUNT(*) >= 2 ORDER BY total DESC
                """, "HAVING selecciona grupos una vez calculada su cantidad."),
        new Example(7, "Serie de valores", """
                SELECT generate_series(1, 12) AS mes
                """, "generate_series produce filas que no dependen de una tabla."),
        new Example(7, "Resumen de precios", """
                SELECT COUNT(*) AS total, MIN(precio) AS minimo,
                MAX(precio) AS maximo, ROUND(AVG(precio),2) AS promedio,
                SUM(precio) AS suma
                FROM libros_consulta
                """, "Comparamos cinco funciones de agregación sobre el catálogo."),
        new Example(7, "COALESCE: editoriales no registradas", """
                SELECT id_libro, titulo,
                COALESCE(editorial, 'Sin editorial') AS editorial_visible
                FROM libros_consulta
                ORDER BY id_libro LIMIT 15
                """, "COALESCE sustituye valores NULL solo en el resultado. No modifica el catálogo de editoriales."),
        new Example(7, "CASE: estado de disponibilidad", """
                SELECT id_libro, titulo,
                CASE WHEN disponible THEN 'Disponible' ELSE 'Prestado' END AS estado
                FROM libros_consulta
                ORDER BY id_libro LIMIT 15
                """, "CASE asigna una etiqueta a cada libro mediante una condición. El valor booleano sigue almacenado en la tabla."),
        new Example(7, "ROUND: precios con incremento", """
                SELECT id_libro, titulo, precio,
                ROUND(precio * 1.10, 2) AS precio_simulado
                FROM libros_consulta
                ORDER BY id_libro LIMIT 15
                """, "Calculamos un aumento hipotético del 10 %. La consulta es solo de lectura y no actualiza precios."),
        new Example(7, "Conteo de disponibles por categoría", """
                SELECT categoria,
                COUNT(*) AS total,
                COUNT(*) FILTER (WHERE disponible) AS disponibles,
                COUNT(*) FILTER (WHERE NOT disponible) AS no_disponibles
                FROM libros_consulta
                GROUP BY categoria
                ORDER BY categoria
                """, "FILTER permite contar subconjuntos dentro de cada categoría. La disponibilidad pertenece a cada libro."),
        new Example(7, "Días entre préstamo y fecha límite", """
                SELECT id_prestamo, fecha_prestamo, fecha_devolucion,
                fecha_devolucion - fecha_prestamo AS plazo_dias
                FROM prestamos
                ORDER BY id_prestamo LIMIT 15
                """, "PostgreSQL resta valores DATE y devuelve días. La fecha de devolución aquí es la fecha límite, no la devolución real."),
        new Example(8, "INNER JOIN: libro y autor", """
                SELECT l.id_libro, l.titulo, a.nombre AS autor
                FROM libros_consulta l INNER JOIN autores a ON a.id_autor = l.id_autor
                ORDER BY l.id_libro LIMIT 12
                """, "Unimos filas cuya clave foránea corresponde a una clave primaria."),
        new Example(8, "JOIN: historial de préstamos", """
                SELECT p.id_prestamo, l.titulo, u.nombre AS usuario
                FROM prestamos p INNER JOIN libros_consulta l ON l.id_libro = p.id_libro
                INNER JOIN usuarios u ON u.id_usuario = p.id_usuario
                ORDER BY p.id_prestamo LIMIT 15
                """, "Relacionamos tres tablas para mostrar datos descriptivos."),
        new Example(8, "LEFT JOIN: autores sin libros", """
                SELECT a.id_autor, a.nombre
                FROM autores a LEFT JOIN libros_consulta l ON l.id_autor = a.id_autor
                WHERE l.id_libro IS NULL ORDER BY a.id_autor
                """, "Conservamos los autores y detectamos los que no tienen libros."),
        new Example(8, "LEFT JOIN: usuarios y préstamos", """
                SELECT u.id_usuario, u.nombre, COUNT(p.id_prestamo) AS prestamos
                FROM usuarios u LEFT JOIN prestamos p ON p.id_usuario = u.id_usuario
                GROUP BY u.id_usuario, u.nombre ORDER BY u.id_usuario
                """, "Contamos incluso usuarios que carecen de préstamos históricos."),
        new Example(8, "RIGHT JOIN: autores y libros", """
                SELECT a.nombre AS autor, l.titulo
                FROM libros_consulta l RIGHT JOIN autores a ON a.id_autor = l.id_autor
                ORDER BY a.nombre, l.titulo LIMIT 15
                """, "Conservamos todas las filas del lado derecho: autores."),
        new Example(8, "JOIN y agregación", """
                SELECT a.nombre, COUNT(l.id_libro) AS titulos
                FROM autores a LEFT JOIN libros_consulta l ON l.id_autor = a.id_autor
                GROUP BY a.id_autor, a.nombre ORDER BY titulos DESC, a.nombre
                """, "Agrupamos resultados después de relacionar tablas."),
        new Example(8, "JOIN: préstamos sin devolver", """
                SELECT p.id_prestamo, u.nombre, l.titulo, p.fecha_devolucion
                FROM prestamos p JOIN usuarios u ON u.id_usuario = p.id_usuario
                JOIN libros_consulta l ON l.id_libro = p.id_libro
                WHERE p.fecha_devolucion_real IS NULL ORDER BY p.id_prestamo
                """, "Filtramos una relación entre tres tablas por estado del préstamo."),
        new Example(8, "JOIN: libros y categorías normalizadas", """
                SELECT l.id_libro, l.titulo, c.nombre AS categoria
                FROM libros l
                INNER JOIN categorias c ON c.id_categoria = l.id_categoria
                ORDER BY l.id_libro LIMIT 15
                """, "La clave foránea id_categoria relaciona libros con categorias sin repetir el nombre en cada fila."),
        new Example(8, "LEFT JOIN: categorías sin libros", """
                SELECT c.id_categoria, c.nombre
                FROM categorias c
                LEFT JOIN libros l ON l.id_categoria = c.id_categoria
                WHERE l.id_libro IS NULL
                ORDER BY c.nombre
                """, "LEFT JOIN conserva las categorías aunque no tengan libros. El filtro NULL permite encontrarlas."),
        new Example(8, "LEFT JOIN: libros y editoriales opcionales", """
                SELECT l.titulo,
                COALESCE(e.nombre, 'Sin editorial') AS editorial
                FROM libros l
                LEFT JOIN editoriales e ON e.id_editorial = l.id_editorial
                ORDER BY l.titulo LIMIT 15
                """, "LEFT JOIN es necesario porque id_editorial admite NULL. INNER JOIN excluiría libros sin editorial."),
        new Example(8, "FULL JOIN: autores y libros", """
                SELECT a.nombre AS autor, l.titulo
                FROM autores a
                FULL OUTER JOIN libros l ON l.id_autor = a.id_autor
                ORDER BY a.nombre NULLS LAST, l.titulo NULLS LAST LIMIT 20
                """, "FULL OUTER JOIN muestra filas relacionadas y también las no emparejadas de ambos lados. La clave foránea hace improbable un libro huérfano."),
        new Example(8, "JOIN: préstamos vencidos", """
                SELECT u.nombre AS usuario, l.titulo,
                p.fecha_prestamo, p.fecha_devolucion AS fecha_limite
                FROM prestamos p
                JOIN usuarios u ON u.id_usuario = p.id_usuario
                JOIN libros l ON l.id_libro = p.id_libro
                WHERE p.fecha_devolucion_real IS NULL
                AND p.fecha_devolucion < CURRENT_DATE
                ORDER BY p.fecha_devolucion, u.nombre
                """, "Combinamos tres tablas. Solo están vencidos los préstamos no devueltos cuya fecha límite es anterior a hoy."),
        new Example(9, "Subconsulta escalar: precio medio", """
                SELECT id_libro, titulo, precio FROM libros_consulta
                WHERE precio > (SELECT AVG(precio) FROM libros_consulta)
                ORDER BY precio DESC, id_libro
                """, "La consulta interior produce el promedio usado por la exterior."),
        new Example(9, "IN: autores con libros", """
                SELECT id_autor, nombre FROM autores
                WHERE id_autor IN (SELECT id_autor FROM libros_consulta)
                ORDER BY id_autor
                """, "IN compara cada valor con el conjunto de la subconsulta."),
        new Example(9, "NOT EXISTS: libros sin historial", """
                SELECT l.id_libro, l.titulo FROM libros_consulta l
                WHERE NOT EXISTS (SELECT 1 FROM prestamos p WHERE p.id_libro=l.id_libro)
                ORDER BY l.id_libro
                """, "La subconsulta correlacionada consulta los préstamos de cada libro."),
        new Example(9, "EXISTS: usuarios con préstamos", """
                SELECT u.id_usuario, u.nombre FROM usuarios u
                WHERE EXISTS (SELECT 1 FROM prestamos p WHERE p.id_usuario=u.id_usuario)
                ORDER BY u.id_usuario
                """, "EXISTS comprueba la existencia de al menos una fila relacionada."),
        new Example(9, "ALL: libros más caros", """
                SELECT id_libro, titulo, precio FROM libros_consulta
                WHERE precio >= ALL (SELECT precio FROM libros_consulta)
                ORDER BY id_libro
                """, "ALL contrasta el precio con todos los elementos de una subconsulta."),
        new Example(9, "Subconsulta en FROM", """
                SELECT resumen.categoria, resumen.cantidad FROM (
                SELECT categoria, COUNT(*) AS cantidad FROM libros_consulta GROUP BY categoria
                ) AS resumen WHERE resumen.cantidad >= 2 ORDER BY resumen.cantidad DESC
                """, "La subconsulta genera una tabla derivada para filtrar posteriormente."),
        new Example(9, "Correlacionada: precio frente a categoría", """
                SELECT l.id_libro, l.titulo, l.categoria, l.precio FROM libros_consulta l
                WHERE l.precio > (SELECT AVG(l2.precio) FROM libros_consulta l2
                WHERE l2.categoria = l.categoria)
                ORDER BY l.categoria, l.precio DESC
                """, "Calculamos un promedio diferente para la categoría de cada libro."),
        new Example(9, "ANY: precio superior a algún libro", """
                SELECT l.id_libro, l.titulo, l.precio
                FROM libros_consulta l
                WHERE l.precio > ANY (
                SELECT l2.precio FROM libros_consulta l2
                WHERE l2.categoria = 'Bases de datos'
                )
                ORDER BY l.precio DESC, l.id_libro
                """, "ANY significa que basta superar al menos uno de los precios devueltos. Si la subconsulta está vacía, no hay coincidencias."),
        new Example(9, "NOT EXISTS: usuarios sin préstamos activos", """
                SELECT u.id_usuario, u.nombre
                FROM usuarios u
                WHERE NOT EXISTS (
                SELECT 1 FROM prestamos p
                WHERE p.id_usuario = u.id_usuario
                AND p.fecha_devolucion_real IS NULL
                )
                ORDER BY u.nombre
                """, "NOT EXISTS comprueba, para cada usuario, que no haya préstamos pendientes. Puede incluir usuarios con préstamos históricos devueltos."),
        new Example(9, "IN: libros de categorías con dos o más títulos", """
                SELECT l.id_libro, l.titulo, l.categoria
                FROM libros_consulta l
                WHERE l.categoria IN (
                SELECT categoria FROM libros_consulta
                GROUP BY categoria HAVING COUNT(*) >= 2
                )
                ORDER BY l.categoria, l.titulo
                """, "La subconsulta identifica las categorías que cumplen HAVING y la consulta exterior recupera sus libros."),
        new Example(9, "Subconsulta escalar: préstamos del usuario", """
                SELECT u.id_usuario, u.nombre,
                (SELECT COUNT(*) FROM prestamos p
                WHERE p.id_usuario = u.id_usuario) AS total_prestamos
                FROM usuarios u
                ORDER BY total_prestamos DESC, u.nombre
                """, "La subconsulta correlacionada devuelve un conteo por usuario, incluyendo cero cuando no existen préstamos."),
        new Example(9, "EXISTS: categorías con libros disponibles", """
                SELECT c.id_categoria, c.nombre
                FROM categorias c
                WHERE EXISTS (
                SELECT 1 FROM libros l
                WHERE l.id_categoria = c.id_categoria
                AND l.disponible = TRUE
                )
                ORDER BY c.nombre
                """, "EXISTS verifica que haya al menos un libro disponible de la categoría seleccionada.")
    );

    private final Connection connection;

    public LabDao(Connection connection) {
        this.connection = connection;
    }

    public int size() {
        return EXAMPLES.size();
    }

    // Devolvemos el nombre de los ejercicios del laboratorio indicado.
    public List<String> titles(int activity) {
        List<String> titles = new ArrayList<>();
        for (Example example : EXAMPLES) {
            if (example.activity() == activity) titles.add(example.title());
        }
        return titles;
    }

    public List<String> titles() {
        List<String> titles = new ArrayList<>();
        for (Example example : EXAMPLES) titles.add(example.title());
        return titles;
    }

    // Identificamos la consulta por su actividad y posición dentro del menú.
    public LabResult run(int activity, int number) throws SQLException {
        if (activity < 7 || activity > 9 || number < 1) {
            throw new IllegalArgumentException("Seleccione una consulta válida.");
        }
        int position = 0;
        for (Example example : EXAMPLES) {
            if (example.activity() != activity) continue;
            if (++position == number) return execute(example);
        }
        throw new IllegalArgumentException("La consulta seleccionada no existe.");
    }

    public LabResult run(int number) throws SQLException {
        if (number < 1 || number > EXAMPLES.size()) {
            throw new IllegalArgumentException("Consulta no disponible.");
        }
        return execute(EXAMPLES.get(number - 1));
    }

    private LabResult execute(Example example) throws SQLException {
        List<String> columns = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        // Ejecutamos únicamente consultas SELECT sin modificar los registros.
        try (PreparedStatement statement = SqlTrace.prepare(connection, example.sql());
             ResultSet result = statement.executeQuery()) {
            ResultSetMetaData metadata = result.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                columns.add(metadata.getColumnLabel(i));
            }

            // Convertimos cada fila en valores de texto para la tabla de Lanterna.
            while (result.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columns.size(); i++) {
                    Object value = result.getObject(i);
                    row.add(value == null ? "—" : value.toString());
                }
                rows.add(row);
            }
        }
        return new LabResult("A" + example.activity() + " · " + example.title(),
            example.sql().strip(), example.explanation(), columns, rows);
    }
}
