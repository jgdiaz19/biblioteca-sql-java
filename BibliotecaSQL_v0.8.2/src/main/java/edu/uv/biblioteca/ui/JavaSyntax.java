package edu.uv.biblioteca.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Separa los elementos visibles de Java para aplicar colores en la consola. */
public final class JavaSyntax {
    public enum Kind { KEYWORD, TYPE, STRING, NUMBER, COMMENT, NORMAL }
    public record Part(String text, Kind kind) { }
    private static final Set<String> KEYWORDS = Set.of(
        "public", "private", "protected", "static", "final", "void", "return", "new", "try", "catch",
        "finally", "throws", "throw", "if", "else", "while", "for", "switch", "case", "default",
        "break", "continue", "class", "interface", "record", "package", "import", "extends",
        "implements", "this", "super", "true", "false", "null", "instanceof", "var");
    private static final Set<String> TYPES = Set.of(
        "String", "int", "long", "boolean", "double", "float", "char", "List", "Map", "Set",
        "Connection", "PreparedStatement", "ResultSet", "SQLException", "Book", "Loan", "ArrayList");
    private JavaSyntax() { }

    // Distinguimos literales, comentarios y palabras sin cambiar el código original.
    public static List<Part> tokenize(String line) {
        List<Part> result = new ArrayList<>();
        int i = 0;
        while (i < line.length()) {
            int start = i;
            char ch = line.charAt(i);
            Kind kind = Kind.NORMAL;
            if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
                i = line.length();
                kind = Kind.COMMENT;
            } else if (ch == '\"' || ch == '\'') {
                char quote = ch;
                i++;
                while (i < line.length()) {
                    if (line.charAt(i) == '\\') { i = Math.min(line.length(), i + 2); continue; }
                    if (line.charAt(i++) == quote) break;
                }
                kind = Kind.STRING;
            } else if (Character.isDigit(ch)) {
                i++;
                while (i < line.length() && (Character.isDigit(line.charAt(i)) || line.charAt(i) == '.')) i++;
                kind = Kind.NUMBER;
            } else if (Character.isJavaIdentifierStart(ch)) {
                i++;
                while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i))) i++;
                String word = line.substring(start, i);
                kind = KEYWORDS.contains(word) ? Kind.KEYWORD : TYPES.contains(word) ? Kind.TYPE : Kind.NORMAL;
            } else i++;
            result.add(new Part(line.substring(start, i), kind));
        }
        return result;
    }
}
