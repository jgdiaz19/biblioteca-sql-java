package edu.uv.biblioteca.ui;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ClassicTuiFilterTest {
    @Test void encuentraCoincidenciasEnCualquierColumna() {
        var rows = List.of(List.of("1", "SQL avanzado", "Disponible"),
                           List.of("2", "Algoritmos", "Prestado"));
        // Validamos que el filtro no dependa de mayúsculas y minúsculas.
        assertEquals(1, ClassicTui.filterRows(rows, "sql").size());
        assertEquals(1, ClassicTui.filterRows(rows, "PRESTADO").size());
        assertEquals(2, ClassicTui.filterRows(rows, "").size());
        assertEquals(0, ClassicTui.filterRows(rows, "javascript").size());
    }
}
