package edu.uv.biblioteca.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Resume los conceptos SQL reconocibles para orientar la lectura del código. */
public final class SqlExplanation {
    private SqlExplanation() { }

    // Cuando no hay explicación del laboratorio, describimos la sentencia ejecutada.
    public static String describe(String sql) {
        String upper = sql.toUpperCase(Locale.ROOT);
        List<String> points = new ArrayList<>();
        if (containsWord(upper, "SELECT")) points.add("SELECT: indique cuáles columnas se solicitan y qué alias se usan. Observe también las expresiones calculadas: cada expresión genera una columna del resultado.");
        if (containsWord(upper, "INSERT")) points.add("INSERT: identifique la tabla destino y las columnas que reciben valores. Verifique cómo Java define esos valores antes de ejecutar la sentencia.");
        if (containsWord(upper, "UPDATE")) points.add("UPDATE: observe las columnas asignadas en SET y la condición que determina qué registros cambian. Sin un filtro adecuado podrían modificarse más filas de las esperadas.");
        if (containsWord(upper, "DELETE")) points.add("DELETE: localice el filtro y compruebe que identifica únicamente los registros que se pretenden eliminar.");
        if (containsWord(upper, "JOIN")) points.add("JOIN: localice cada tabla, su alias y la condición ON que relaciona sus claves. Revise si la unión conserva filas sin coincidencia (LEFT JOIN) o solamente las relacionadas (INNER JOIN).");
        if (containsWord(upper, "WHERE")) points.add("WHERE: la condición se evalúa sobre las filas antes del agrupamiento. Si contiene ?, el valor se proporciona desde Java a través de PreparedStatement.");
        if (containsWord(upper, "GROUP BY")) points.add("GROUP BY: agrupa registros que comparten valores. Después examine las funciones COUNT, SUM o AVG que calculan un valor por grupo.");
        if (containsWord(upper, "HAVING")) points.add("HAVING: aplica condiciones a los grupos ya formados. Compárelo con WHERE, que filtra filas individuales antes de agrupar.");
        if (containsWord(upper, "ORDER BY")) points.add("ORDER BY: identifica las columnas de ordenamiento y si se utiliza ASC o DESC. Este orden ayuda a mostrar resultados reproducibles en la tabla de la consola.");
        if (containsWord(upper, "EXISTS")) points.add("EXISTS: comprueba si la subconsulta encuentra al menos un registro. Estudie si usa columnas de la consulta exterior, porque entonces es una subconsulta correlacionada.");
        if (containsWord(upper, "ILIKE")) points.add("ILIKE: compara cadenas sin distinguir mayúsculas. Los porcentajes del patrón permiten encontrar coincidencias dentro del texto, como sucede en la búsqueda incremental.");
        if (upper.contains("?")) points.add("MARCADOR ?: JDBC conserva la estructura SQL y asigna los valores por separado. En la vista Java identifique la llamada setString, setInt u otro setX y su posición numérica.");
        if (points.isEmpty()) points.add("Esta sentencia se envía a PostgreSQL desde la capa JDBC.");
        return "LECTURA GUIADA DE LA SENTENCIA\n\n" + String.join("\n\n", points)
            + "\n\nRECORRIDO DE LA OPERACIÓN\n"
            + "1. Busque esta sentencia en el DAO o servicio indicado en la vista Java.\n"
            + "2. Observe los filtros, las uniones y el orden de los resultados.\n"
            + "3. Compare las columnas seleccionadas con las que Java extrae del ResultSet.\n"
            + "4. Vuelva a la tabla de BibliotecaSQL y compruebe cómo se presenta la información.";
    }

    private static boolean containsWord(String sql, String word) {
        return java.util.regex.Pattern.compile("(?<![A-Z_])" + java.util.regex.Pattern.quote(word) + "(?![A-Z_])")
            .matcher(sql).find();
    }
}
