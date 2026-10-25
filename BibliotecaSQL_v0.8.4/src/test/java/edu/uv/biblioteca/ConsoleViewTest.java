package edu.uv.biblioteca;

import edu.uv.biblioteca.ui.ConsoleView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica que la consola muestre tablas y reciba entradas correctamente. */
public class ConsoleViewTest {
    @Test
    void trimLongTexts() {
        // Validamos que el texto no exceda el ancho de una columna.
        assertEquals("ABCD…", ConsoleView.fit("ABCDEFGHIJ", 5));
        assertEquals("", ConsoleView.fit(null, 5));
        assertEquals("a b", ConsoleView.fit("a\nb", 5));
    }

    @Test
    void printTableWithBordersAndRows() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ConsoleView view = new ConsoleView(new ByteArrayInputStream(new byte[0]),
                new PrintStream(output, true, StandardCharsets.UTF_8))) {
            // Dibujamos una tabla pequeña para revisar sus límites y la cantidad de filas.
            view.table(List.of("ID", "LIBRO"), List.of(List.of("1", "SQL")), 4, 10);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("| ID"));
        assertTrue(text.contains("SQL"));
        assertTrue(text.contains("Registros: 1"));
    }

    @Test
    void readTrimmedInput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ConsoleView view = new ConsoleView(new ByteArrayInputStream("  42  \n".getBytes(StandardCharsets.UTF_8)),
                new PrintStream(output, true, StandardCharsets.UTF_8))) {
            // Comprobamos que la captura elimine espacios innecesarios.
            assertEquals("42", view.ask("ID: "));
        }
    }
}
