package edu.uv.biblioteca.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Identifica elementos del SQL para pintarlos en la terminal. */
public final class SqlSyntax {
    private static final Set<String> KEYWORDS = Set.of(
        "SELECT", "FROM", "WHERE", "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "OUTER",
        "ON", "AS", "AND", "OR", "NOT", "IN", "EXISTS", "NULL", "IS", "TRUE", "FALSE",
        "GROUP", "BY", "HAVING", "ORDER", "ASC", "DESC", "LIMIT", "OFFSET", "DISTINCT",
        "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "RETURNING", "WITH",
        "UNION", "ALL", "ANY", "CASE", "WHEN", "THEN", "ELSE", "END", "BETWEEN",
        "LIKE", "ILIKE", "OVER", "PARTITION", "FOR", "SHARE", "OF", "DATE", "INTERVAL",
        "CREATE", "TABLE", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "BEGIN", "COMMIT",
        "ROLLBACK", "CONFLICT", "DO", "NOTHING", "CAST", "COALESCE");
    private static final Set<String> FUNCTIONS = Set.of(
        "COUNT", "SUM", "AVG", "MIN", "MAX", "LOWER", "UPPER", "LENGTH", "TRIM",
        "CONCAT", "ROUND", "EXTRACT", "TO_CHAR", "GENERATE_SERIES", "NOW", "CURRENT_DATE",
        "NULLIF", "ABS", "SUBSTRING");

    public enum Kind { KEYWORD, FUNCTION, STRING, NUMBER, COMMENT, IDENTIFIER, SYMBOL }
    public record Part(String text, Kind kind) { }
    private SqlSyntax() { }

    // Recorremos la línea para separar las palabras, números y literales.
    public static List<Part> tokenize(String line) {
        List<Part> parts = new ArrayList<>();
        int index = 0;
        while (index < line.length()) {
            char current = line.charAt(index);
            int start = index;
            Kind kind;
            if (current == '-' && index + 1 < line.length() && line.charAt(index + 1) == '-') {
                index = line.length();
                kind = Kind.COMMENT;
            } else if (current == '\'' || current == '"') {
                char quote = current;
                index++;
                while (index < line.length()) {
                    if (line.charAt(index++) == quote) {
                        if (index < line.length() && line.charAt(index) == quote) index++;
                        else break;
                    }
                }
                kind = current == '\'' ? Kind.STRING : Kind.IDENTIFIER;
            } else if (Character.isDigit(current)) {
                index++;
                while (index < line.length() && (Character.isDigit(line.charAt(index))
                    || line.charAt(index) == '.')) index++;
                kind = Kind.NUMBER;
            } else if (Character.isLetter(current) || current == '_') {
                index++;
                while (index < line.length() && (Character.isLetterOrDigit(line.charAt(index))
                    || line.charAt(index) == '_')) index++;
                String word = line.substring(start, index).toUpperCase(Locale.ROOT);
                kind = KEYWORDS.contains(word) ? Kind.KEYWORD
                    : FUNCTIONS.contains(word) ? Kind.FUNCTION : Kind.IDENTIFIER;
            } else {
                index++;
                kind = Kind.SYMBOL;
            }
            parts.add(new Part(line.substring(start, index), kind));
        }
        return parts;
    }
}
