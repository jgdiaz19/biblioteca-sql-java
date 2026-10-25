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
        if (containsWord(upper, "SELECT")) points.add("SELECT: define las columnas o expresiones que devuelve la consulta.");
        if (containsWord(upper, "INSERT")) points.add("INSERT: agrega registros a una tabla.");
        if (containsWord(upper, "UPDATE")) points.add("UPDATE: modifica registros existentes.");
        if (containsWord(upper, "DELETE")) points.add("DELETE: elimina registros según la condición establecida.");
        if (containsWord(upper, "JOIN")) points.add("JOIN: combina filas de tablas relacionadas mediante ON.");
        if (containsWord(upper, "WHERE")) points.add("WHERE: filtra las filas que cumplen la condición.");
        if (containsWord(upper, "GROUP BY")) points.add("GROUP BY: reúne filas por valores comunes.");
        if (containsWord(upper, "HAVING")) points.add("HAVING: filtra grupos después de la agregación.");
        if (containsWord(upper, "ORDER BY")) points.add("ORDER BY: ordena los resultados de la consulta.");
        if (containsWord(upper, "EXISTS")) points.add("EXISTS: evalúa si una subconsulta devuelve al menos una fila.");
        if (containsWord(upper, "ILIKE")) points.add("ILIKE: compara patrones de texto sin distinguir mayúsculas de minúsculas.");
        if (upper.contains("?")) points.add("?: marcador que recibe su valor mediante PreparedStatement.");
        if (points.isEmpty()) points.add("Esta sentencia se envía a PostgreSQL desde la capa JDBC.");
        return String.join("\n\n", points);
    }

    private static boolean containsWord(String sql, String word) {
        return java.util.regex.Pattern.compile("(?<![A-Z_])" + java.util.regex.Pattern.quote(word) + "(?![A-Z_])")
            .matcher(sql).find();
    }
}
