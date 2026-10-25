package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.dao.SqlTrace;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Permite consultar el código Java que ejecutó una sentencia JDBC. */
public final class JavaSourceView {
    private JavaSourceView() { }

    public record Source(String location, List<String> lines, boolean available) { }

    public static Source forEntry(SqlTrace.Entry entry) {
        String qualifiedName = entry.sourceClass();
        String relative = qualifiedName.replace('.', '/').replaceAll("\\$.*", "") + ".java";
        Path base = Path.of("src", "main", "java").toAbsolutePath().normalize();
        Path file = base.resolve(relative).normalize();

        // Validamos que el archivo se encuentre dentro del proyecto.
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return new Source(relative, List.of(
                "No se encontró el código fuente en src/main/java.",
                "Ejecute Maven desde la carpeta principal del repositorio."), false);
        }
        try {
            List<String> all = Files.readAllLines(file, StandardCharsets.UTF_8);
            int center = Math.max(0, Math.min(all.size() - 1, entry.sourceLine() - 1));
            int first = Math.max(0, center - 13);
            int last = Math.min(all.size(), center + 25);
            List<String> excerpt = new ArrayList<>();
            // Conservamos los números originales para ubicar el fragmento en VS Code.
            for (int i = first; i < last; i++) {
                excerpt.add(String.format("%4d  %s", i + 1, all.get(i)));
            }
            return new Source(relative + " · " + entry.sourceMethod() + "()", excerpt, true);
        } catch (IOException e) {
            return new Source(relative, List.of("No fue posible leer el archivo Java."), false);
        }
    }
}
