package edu.uv.biblioteca.ui;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import edu.uv.biblioteca.dao.SqlTrace;
import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.LabResult;
import edu.uv.biblioteca.service.LibraryService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Dibujamos los menús y las tablas como en los programas de consola clásicos.
// La interfaz procesa teclas y muestra resultados; las reglas de negocio viven en servicios.
public final class ClassicTui implements AutoCloseable {
    private static final TextColor BLUE = new TextColor.RGB(0, 0, 170);
    private static final TextColor WHITE = new TextColor.RGB(244, 244, 244);
    private static final TextColor YELLOW = new TextColor.RGB(255, 255, 110);
    private static final TextColor CYAN = new TextColor.RGB(115, 235, 235);
    private static final TextColor GREY = new TextColor.RGB(204, 204, 204);
    private static final TextColor BLACK = new TextColor.RGB(5, 5, 55);
    private final Screen screen;
    private final LibraryService service;
    private final TextGraphics graphics;
    private String notice = "";
    private boolean teaching;
    private String sqlExplanation = "";
    private int width;
    private int height;

    public ClassicTui(LibraryService service) throws IOException {
        this.service = service;
        // Inicializamos la terminal para leer teclas sin esperar ENTER.
        screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal());
        screen.startScreen();
        screen.setCursorPosition(null);
        graphics = screen.newTextGraphics();
    }

    public void run() throws IOException {
        splash();
        String[] main = {"Catálogo de libros", "Préstamos activos", "Estadísticas por categoría",
            "Registrar préstamo", "Registrar devolución", "Préstamos vencidos",
            "Préstamos por usuario", "Laboratorios SQL", "Modo didáctico"};
        while (true) {
            int selected = menu("MENÚ PRINCIPAL", main, "0 Salir   1-9 Elegir   ↑↓ Mover   ENTER Abrir");
            if (selected == 0) return;
            try {
                SqlTrace.clear();
                sqlExplanation = "";
                switch (selected) {
                    case 1 -> tableBooks();
                    case 2 -> tableLoans();
                    case 3 -> tableStats();
                    case 4 -> borrow();
                    case 5 -> giveBack();
                    case 6 -> tableOverdue();
                    case 7 -> tableUsers();
                    case 8 -> laboratory();
                    case 9 -> { teaching = !teaching; notice = "Modo didáctico: " + (teaching ? "activado" : "desactivado"); }
                    default -> { }
                }
            } catch (SQLException ex) {
                message("Error en PostgreSQL", "SQLState: " + ex.getSQLState());
            } catch (IllegalArgumentException ex) {
                message("Dato incorrecto", ex.getMessage());
            }
        }
    }

    private void splash() throws IOException {
        while (true) {
            frame("UNIVERSIDAD VERACRUZANA", "Pulse una tecla para continuar");
            center(height / 2 - 6, "██╗   ██╗██╗   ██╗", YELLOW);
            center(height / 2 - 5, "██║   ██║██║   ██║", YELLOW);
            center(height / 2 - 4, "██║   ██║██║   ██║", YELLOW);
            center(height / 2 - 3, "╚██╗ ██╔╝╚██╗ ██╔╝", YELLOW);
            center(height / 2 - 2, " ╚████╔╝  ╚████╔╝ ", YELLOW);
            center(height / 2 - 1, "  ╚═══╝    ╚═══╝  ", YELLOW);
            center(height / 2 + 1, "BIBLIOTECASQL  |  v0.8", WHITE);
            center(height / 2 + 3, "Ingeniería de Software · Bases de Datos", CYAN);
            center(height / 2 + 5, "Monograma tipográfico, no escudo oficial", GREY);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key != null) return;
        }
    }

    private int menu(String title, String[] options, String help) throws IOException {
        int selected = 0;
        while (true) {
            frame(title, help);
            int top = Math.max(4, (height - options.length * 2) / 2);
            for (int i = 0; i < options.length && top + i * 2 < height - 3; i++) {
                String label = " " + (i + 1) + "  " + options[i];
                line(6, top + i * 2, (i == selected ? "▶" : " ") + label,
                    i == selected ? YELLOW : WHITE);
            }
            if (!notice.isEmpty()) line(4, height - 3, notice, CYAN);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) return 0;
            if (key.getKeyType() == KeyType.ArrowUp) selected = (selected + options.length - 1) % options.length;
            if (key.getKeyType() == KeyType.ArrowDown) selected = (selected + 1) % options.length;
            if (key.getKeyType() == KeyType.Enter) return selected + 1;
            if (key.getKeyType() == KeyType.Character) {
                char c = key.getCharacter();
                if (c == '0') return 0;
                if (c >= '1' && c <= '9' && c - '1' < options.length) return c - '0';
            }
        }
    }

    private void tableBooks() throws IOException, SQLException {
        // Cargamos el catálogo una sola vez para habilitar el filtrado en Java.
        List<Book> complete = service.listBooks();
        List<Book> filtered = complete;
        String search = "";
        boolean searching = false;
        boolean sqlMode = false;
        int selected = 0;
        int offset = 0;
        String error = "";
        while (true) {
            frame("CATÁLOGO DE LIBROS", teaching
                ? "1 Buscar   2 Java/SQL   3 Ver SQL   ↑↓ Mover   0 Volver"
                : "1 Buscar   2 Java/SQL   ↑↓ Mover   ENTER Detalle   0 Volver");
            line(3, 3, "Filtro: " + (searching ? "[" + search + "_]" : search.isEmpty() ? "(sin filtro)" : search), YELLOW);
            line(3, 4, "Modo: " + (sqlMode ? "PostgreSQL (ILIKE)" : "Java (memoria)") +
                "     Coincidencias: " + filtered.size() + "/" + complete.size(), CYAN);
            int visible = Math.max(1, height - 11);
            if (selected < offset) offset = selected;
            if (selected >= offset + visible) offset = selected - visible + 1;
            line(3, 6, fit(String.format("%-5s %-" + Math.max(8, width - 37) + "s %-12s %s", "ID", "TÍTULO", "CATEGORÍA", "ESTADO"), width - 6), YELLOW);
            horizontal(3, 7, width - 6);
            for (int i = offset; i < filtered.size() && i < offset + visible; i++) {
                Book b = filtered.get(i);
                String row = String.format("%-5d %-" + Math.max(8, width - 37) + "s %-12s %s",
                    b.id(), fit(b.title(), Math.max(8, width - 37)), fit(b.category(), 12), b.available() ? "SÍ" : "NO");
                line(3, 8 + i - offset, (i == selected ? ">" : " ") + fit(row, width - 7),
                    i == selected ? YELLOW : WHITE);
            }
            if (!error.isEmpty()) line(3, height - 3, error, YELLOW);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape) {
                if (searching) { searching = false; continue; }
                return;
            }
            if (searching) {
                if (type == KeyType.Enter) { searching = false; continue; }
                if (type == KeyType.Backspace && !search.isEmpty()) search = search.substring(0, search.length() - 1);
                else if (type == KeyType.Character && !Character.isISOControl(ch) && search.length() < 80) search += ch;
                else continue;
            } else if (ch == '0') return;
            else if (ch == '1' || ch == '/') { searching = true; continue; }
            else if (ch == '2') {
                sqlMode = !sqlMode;
                // Regresamos al catálogo completo para aplicar el modo elegido.
                filtered = complete;
                search = "";
                selected = 0;
                offset = 0;
                continue;
            }
            else if (ch == '3' && teaching) { sqlWindow("BÚSQUEDA DE LIBROS"); continue; }
            else if (type == KeyType.ArrowUp) { selected = Math.max(0, selected - 1); continue; }
            else if (type == KeyType.ArrowDown) { selected = Math.min(filtered.size() - 1, selected + 1); continue; }
            else if (type == KeyType.PageUp) { selected = Math.max(0, selected - visible); continue; }
            else if (type == KeyType.PageDown) { selected = Math.min(filtered.size() - 1, selected + visible); continue; }
            else if (type == KeyType.Enter && !filtered.isEmpty()) {
                Book b = filtered.get(selected);
                message("DETALLE DEL LIBRO", "ID: " + b.id() + " | " + b.title() + " | " + b.category()
                    + " | " + (b.available() ? "Disponible" : "Prestado"));
                continue;
            } else continue;
            error = "";
            // Actualizamos la lista según el modo de búsqueda elegido.
            if (search.isBlank()) filtered = complete;
            else if (sqlMode) {
                try { filtered = service.searchBooks(search); }
                catch (SQLException ex) { error = "No se pudo buscar: " + ex.getSQLState(); }
            } else {
                String term = search.toLowerCase(Locale.ROOT);
                filtered = complete.stream().filter(b -> (b.title() + " " + b.category() + " " + b.id())
                    .toLowerCase(Locale.ROOT).contains(term)).toList();
            }
            selected = 0;
            offset = 0;
        }
    }

    private void tableLoans() throws IOException, SQLException {
        var loans = service.activeLoans();
        List<List<String>> rows = new ArrayList<>();
        loans.forEach(x -> rows.add(List.of(String.valueOf(x.id()), x.user(), x.title(), String.valueOf(x.dueDate()))));
        table("PRÉSTAMOS ACTIVOS", List.of("ID", "USUARIO", "LIBRO", "LÍMITE"), rows);
    }
    private void tableStats() throws IOException, SQLException {
        List<List<String>> rows = new ArrayList<>();
        service.statistics().forEach(x -> rows.add(List.of(x.category(), String.valueOf(x.total()), String.valueOf(x.available()))));
        table("ESTADÍSTICAS", List.of("CATEGORÍA", "TOTAL", "DISPONIBLES"), rows);
    }
    private void tableOverdue() throws IOException, SQLException {
        List<List<String>> rows = new ArrayList<>();
        service.overdue().forEach(x -> rows.add(List.of(String.valueOf(x.id()), x.user(), x.title(), String.valueOf(x.dueDate()), String.valueOf(x.daysLate()))));
        table("PRÉSTAMOS VENCIDOS", List.of("ID", "USUARIO", "LIBRO", "FECHA", "DÍAS"), rows);
    }
    private void tableUsers() throws IOException, SQLException {
        List<List<String>> rows = new ArrayList<>();
        service.userCounts().forEach(x -> rows.add(List.of(x.user(), String.valueOf(x.activeLoans()))));
        table("PRÉSTAMOS POR USUARIO", List.of("USUARIO", "ACTIVOS"), rows);
    }

    private void laboratory() throws IOException, SQLException {
        while (true) {
            int activity = menu("LABORATORIOS SQL", new String[]{"A7 · Funciones y agrupación", "A8 · JOIN", "A9 · Subconsultas"},
                "1-3 Elegir laboratorio   0 Volver");
            if (activity == 0) return;
            int a = activity + 6;
            while (true) {
                var titles = service.labTitles(a);
                int choice = menu("LABORATORIO A" + a, titles.toArray(String[]::new), "1-7 Elegir consulta   0 Volver");
                if (choice == 0) break;
                LabResult result = service.labRun(a, choice);
                List<List<String>> rows = new ArrayList<>();
                rows.addAll(result.rows());
                sqlExplanation = result.explanation();
                table("A" + a + " · " + result.title(), result.columns(), rows);

            }
        }
    }

    private void borrow() throws IOException, SQLException {
        String user = input("REGISTRAR PRÉSTAMO", "ID del usuario (0 cancela): ");
        if (user == null || user.equals("0")) return;
        String book = input("REGISTRAR PRÉSTAMO", "ID del libro (0 cancela): ");
        if (book == null || book.equals("0")) return;
        int id = service.borrow(parseId(user), parseId(book));
        message("OPERACIÓN COMPLETADA", "Préstamo registrado con ID: " + id);
    }
    private void giveBack() throws IOException, SQLException {
        String loan = input("REGISTRAR DEVOLUCIÓN", "ID del préstamo (0 cancela): ");
        if (loan == null || loan.equals("0")) return;
        service.returnBook(parseId(loan));
        message("OPERACIÓN COMPLETADA", "La devolución quedó registrada.");
    }
    private int parseId(String text) {
        try {
            int id = Integer.parseInt(text.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Escriba un identificador positivo."); }
    }

    private void table(String title, List<String> columns, List<List<String>> rows) throws IOException {
        // Filtramos también los reportes sin modificar sus consultas originales.
        String search = "";
        boolean editing = false;
        int offset = 0;
        while (true) {
            List<List<String>> filtered = filterRows(rows, search);
            frame(title, teaching ? "1 / Buscar   3 Ver SQL   ↑↓ Desplazar   0 Volver"
                : "1 / Buscar   ↑↓ Desplazar   0 Volver");
            line(3, 3, "Buscar: " + (editing ? "[" + search + "_]" : search.isEmpty() ? "(todos)" : search)
                + "    Coincidencias: " + filtered.size() + "/" + rows.size(), CYAN);
            int colWidth = Math.max(8, (width - 8) / Math.max(1, columns.size()));
            for (int c = 0; c < columns.size(); c++) line(3 + c * colWidth, 4, fit(columns.get(c), colWidth - 1), YELLOW);
            horizontal(3, 5, width - 6);
            int page = Math.max(1, height - 9);
            offset = Math.min(offset, Math.max(0, filtered.size() - page));
            for (int i = offset; i < filtered.size() && i < offset + page; i++) {
                List<String> values = filtered.get(i);
                for (int c = 0; c < Math.min(columns.size(), values.size()); c++)
                    line(3 + c * colWidth, 6 + i - offset, fit(values.get(c), colWidth - 1), WHITE);
            }
            line(3, height - 3, "Registros: " + filtered.size() + " | Página: " + (offset / page + 1), CYAN);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape) {
                if (editing) { editing = false; continue; }
                return;
            }
            if (editing) {
                if (type == KeyType.Enter) { editing = false; continue; }
                if (type == KeyType.Backspace && !search.isEmpty()) search = search.substring(0, search.length() - 1);
                else if (type == KeyType.Character && !Character.isISOControl(ch) && search.length() < 80) search += ch;
                else continue;
                offset = 0;
            } else if (ch == '0') return;
            else if (ch == '1' || ch == '/') { editing = true; continue; }
            else if (ch == '3' && teaching) { sqlWindow(title); continue; }
            else if (type == KeyType.ArrowDown) offset = Math.min(Math.max(0, filtered.size() - page), offset + 1);
            else if (type == KeyType.ArrowUp) offset = Math.max(0, offset - 1);
            else if (type == KeyType.PageDown) offset = Math.min(Math.max(0, filtered.size() - page), offset + page);
            else if (type == KeyType.PageUp) offset = Math.max(0, offset - page);
        }
    }

    // Comparamos el texto de todas las columnas para encontrar coincidencias.
    static List<List<String>> filterRows(List<List<String>> rows, String term) {
        if (term == null || term.isBlank()) return rows;
        String normalized = term.toLowerCase(Locale.ROOT);
        return rows.stream().filter(row -> row.stream().anyMatch(value ->
            value != null && value.toLowerCase(Locale.ROOT).contains(normalized))).toList();
    }

    private String input(String title, String prompt) throws IOException {
        StringBuilder value = new StringBuilder();
        while (true) {
            frame(title, "Escribir número   ENTER Aceptar   ESC Cancelar");
            center(height / 2 - 1, prompt + value + "_", YELLOW);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) return null;
            if (key.getKeyType() == KeyType.Enter) return value.toString();
            if (key.getKeyType() == KeyType.Backspace && value.length() > 0) value.deleteCharAt(value.length() - 1);
            if (key.getKeyType() == KeyType.Character && Character.isDigit(key.getCharacter()) && value.length() < 9)
                value.append(key.getCharacter());
        }
    }

    private void message(String title, String message) throws IOException {
        while (true) {
            frame(title, teaching ? "ENTER / ESC Volver   3 Ver SQL" : "ENTER / ESC Volver");
            int max = Math.max(12, width - 10);
            for (int i = 0; i < message.length(); i += max)
                line(5, Math.max(4, height / 2 - 2) + i / max, message.substring(i, Math.min(message.length(), i + max)), WHITE);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key != null && key.getKeyType() == KeyType.Character
                    && key.getCharacter() == '3' && teaching) {
                sqlWindow(title);
            } else if (key != null) {
                return;
            }
        }
    }

    /** Abre una ventana para estudiar las sentencias que ejecutó JDBC. */
    private void sqlWindow(String title) throws IOException {
        List<SqlTrace.Entry> entries = SqlTrace.snapshot();
        if (entries.isEmpty()) {
            return;
        }

        int current = entries.size() - 1;
        int scroll = 0;
        boolean explanation = false;

        while (true) {
            frame("MODO DIDÁCTICO · " + title,
                "←→ Sentencia   ↑↓ Desplazar   2 Explicación   0 / ESC Cerrar");
            SqlTrace.Entry entry = entries.get(current);

            // Ajustamos el código al ancho de la terminal conservando la sangría.
            List<String> lines = new ArrayList<>();
            if (explanation && !sqlExplanation.isBlank()) {
                lines.add("EXPLICACIÓN");
                lines.addAll(wrap(sqlExplanation, Math.max(15, width - 11)));
                lines.add("");
            }
            lines.add("SQL ejecutado:");
            for (String sqlLine : entry.sql().split("\\R")) {
                lines.addAll(wrap(sqlLine, Math.max(15, width - 11)));
            }
            lines.add("");
            lines.add("PARÁMETROS JDBC:");
            if (entry.parameters().isEmpty()) {
                lines.add("Sin parámetros");
            } else {
                lines.addAll(entry.parameters());
            }

            line(4, 3, "Sentencia " + (current + 1) + " de " + entries.size(), YELLOW);
            // Dibujamos una ventana interior para separar el código de la pantalla principal.
            horizontal(3, 4, width - 6);
            horizontal(3, height - 4, width - 6);
            for (int y = 5; y < height - 4; y++) {
                line(3, y, "│", WHITE);
                line(width - 4, y, "│", WHITE);
            }
            int capacity = Math.max(1, height - 10);
            scroll = Math.min(scroll, Math.max(0, lines.size() - capacity));
            for (int i = 0; i < capacity && i + scroll < lines.size(); i++) {
                line(5, 5 + i, lines.get(i + scroll),
                    lines.get(i + scroll).startsWith("SQL ejecutado")
                        || lines.get(i + scroll).startsWith("PARÁMETROS") ? YELLOW : WHITE);
            }
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape || ch == '0') return;
            if (type == KeyType.ArrowUp) scroll = Math.max(0, scroll - 1);
            if (type == KeyType.ArrowDown) scroll = Math.min(Math.max(0, lines.size() - capacity), scroll + 1);
            if (type == KeyType.PageUp) scroll = Math.max(0, scroll - capacity);
            if (type == KeyType.PageDown) scroll = Math.min(Math.max(0, lines.size() - capacity), scroll + capacity);
            if (type == KeyType.ArrowLeft) { current = Math.max(0, current - 1); scroll = 0; }
            if (type == KeyType.ArrowRight) { current = Math.min(entries.size() - 1, current + 1); scroll = 0; }
            if (ch == '2') { explanation = !explanation; scroll = 0; }
        }
    }

    // Dividimos las líneas largas para que puedan leerse en pantallas pequeñas.
    private static List<String> wrap(String value, int length) {
        List<String> result = new ArrayList<>();
        if (value.isEmpty()) {
            result.add("");
            return result;
        }
        for (int i = 0; i < value.length(); i += length) {
            result.add(value.substring(i, Math.min(value.length(), i + length)));
        }
        return result;
    }

    private void frame(String title, String help) throws IOException {
        TerminalSize size = screen.doResizeIfNecessary();
        // Consultamos el tamaño disponible para no escribir fuera de la terminal.
        width = screen.getTerminalSize().getColumns();
        height = screen.getTerminalSize().getRows();
        graphics.setBackgroundColor(BLUE);
        graphics.setForegroundColor(WHITE);
        graphics.fillRectangle(new com.googlecode.lanterna.TerminalPosition(0, 0), new TerminalSize(width, height), ' ');
        if (width < 55 || height < 18) {
            line(0, 0, "Amplíe la terminal a 55 columnas y 18 filas.", YELLOW);
            return;
        }
        graphics.setBackgroundColor(GREY);
        graphics.setForegroundColor(BLACK);
        graphics.fillRectangle(new com.googlecode.lanterna.TerminalPosition(0, 0), new TerminalSize(width, 1), ' ');
        line(2, 0, " BibliotecaSQL   Archivo   Catálogo   Préstamos   SQL ", BLACK);
        graphics.setBackgroundColor(BLUE);
        graphics.setForegroundColor(WHITE);
        horizontal(1, 2, width - 2);
        horizontal(1, height - 4, width - 2);
        for (int y = 3; y < height - 4; y++) {
            line(1, y, "│", WHITE);
            line(width - 2, y, "│", WHITE);
        }
        line(1, 2, "┌", WHITE); line(width - 2, 2, "┐", WHITE);
        line(1, height - 4, "└", WHITE); line(width - 2, height - 4, "┘", WHITE);
        center(2, "[ " + title + " ]", YELLOW);
        graphics.setBackgroundColor(GREY);
        graphics.setForegroundColor(BLACK);
        graphics.fillRectangle(new com.googlecode.lanterna.TerminalPosition(0, height - 1), new TerminalSize(width, 1), ' ');
        line(1, height - 1, help, BLACK);
        graphics.setBackgroundColor(BLUE);
        // Mostramos el estado para que sea evidente cuándo se puede consultar el SQL.
        if (teaching && height >= 5) {
            line(Math.max(2, width - 22), 1, "DIDÁCTICO ACTIVO", CYAN);
        }
    }

    private void horizontal(int x, int y, int length) {
        line(x, y, "─".repeat(Math.max(0, length)), WHITE);
    }
    private void center(int y, String text, TextColor color) {
        line(Math.max(2, (width - text.length()) / 2), y, text, color);
    }
    private void line(int x, int y, String text, TextColor color) {
        if (y < 0 || y >= height || x >= width || text == null) return;
        graphics.setForegroundColor(color);
        graphics.putString(Math.max(0, x), y, fit(text, width - Math.max(0, x)));
    }
    private String fit(String value, int max) {
        if (value == null || max <= 0) return "";
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 1)) + "…";
    }
    private KeyStroke nextKey() throws IOException {
        return screen.readInput();
    }
    @Override public void close() throws IOException { screen.stopScreen(); }
}
