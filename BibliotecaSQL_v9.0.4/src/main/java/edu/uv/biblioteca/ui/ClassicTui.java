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
import edu.uv.biblioteca.dao.CatalogDao;
import edu.uv.biblioteca.model.CatalogEntry;
import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.BookDetails;
import edu.uv.biblioteca.model.Author;
import edu.uv.biblioteca.model.AuthorDetails;
import edu.uv.biblioteca.model.LibraryUser;
import edu.uv.biblioteca.model.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import edu.uv.biblioteca.model.LoanHistory;
import edu.uv.biblioteca.model.CategoryBook;
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
    // Usamos el mismo formato de fecha en todas las pantallas y formularios.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT);

    private static String date(LocalDate value) {
        return value == null ? "—" : value.format(DATE_FORMAT);
    }

    private static LocalDate parseDate(String value) {
        return LocalDate.parse(value, DATE_FORMAT);
    }

    private static final TextColor BLUE = new TextColor.RGB(0, 0, 170);
    private static final TextColor WHITE = new TextColor.RGB(244, 244, 244);
    private static final TextColor YELLOW = new TextColor.RGB(255, 255, 110);
    private static final TextColor CYAN = new TextColor.RGB(115, 235, 235);
    private static final TextColor GREY = new TextColor.RGB(204, 204, 204);
    private static final TextColor SQL_KEYWORD = new TextColor.RGB(255, 235, 105);
    private static final TextColor SQL_FUNCTION = new TextColor.RGB(124, 236, 236);
    private static final TextColor SQL_STRING = new TextColor.RGB(159, 255, 165);
    private static final TextColor SQL_NUMBER = new TextColor.RGB(255, 188, 135);
    private static final TextColor SQL_COMMENT = new TextColor.RGB(183, 189, 199);
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
        String[] main = {"Catálogo de libros", "Administrar catálogos", "Préstamos activos",
            "Estadísticas por categoría", "Registrar préstamo", "Registrar devolución",
            "Préstamos vencidos", "Préstamos por usuario", "Laboratorios SQL"};
        while (true) {
            int selected = menu("MENÚ PRINCIPAL", main,
                    "0 Salir   1-9 Elegir   D Modo didáctico   ↑↓ Mover");
            if (selected == 0) return;
            if (selected == -2) {
                teaching = !teaching;
                notice = "Modo didáctico: " + (teaching ? "activado" : "desactivado");
                continue;
            }
            try {
                SqlTrace.clear();
                sqlExplanation = "";
                switch (selected) {
                    case 1 -> tableBooks();
                    case 2 -> manageCatalogs();
                    case 3 -> manageLoans();
                    case 4 -> tableStats();
                    case 5 -> borrow();
                    case 6 -> giveBack();
                    case 7 -> tableOverdue();
                    case 8 -> tableUsers();
                    case 9 -> laboratory();
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
            center(height / 2 + 1, "BIBLIOTECASQL  |  v9.0", WHITE);
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
                if ((c == 'd' || c == 'D') && title.equals("MENÚ PRINCIPAL")) return -2;
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
                ? "1 Buscar  2 Cambiar filtro  3 SQL  4 Alta  5 Editar  6 Borrar  0 Volver"
                : "1 Buscar  2 Cambiar filtro  4 Alta  5 Editar  6 Borrar  0 Volver");
            line(3, 3, "Filtro: " + (searching ? "[" + search + "_]" : search.isEmpty() ? "(sin filtro)" : search), YELLOW);
            line(3, 4, "Búsqueda: " + (sqlMode ? "PostgreSQL / ILIKE" : "Java / en memoria") +
                "     Coincidencias: " + filtered.size() + "/" + complete.size(), CYAN);
            int visible = Math.max(1, height - 12);
            if (selected < offset) offset = selected;
            if (selected >= offset + visible) offset = selected - visible + 1;
            line(3, 6, fit(String.format("%-5s %-" + Math.max(12, Math.min(46, width - 47)) + "s %-20s %s", "ID", "TÍTULO", "CATEGORÍA", "ESTADO"), width - 6), YELLOW);
            horizontal(3, 7, width - 6);
            for (int i = offset; i < filtered.size() && i < offset + visible; i++) {
                Book b = filtered.get(i);
                String row = String.format("%-5d %-" + Math.max(12, Math.min(46, width - 47)) + "s %-20s %s",
                    b.id(), fit(b.title(), Math.max(12, Math.min(46, width - 47))), fit(b.category(), 20), b.available() ? "SÍ" : "NO");
                line(3, 8 + i - offset, (i == selected ? ">" : " ") + fit(row, width - 7),
                    i == selected ? YELLOW : WHITE);
            }
            line(3, height - 3, "Libro " + (filtered.isEmpty() ? 0 : selected + 1) + "/" + filtered.size()
                + "  ↑↓ Mover  PgUp/PgDn Página", CYAN);
            scrollMarker(width - 4, 8, visible, filtered.size(), offset);
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
            else if (ch == '4') {
                editBook(null);
                complete = service.listBooks(); filtered = complete;
                search = ""; selected = offset = 0;
                continue;
            }
            else if (ch == '5' && !filtered.isEmpty()) {
                editBook(service.bookDetails(filtered.get(selected).id()));
                complete = service.listBooks(); filtered = complete;
                search = ""; selected = offset = 0;
                continue;
            }
            else if (ch == '6' && !filtered.isEmpty()) {
                Book target = filtered.get(selected);
                if (confirm("ELIMINAR LIBRO", "¿Eliminar «" + target.title() + "»?")) {
                    try {
                        service.deleteBook(target.id());
                        message("OPERACIÓN CORRECTA", "El libro se eliminó correctamente.");
                    } catch (SQLException | IllegalArgumentException ex) {
                        showFailure("No se pudo eliminar el libro", ex);
                    }
                    complete = service.listBooks(); filtered = complete;
                    search = ""; selected = offset = 0;
                }
                continue;
            }
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

    // La fecha de préstamo y la fecha límite se muestran en columnas distintas.
    private void tableLoans() throws IOException, SQLException {
        manageLoans();
    }

    // ENTER abre los ejemplares de la categoría e identifica quién tiene cada libro.
    private void tableStats() throws IOException, SQLException {
        while (true) {
            var stats = service.statistics();
            List<List<String>> rows = new ArrayList<>();
            stats.forEach(x -> rows.add(List.of(x.category(), String.valueOf(x.total()),
                    String.valueOf(x.available()), String.valueOf(x.total() - x.available()))));
            TableAction selection = catalogTable("ESTADÍSTICAS POR CATEGORÍA",
                    List.of("CATEGORÍA", "TOTAL", "DISPONIBLES", "PRESTADOS"), rows,
                    "ENTER Detalle  0 Volver", false);
            if (selection.action() == 0) return;
            if (selection.action() != 1 || selection.index() < 0) continue;
            String category = stats.get(selection.index()).category();
            List<List<String>> details = new ArrayList<>();
            for (CategoryBook book : service.categoryBooks(category)) {
                details.add(List.of(book.title(), book.available() ? "Disponible" : "Prestado",
                        book.borrower() == null ? (book.available() ? "Biblioteca" : "Sin usuario activo")
                                : book.borrower()));
            }
            table("DETALLE: " + category, List.of("LIBRO", "ESTADO", "UBICACIÓN / USUARIO"), details);
        }
    }

    private void tableOverdue() throws IOException, SQLException {
        List<List<String>> rows = new ArrayList<>();
        service.overdue().forEach(x -> rows.add(List.of(x.user(), x.title(), date(x.loanDate()),
                date(x.dueDate()), String.valueOf(x.daysLate()))));
        table("PRÉSTAMOS VENCIDOS", List.of("USUARIO", "LIBRO", "PRÉSTAMO", "VENCIMIENTO", "DÍAS"), rows);
    }

    // ENTER muestra tanto los préstamos activos como los que ya fueron devueltos.
    private void tableUsers() throws IOException, SQLException {
        while (true) {
            var counts = service.userCounts();
            List<List<String>> rows = new ArrayList<>();
            counts.forEach(x -> rows.add(List.of(x.user(), String.valueOf(x.totalLoans()),
                    String.valueOf(x.activeLoans()), String.valueOf(x.returnedLoans()))));
            TableAction selection = catalogTable("PRÉSTAMOS POR USUARIO",
                    List.of("USUARIO", "TOTAL", "ACTIVOS", "DEVUELTOS"), rows,
                    "ENTER Historial  0 Volver", false);
            if (selection.action() == 0) return;
            if (selection.action() != 1 || selection.index() < 0) continue;
            var user = counts.get(selection.index());
            List<List<String>> history = new ArrayList<>();
            for (LoanHistory entry : service.loanHistory(user.userId())) {
                history.add(List.of(entry.title(), date(entry.loanDate()), date(entry.dueDate()),
                        date(entry.returnedDate()), entry.returnedDate() == null ? "Activo" : "Devuelto"));
            }
            table("HISTORIAL: " + user.user(),
                    List.of("LIBRO", "PRÉSTAMO", "LÍMITE", "DEVOLUCIÓN", "ESTADO"), history);
        }
    }

    private void laboratory() throws IOException, SQLException {
        while (true) {
            int activity = menu("LABORATORIOS SQL", new String[]{"A7 · Funciones y agrupación", "A8 · JOIN", "A9 · Subconsultas"},
                "1-3 Elegir laboratorio   0 Volver");
            if (activity == 0) return;
            int a = activity + 6;
            while (true) {
                var titles = service.labTitles(a);
                // La lista permite elegir más de nueve ejercicios sin usar teclas de función.
                int index = choose("LABORATORIO A" + a + " · " + titles.size() + " CONSULTAS", titles);
                if (index < 0) break;
                SqlTrace.clear();
                LabResult result = service.labRun(a, index + 1);
                List<List<String>> rows = new ArrayList<>();
                rows.addAll(result.rows());
                sqlExplanation = result.explanation();
                table("A" + a + " · " + result.title(), result.columns(), rows);

            }
        }
    }

    // Seleccionamos un usuario existente o lo registramos desde este mismo flujo.
    private void borrow() throws IOException, SQLException {
        List<LibraryUser> users = service.activeUsers();
        List<String> options = new ArrayList<>();
        options.add("+ Registrar nuevo usuario");
        users.forEach(u -> options.add(u.name()));
        int userChoice = choose("USUARIO DEL PRÉSTAMO", options);
        if (userChoice < 0) return;
        Integer userId = userChoice == 0 ? null : users.get(userChoice - 1).id();
        String userName = userChoice == 0 ? "Nuevo usuario" : users.get(userChoice - 1).name();
        String name = null;
        String type = null;
        String career = null;
        if (userId == null) {
            name = modalInput("NUEVO USUARIO", "Nombre completo", "", 120);
            if (name == null) return;
            type = modalInput("NUEVO USUARIO", "Tipo de usuario (Alumno/Docente/Otro)", "Alumno", 30);
            if (type == null) return;
            career = modalInput("NUEVO USUARIO", "Carrera (opcional)", "", 100);
            if (career == null) return;
            userName = name.trim();
        }

        List<Book> available = service.listBooks().stream().filter(Book::available).toList();
        if (available.isEmpty()) {
            message("SIN DISPONIBILIDAD", "No hay libros disponibles para préstamo.");
            return;
        }
        int bookChoice = choose("SELECCIONE LIBRO DISPONIBLE", available.stream()
                .map(Book::title).toList());
        if (bookChoice < 0) return;
        Book book = available.get(bookChoice);
        String period = modalInput("PLAZO DEL PRÉSTAMO", "Días (1 a 365)", "14", 3);
        if (period == null) return;
        int days;
        try {
            days = Integer.parseInt(period.trim());
            if (days < 1 || days > 365) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            message("PLAZO INCORRECTO", "Capture un plazo entre 1 y 365 días.");
            return;
        }
        String summary = userName + " | " + book.title() + " | " + days + " días";
        if (!confirm("CONFIRMAR PRÉSTAMO", summary)) return;
        try {
            // Al crear un usuario, guardamos el usuario y el préstamo de forma atómica.
            int loanId = userId == null
                    ? service.borrowWithNewUser(name, type, career, book.id(), days)
                    : service.borrow(userId, book.id(), days);
            message("OPERACIÓN CORRECTA", "Préstamo " + loanId + " registrado. Límite: "
                    + date(LocalDate.now().plusDays(days)));
        } catch (SQLException | IllegalArgumentException ex) {
            showFailure("No se pudo registrar el préstamo", ex);
        }
    }

    // Seleccionamos el préstamo desde una tabla, sin capturar identificadores.
    private void giveBack() throws IOException, SQLException {
        while (true) {
            List<Loan> loans = service.activeLoans();
            List<List<String>> rows = new ArrayList<>();
            loans.forEach(x -> rows.add(List.of(x.user(), x.title(), date(x.loanDate()), date(x.dueDate()))));
            TableAction selection = catalogTable("REGISTRAR DEVOLUCIÓN",
                    List.of("USUARIO", "LIBRO", "PRÉSTAMO", "FECHA LÍMITE"), rows,
                    "ENTER Devolver  0 Volver", false);
            if (selection.action() == 0) return;
            if (selection.action() != 1 || selection.index() < 0) continue;
            Loan loan = loans.get(selection.index());
            if (!confirm("CONFIRMAR DEVOLUCIÓN", "¿Devolver «" + loan.title() + "» de " + loan.user() + "?"))
                continue;
            try {
                service.returnBook(loan.id());
                message("OPERACIÓN CORRECTA", "Devolución registrada el " + date(LocalDate.now()));
            } catch (SQLException | IllegalArgumentException ex) {
                showFailure("No se pudo registrar la devolución", ex);
            }
        }
    }

    // Mostramos los préstamos activos en una tabla con las operaciones a la vista.
    private void manageLoans() throws IOException, SQLException {
        while (true) {
            List<Loan> loans = service.activeLoans();
            List<List<String>> rows = new ArrayList<>();
            for (Loan loan : loans) {
                rows.add(List.of(String.valueOf(loan.id()), loan.user(),
                        loan.title(), date(loan.loanDate()), date(loan.dueDate())));
            }
            TableAction choice = catalogTable("PRÉSTAMOS ACTIVOS",
                    List.of("ID", "USUARIO", "LIBRO", "PRÉSTAMO", "LÍMITE"), rows,
                    "4 Nuevo  5 Cambiar fecha  6 Devolver  7 Cancelar  0 Volver", true);
            if (choice.action() == 0) return;
            if (choice.action() == 4) { borrow(); continue; }
            if (choice.index() < 0) continue;
            Loan loan = loans.get(choice.index());
            if (choice.action() == 1) {
                message("DETALLE DEL PRÉSTAMO", loan.user() + " | " + loan.title()
                        + " | Préstamo: " + date(loan.loanDate()) + " | Límite: " + date(loan.dueDate()));
            } else if (choice.action() == 5) {
                String value = modalInput("MODIFICAR PLAZO", "Fecha límite DD-MM-AAAA",
                        date(loan.dueDate()), 10);
                if (value == null) continue;
                try {
                    service.changeDueDate(loan.id(), parseDate(value));
                    message("OPERACIÓN CORRECTA", "La fecha límite se actualizó correctamente.");
                } catch (java.time.format.DateTimeParseException ex) {
                    message("DATO INCORRECTO", "Utilice el formato DD-MM-AAAA.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo modificar el préstamo", ex);
                }
            } else if (choice.action() == 6) {
                if (!confirm("CONFIRMAR DEVOLUCIÓN", "¿Registrar la devolución del libro?")) continue;
                try {
                    service.returnBook(loan.id());
                    message("OPERACIÓN CORRECTA", "La devolución se registró correctamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo registrar la devolución", ex);
                }
            } else if (choice.action() == 7) {
                if (!confirm("CANCELAR PRÉSTAMO", "¿Cancelar el préstamo seleccionado?")) continue;
                try {
                    service.cancelLoan(loan.id());
                    message("OPERACIÓN CORRECTA", "Préstamo cancelado; libro disponible nuevamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo cancelar el préstamo", ex);
                }
            }
        }
    }

    // Agrupamos los catálogos para mantener nueve opciones en el menú principal.
    private void manageCatalogs() throws IOException, SQLException {
        while (true) {
            int option = menu("ADMINISTRACIÓN DE CATÁLOGOS",
                    new String[]{"Autores", "Categorías", "Editoriales"},
                    "1 Autores  2 Categorías  3 Editoriales  0 Volver");
            if (option == 0) return;
            if (option == 1) manageAuthors();
            if (option == 2) manageControlledCatalog(CatalogDao.Type.CATEGORY);
            if (option == 3) manageControlledCatalog(CatalogDao.Type.PUBLISHER);
        }
    }

    // Presentamos cada catálogo en una tabla navegable con las mismas teclas del catálogo de libros.
    private void manageControlledCatalog(CatalogDao.Type type) throws IOException, SQLException {
        String title = type == CatalogDao.Type.CATEGORY ? "CATEGORÍAS" : "EDITORIALES";
        while (true) {
            List<CatalogEntry> entries = service.catalog(type);
            List<List<String>> rows = new ArrayList<>();
            for (CatalogEntry entry : entries) {
                rows.add(List.of(String.valueOf(entry.id()), entry.name()));
            }
            TableAction action = catalogTable(title, List.of("ID", "NOMBRE"), rows,
                    "4 Agregar  5 Modificar  6 Eliminar  0 Volver", false);
            if (action.action() == 0) return;
            if (action.action() == 4) {
                String name = modalInput("AGREGAR " + title, "Nombre", "", 100);
                if (name == null || !confirm("CONFIRMAR ALTA", "¿Guardar el nuevo valor?")) continue;
                try {
                    service.addCatalog(type, name);
                    message("OPERACIÓN CORRECTA", "Valor registrado correctamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo registrar el valor", ex);
                }
                continue;
            }
            if (action.index() < 0) continue;
            CatalogEntry entry = entries.get(action.index());
            if (action.action() == 1) {
                message("DETALLE DE " + title, "ID: " + entry.id() + " | Nombre: " + entry.name());
            } else if (action.action() == 5) {
                String name = modalInput("MODIFICAR " + title, "Nombre", entry.name(), 100);
                if (name == null || !confirm("CONFIRMAR CAMBIO", "¿Guardar los cambios?")) continue;
                try {
                    service.updateCatalog(type, entry.id(), name);
                    message("OPERACIÓN CORRECTA", "Valor actualizado correctamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo modificar el valor", ex);
                }
            } else if (action.action() == 6) {
                if (!confirm("ELIMINAR " + title, "¿Eliminar «" + entry.name() + "»?")) continue;
                try {
                    service.deleteCatalog(type, entry.id());
                    message("OPERACIÓN CORRECTA", "Valor eliminado correctamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo eliminar: puede tener libros asociados", ex);
                }
            }
        }
    }

    // Usamos el mismo componente para buscar, seleccionar y administrar autores.
    private void manageAuthors() throws IOException, SQLException {
        while (true) {
            List<AuthorDetails> authors = service.authorCatalog();
            List<List<String>> rows = new ArrayList<>();
            for (AuthorDetails author : authors) {
                rows.add(List.of(String.valueOf(author.id()), author.name(),
                        author.nationality() == null ? "Sin especificar" : author.nationality()));
            }
            TableAction action = catalogTable("CATÁLOGO DE AUTORES",
                    List.of("ID", "NOMBRE", "NACIONALIDAD"), rows,
                    "4 Agregar  5 Modificar  6 Eliminar  0 Volver", false);
            if (action.action() == 0) return;
            if (action.action() == 4) { editAuthor(null); continue; }
            if (action.index() < 0) continue;
            AuthorDetails author = authors.get(action.index());
            if (action.action() == 1) {
                message("DETALLE DEL AUTOR", "Nombre: " + author.name()
                        + " | Nacionalidad: " + (author.nationality() == null
                        ? "Sin especificar" : author.nationality()));
            } else if (action.action() == 5) {
                editAuthor(author);
            } else if (action.action() == 6) {
                if (!confirm("ELIMINAR AUTOR", "¿Eliminar a «" + author.name() + "»?")) continue;
                try {
                    service.deleteAuthor(author.id());
                    message("OPERACIÓN CORRECTA", "El autor se eliminó correctamente.");
                } catch (SQLException | IllegalArgumentException ex) {
                    showFailure("No se pudo eliminar el autor", ex);
                }
            }
        }
    }

    // Pedimos los datos del autor en ventanas emergentes y validamos antes de guardarlos.
    private void editAuthor(AuthorDetails previous) throws IOException {
        boolean create = previous == null;
        String title = create ? "AGREGAR AUTOR" : "MODIFICAR AUTOR";
        String name = modalInput(title, "Nombre completo", create ? "" : previous.name(), 120);
        if (name == null) return;
        String nationality = modalInput(title, "Nacionalidad (opcional)",
                create ? "" : previous.nationality(), 60);
        if (nationality == null) return;
        if (!confirm(title, "¿Guardar los datos de «" + name.trim() + "»?")) return;
        try {
            if (create) {
                service.addAuthor(name, nationality);
                message("OPERACIÓN CORRECTA", "El autor se registró correctamente.");
            } else {
                service.updateAuthor(new AuthorDetails(previous.id(), name, nationality));
                message("OPERACIÓN CORRECTA", "El autor se actualizó correctamente.");
            }
        } catch (SQLException | IllegalArgumentException ex) {
            showFailure("No se pudo guardar el autor", ex);
        }
    }

    // Mostramos categorías y editoriales en una ventana sobre la pantalla principal.
    // El primer elemento permite capturar un valor que aún no está registrado.
    private String chooseControlledValue(String title, List<String> values,
                                         String current, int maxLength, boolean optional)
            throws IOException {
        while (true) {
            List<String> options = new ArrayList<>();
            options.add("+ Registrar " + (optional ? "nueva editorial" : "nueva categoría"));
            if (optional) options.add("(Sin editorial)");
            options.addAll(values);
            int index = modalCatalogSelect(title, options, current);
            if (index < 0) return null;
            if (index > 0) {
                if (optional && index == 1) return "";
                return options.get(index);
            }

            // Capturamos el nuevo valor en otra ventana sin guardar todavía en PostgreSQL.
            String value = modalInput("REGISTRAR " + title,
                    optional ? "Nombre de la editorial" : "Nombre de la categoría", "", maxLength);
            if (value == null) continue;
            String normalized = value.trim().replaceAll("\\s+", " ");
            if (normalized.isBlank()) {
                message("DATO INCORRECTO", "El nombre no puede estar vacío.");
                continue;
            }
            boolean exists = values.stream().anyMatch(v -> v.equalsIgnoreCase(normalized));
            if (exists) {
                message("VALOR EXISTENTE", "Ese nombre ya está registrado. Selecciónelo de la lista.");
                continue;
            }
            // Solo devolvemos el texto; el catálogo se actualizará al guardar el libro.
            return normalized;
        }
    }

    // Dibujamos una lista dentro de un recuadro y resaltamos el elemento actual.
    private int modalCatalogSelect(String title, List<String> options, String current)
            throws IOException {
        int selected = 0;
        int offset = 0;
        for (int i = 0; i < options.size(); i++) {
            if (current != null && options.get(i).equalsIgnoreCase(current)) {
                selected = i;
                break;
            }
        }
        while (true) {
            frame("REGISTRO DE LIBRO", "↑↓ Elegir   ENTER Aceptar   ESC Cancelar");
            int boxWidth = Math.min(Math.max(38, width - 12), 76);
            int boxHeight = Math.min(Math.max(10, height - 8), 19);
            int left = Math.max(2, (width - boxWidth) / 2);
            int top = Math.max(2, (height - boxHeight) / 2);
            int page = Math.max(1, boxHeight - 6);
            if (selected < offset) offset = selected;
            if (selected >= offset + page) offset = selected - page + 1;

            modalBox(left, top, boxWidth, boxHeight, title);
            line(left + 2, top + 1, fit(title, boxWidth - 5), YELLOW);
            for (int i = offset; i < options.size() && i < offset + page; i++) {
                String item = (i == selected ? "▶ " : "  ") + options.get(i);
                line(left + 2, top + 3 + i - offset, fit(item, boxWidth - 5),
                        i == selected ? YELLOW : WHITE);
            }
            line(left + 2, top + boxHeight - 2,
                    "↑↓ Seleccionar  ENTER Elegir  ESC Volver  " + (selected + 1) + "/" + options.size(), CYAN);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            if (type == KeyType.Escape) return -1;
            if (type == KeyType.Enter) return selected;
            if (type == KeyType.ArrowUp) selected = Math.max(0, selected - 1);
            if (type == KeyType.ArrowDown) selected = Math.min(options.size() - 1, selected + 1);
            if (type == KeyType.PageUp) selected = Math.max(0, selected - page);
            if (type == KeyType.PageDown) selected = Math.min(options.size() - 1, selected + page);
            if (type == KeyType.Home) selected = 0;
            if (type == KeyType.End) selected = options.size() - 1;
        }
    }

    // Permitimos elegir un autor registrado o capturar los datos de uno nuevo.
    private void editBook(BookDetails previous) throws IOException, SQLException {
        boolean create = previous == null;
        String title = create ? "AGREGAR LIBRO" : "MODIFICAR LIBRO";
        List<Author> authors = service.authors();

        List<String> choices = new ArrayList<>();
        choices.add("+ Registrar nuevo autor");
        for (Author author : authors) choices.add(author.name());
        int authorChoice = choose("AUTOR DEL LIBRO", choices);
        if (authorChoice < 0) return;

        // El nuevo autor se guarda después de confirmar el libro completo.
        boolean newAuthor = authorChoice == 0;
        String authorName = null;
        String nationality = null;
        if (newAuthor) {
            authorName = modalInput("NUEVO AUTOR", "Nombre completo", "", 120);
            if (authorName == null) return;
            nationality = modalInput("NUEVO AUTOR", "Nacionalidad (opcional)", "", 60);
            if (nationality == null) return;
        }

        String name = modalInput(title, "Título", create ? "" : previous.title(), 180);
        if (name == null) return;
        String isbn = modalInput(title, "ISBN", create ? "" : previous.isbn(), 20);
        if (isbn == null) return;
        String year = modalInput(title, "Año de publicación", create ? "2026" : String.valueOf(previous.year()), 4);
        if (year == null) return;
        String category = chooseControlledValue("CATEGORÍA DEL LIBRO", service.categories(),
                create ? null : previous.category(), 80, false);
        if (category == null) return;
        String publisher = chooseControlledValue("EDITORIAL DEL LIBRO", service.publishers(),
                create ? null : previous.publisher(), 100, true);
        if (publisher == null) return;
        String pages = modalInput(title, "Número de páginas", create ? "100" : String.valueOf(previous.pages()), 8);
        if (pages == null) return;
        String price = modalInput(title, "Precio", create ? "0.00" : previous.price().toPlainString(), 16);
        if (price == null) return;

        try {
            // Para un autor nuevo, el identificador real lo generará PostgreSQL.
            int authorId = newAuthor ? 1 : authors.get(authorChoice - 1).id();
            BookDetails book = new BookDetails(create ? 0 : previous.id(), name.trim(), isbn.trim(),
                    Integer.parseInt(year.trim()), category.trim(), publisher.trim(),
                    Integer.parseInt(pages.trim()), new BigDecimal(price.trim()),
                    create || previous.available(), authorId);
            if (!confirm(title, "¿Guardar los cambios de «" + book.title() + "»?")) return;

            // Registramos el libro y sus nuevos valores relacionados en una sola transacción.
            int savedId = service.saveBookWithSelections(book, create,
                    newAuthor ? authorName : null, nationality);
            message("OPERACIÓN CORRECTA", create
                    ? "Libro agregado correctamente. Referencia: " + savedId
                    : "El libro se actualizó correctamente.");
        } catch (NumberFormatException ex) {
            message("DATO INCORRECTO", "Año, páginas y precio deben contener valores numéricos válidos.");
        } catch (SQLException | IllegalArgumentException ex) {
            showFailure("No se pudo guardar el libro", ex);
        }
    }

    // Mostramos errores breves, incluidas las restricciones de integridad de PostgreSQL.
    private void showFailure(String context, Exception ex) throws IOException {
        String reason = ex.getMessage() == null ? "Error no especificado" : ex.getMessage();
        if (ex instanceof SQLException sql) {
            if ("23505".equals(sql.getSQLState())) reason = "Ya existe un registro con el mismo ISBN o nombre de catálogo.";
            if ("23503".equals(sql.getSQLState())) reason = "El registro tiene libros o préstamos asociados, o la referencia no existe.";
            if ("23514".equals(sql.getSQLState())) reason = "Algún valor no cumple las reglas de la tabla.";
        }
        message("OPERACIÓN NO COMPLETADA", context + ". " + reason);
    }

    // Separamos selección y edición: en ningún formulario solicitamos el ID interno.
    private int choose(String title, List<String> options) throws IOException {
        if (options.isEmpty()) { message(title, "No hay registros disponibles."); return -1; }
        int selected = 0;
        int offset = 0;
        String search = "";
        boolean editing = false;
        while (true) {
            List<Integer> matches = new ArrayList<>();
            for (int i = 0; i < options.size(); i++)
                if (options.get(i).toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                    matches.add(i);
            selected = Math.max(0, Math.min(selected, matches.size() - 1));
            frame(title, "/ Buscar   ↑↓ Seleccionar   ENTER Abrir   0 Volver");
            line(4, 3, "Buscar: " + (editing ? search + "_" : search.isBlank() ? "(todos)" : search), CYAN);
            int page = Math.max(1, height - 11);
            if (selected < offset) offset = selected;
            if (selected >= offset + page) offset = selected - page + 1;
            for (int i = offset; i < matches.size() && i < offset + page; i++) {
                line(4, 5 + i - offset, (i == selected ? "▶ " : "  ")
                        + fit(options.get(matches.get(i)), width - 12), i == selected ? YELLOW : WHITE);
            }
            line(4, height - 3, "Registro " + (matches.isEmpty() ? 0 : selected + 1)
                    + "/" + matches.size(), CYAN);
            scrollMarker(width - 4, 5, page, matches.size(), offset);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape) { if (editing) editing = false; else return -1; }
            else if (editing) {
                if (type == KeyType.Enter) editing = false;
                else if (type == KeyType.Backspace && !search.isEmpty()) search = search.substring(0, search.length() - 1);
                else if (type == KeyType.Character && !Character.isISOControl(ch) && search.length() < 80) search += ch;
                selected = offset = 0;
            } else if (ch == '0') return -1;
            else if (ch == '/') editing = true;
            else if (type == KeyType.Enter && !matches.isEmpty()) return matches.get(selected);
            else if (type == KeyType.ArrowDown) selected = Math.min(matches.size() - 1, selected + 1);
            else if (type == KeyType.ArrowUp) selected = Math.max(0, selected - 1);
            else if (type == KeyType.PageDown) selected = Math.min(matches.size() - 1, selected + page);
            else if (type == KeyType.PageUp) selected = Math.max(0, selected - page);
            else if (type == KeyType.Home) selected = 0;
            else if (type == KeyType.End) selected = Math.max(0, matches.size() - 1);
        }
    }

    // Redibujamos una ventana emergente y recogemos texto de cualquier campo.
    private String modalInput(String title, String label, String initial, int limit) throws IOException {
        StringBuilder text = new StringBuilder(initial == null ? "" : initial);
        while (true) {
            frame(title, "Escribir   ENTER Guardar campo   ESC Cancelar");
            int left = Math.max(3, width / 8), top = Math.max(4, height / 2 - 4);
            int boxWidth = Math.max(30, width - left * 2);
            modalBox(left, top, boxWidth, 8, title);
            line(left + 2, top + 2, fit(label, boxWidth - 5), YELLOW);
            String value = text.toString();
            line(left + 2, top + 4, fit(value, boxWidth - 6) + "_", WHITE);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) return null;
            if (key.getKeyType() == KeyType.Enter) return value;
            if (key.getKeyType() == KeyType.Backspace && text.length() > 0) text.deleteCharAt(text.length() - 1);
            if (key.getKeyType() == KeyType.Character && !Character.isISOControl(key.getCharacter())
                    && text.length() < limit) text.append(key.getCharacter());
        }
    }

    // Pedimos confirmación antes de alterar información almacenada.
    private boolean confirm(String title, String question) throws IOException {
        while (true) {
            frame(title, "S Confirmar   N Cancelar");
            int left = Math.max(3, width / 8), top = Math.max(4, height / 2 - 3);
            int boxWidth = Math.max(30, width - left * 2);
            modalBox(left, top, boxWidth, 7, title);
            line(left + 2, top + 2, fit(question, boxWidth - 5), WHITE);
            line(left + 2, top + 4, "[S] Sí      [N] No", YELLOW);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Escape) return false;
            if (key.getKeyType() == KeyType.Character) {
                char choice = Character.toLowerCase(key.getCharacter());
                if (choice == 's') return true;
                if (choice == 'n' || choice == '0') return false;
            }
        }
    }

    private void modalBox(int x, int y, int w, int h, String title) {
        graphics.setBackgroundColor(BLACK);
        graphics.setForegroundColor(WHITE);
        graphics.fillRectangle(new com.googlecode.lanterna.TerminalPosition(x, y),
                new TerminalSize(Math.min(w, width - x - 1), Math.min(h, height - y - 2)), ' ');
        line(x, y, "┌" + "─".repeat(Math.max(0, w - 2)) + "┐", WHITE);
        for (int i = 1; i < h - 1; i++) {
            line(x, y + i, "│", WHITE);
            line(x + w - 1, y + i, "│", WHITE);
        }
        line(x, y + h - 1, "└" + "─".repeat(Math.max(0, w - 2)) + "┘", WHITE);
    }

    private int parseId(String text) {
        try {
            int id = Integer.parseInt(text.trim());
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Escriba un identificador positivo."); }
    }

    // Conservamos el índice original para que la búsqueda no modifique el registro elegido.
    private record TableAction(int action, int index) { }

    // Dibujamos las columnas, el registro activo y una barra que indica la posición.
    // ENTER consulta; los números ejecutan las mismas acciones en todos los catálogos.
    private TableAction catalogTable(String title, List<String> columns,
            List<List<String>> rows, String help, boolean loans) throws IOException {
        int selected = 0;
        int offset = 0;
        String search = "";
        boolean searching = false;
        while (true) {
            List<Integer> matches = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                if (filterRows(List.of(rows.get(i)), search).size() > 0) matches.add(i);
            }
            selected = Math.max(0, Math.min(selected, matches.size() - 1));
            int page = Math.max(1, height - 10);
            if (selected < offset) offset = selected;
            if (selected >= offset + page) offset = selected - page + 1;
            offset = Math.min(offset, Math.max(0, matches.size() - page));
            frame(title, "1 / Buscar   " + help);
            line(3, 3, "Buscar: " + (searching ? "[" + search + "_]" : search.isEmpty() ? "(todos)" : search)
                    + "     Coincidencias: " + matches.size() + "/" + rows.size(), CYAN);
            int usable = Math.max(24, width - 10);
            int[] widths = columnWidths(columns, rows, usable);
            int x = 5;
            for (int c = 0; c < columns.size(); c++) {
                line(x, 5, fit(columns.get(c), widths[c] - 1), YELLOW);
                x += widths[c];
            }
            horizontal(3, 6, width - 7);
            for (int i = offset; i < matches.size() && i < offset + page; i++) {
                int y = 7 + i - offset;
                List<String> values = rows.get(matches.get(i));
                line(3, y, i == selected ? "▶" : " ", YELLOW);
                x = 5;
                for (int c = 0; c < Math.min(values.size(), widths.length); c++) {
                    line(x, y, fit(values.get(c), widths[c] - 1), i == selected ? YELLOW : WHITE);
                    x += widths[c];
                }
            }
            line(3, height - 3, "Registro " + (matches.isEmpty() ? 0 : selected + 1)
                    + "/" + matches.size() + "  ↑↓ Mover  PgUp/PgDn Página  ENTER Detalle", CYAN);
            scrollMarker(width - 4, 7, page, matches.size(), offset);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape) {
                if (searching) searching = false;
                else return new TableAction(0, -1);
            } else if (searching) {
                if (type == KeyType.Enter) searching = false;
                else if (type == KeyType.Backspace && !search.isEmpty()) {
                    search = search.substring(0, search.length() - 1);
                    selected = offset = 0;
                } else if (type == KeyType.Character && !Character.isISOControl(ch) && search.length() < 80) {
                    search += ch;
                    selected = offset = 0;
                }
            } else if (ch == '0') return new TableAction(0, -1);
            else if (ch == '1' || ch == '/') searching = true;
            else if (ch == '4') return new TableAction(4, -1);
            else if ((ch == '5' || ch == '6' || (loans && ch == '7')) && !matches.isEmpty())
                return new TableAction(ch - '0', matches.get(selected));
            else if (type == KeyType.Enter && !matches.isEmpty())
                return new TableAction(1, matches.get(selected));
            else if (type == KeyType.ArrowDown) selected = Math.min(matches.size() - 1, selected + 1);
            else if (type == KeyType.ArrowUp) selected = Math.max(0, selected - 1);
            else if (type == KeyType.PageDown) selected = Math.min(matches.size() - 1, selected + page);
            else if (type == KeyType.PageUp) selected = Math.max(0, selected - page);
            else if (type == KeyType.Home) selected = 0;
            else if (type == KeyType.End) selected = Math.max(0, matches.size() - 1);
        }
    }

    // Reservamos más espacio para los campos largos, sin ocultar las últimas columnas.
    private int[] columnWidths(List<String> columns, List<List<String>> rows, int available) {
        int n = columns.size();
        int[] widths = new int[n];
        int total = 0;
        for (int c = 0; c < n; c++) {
            int longest = columns.get(c).length() + 2;
            for (List<String> row : rows) {
                if (c < row.size()) longest = Math.max(longest, row.get(c).length() + 2);
            }
            widths[c] = Math.max(6, Math.min(longest, 34));
            total += widths[c];
        }
        while (total > available) {
            int biggest = 0;
            for (int c = 1; c < n; c++) if (widths[c] > widths[biggest]) biggest = c;
            if (widths[biggest] <= 6) break;
            widths[biggest]--;
            total--;
        }
        return widths;
    }

    private void table(String title, List<String> columns, List<List<String>> rows) throws IOException {
        // Filtramos también los reportes sin modificar sus consultas originales.
        String search = "";
        boolean editing = false;
        int offset = 0;
        int selected = 0;
        while (true) {
            List<List<String>> filtered = filterRows(rows, search);
            frame(title, teaching ? "1 / Buscar   3 Ver SQL   ↑↓ Desplazar   0 Volver"
                : "1 / Buscar   ↑↓ Desplazar   0 Volver");
            line(3, 3, "Buscar: " + (editing ? "[" + search + "_]" : search.isEmpty() ? "(todos)" : search)
                + "    Coincidencias: " + filtered.size() + "/" + rows.size(), CYAN);
            int colWidth = Math.max(8, (width - 10) / Math.max(1, columns.size()));
            for (int c = 0; c < columns.size(); c++) line(3 + c * colWidth, 4, fit(columns.get(c), colWidth - 1), YELLOW);
            horizontal(3, 5, width - 6);
            int page = Math.max(1, height - 9);
            selected = Math.min(selected, Math.max(0, filtered.size() - 1));
            if (selected < offset) offset = selected;
            if (selected >= offset + page) offset = selected - page + 1;
            offset = Math.min(offset, Math.max(0, filtered.size() - page));
            for (int i = offset; i < filtered.size() && i < offset + page; i++) {
                List<String> values = filtered.get(i);
                for (int c = 0; c < Math.min(columns.size(), values.size()); c++)
                    line(5 + c * colWidth, 6 + i - offset, fit(values.get(c), Math.max(1, colWidth - 2)),
                        i == selected ? YELLOW : WHITE);
                line(3, 6 + i - offset, i == selected ? "▶" : " ", YELLOW);
            }
            line(3, height - 3, "Registro: " + (filtered.isEmpty() ? 0 : selected + 1)
                + "/" + filtered.size() + "  |  ↑↓ Seleccionar  PgUp/PgDn Página", CYAN);
            scrollMarker(width - 4, 6, page, filtered.size(), offset);
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
                selected = 0;
            } else if (ch == '0') return;
            else if (ch == '1' || ch == '/') { editing = true; continue; }
            else if (ch == '3' && teaching) { sqlWindow(title); continue; }
            else if (type == KeyType.ArrowDown) selected = Math.min(filtered.size() - 1, selected + 1);
            else if (type == KeyType.ArrowUp) selected = Math.max(0, selected - 1);
            else if (type == KeyType.PageDown) selected = Math.min(filtered.size() - 1, selected + page);
            else if (type == KeyType.PageUp) selected = Math.max(0, selected - page);
            else if (type == KeyType.Home) selected = 0;
            else if (type == KeyType.End) selected = Math.max(0, filtered.size() - 1);
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

    // Mostramos el resultado de la operación en una ventana emergente.
    private void message(String title, String message) throws IOException {
        while (true) {
            frame("BIBLIOTECASQL", teaching ? "ENTER / ESC Volver   3 Ver SQL" : "ENTER / ESC Volver");
            int left = Math.max(3, width / 10);
            int boxWidth = Math.max(30, width - left * 2);
            int top = Math.max(4, height / 2 - 4);
            modalBox(left, top, boxWidth, 8, title);
            String content = message == null ? "" : message;
            int lineWidth = Math.max(15, boxWidth - 6);
            List<String> lines = wrap(content, lineWidth);
            for (int i = 0; i < Math.min(3, lines.size()); i++) {
                line(left + 2, top + 2 + i, lines.get(i), WHITE);
            }
            line(left + 2, top + 6, "ENTER / ESC  Continuar", YELLOW);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            if (key.getKeyType() == KeyType.Character && key.getCharacter() == '3' && teaching) {
                sqlWindow(title);
            } else if (key.getKeyType() == KeyType.Enter || key.getKeyType() == KeyType.Escape) {
                return;
            }
        }
    }

    /** Presentamos el SQL, el código Java real y sus explicaciones en vistas separadas. */
    private void sqlWindow(String title) throws IOException {
        List<SqlTrace.Entry> entries = SqlTrace.snapshot();
        if (entries.isEmpty()) return;

        int current = entries.size() - 1;
        int scroll = 0;
        int selectedLine = 0;
        int view = 1;

        while (true) {
            frame("MODO DIDÁCTICO · " + title,
                "1 SQL  2 Java  3 Exp. SQL  4 Exp. Java  ←→ Sentencia  ↑↓ Leer  0 Salir");
            SqlTrace.Entry entry = entries.get(current);
            JavaSourceView.Source source = JavaSourceView.forEntry(entry);
            List<String> lines = new ArrayList<>();
            String heading;
            if (view == 1) {
                heading = "SENTENCIA SQL EJECUTADA";
                for (String sqlLine : entry.sql().split("\\R", -1)) {
                    lines.addAll(wrap(sqlLine, Math.max(15, width - 11)));
                }
            } else if (view == 2) {
                heading = "CÓDIGO JAVA · " + source.location();
                for (String javaLine : source.lines())
                    lines.addAll(wrap(javaLine, Math.max(15, width - 11)));
            } else {
                heading = view == 3 ? "EXPLICACIÓN SQL" : "EXPLICACIÓN JAVA";
                String explanation = view == 3
                    ? LearningGuide.sqlExplanation(entry, sqlExplanation)
                    : LearningGuide.javaExplanation(entry);
                for (String paragraph : explanation.split("\\R", -1)) {
                    lines.addAll(wrap(paragraph, Math.max(15, width - 11)));
                }
            }
            line(4, 3, "Sentencia " + (current + 1) + " de " + entries.size()
                + " | " + heading, YELLOW);
            horizontal(3, 4, width - 6);
            horizontal(3, height - 4, width - 6);
            for (int y = 5; y < height - 4; y++) {
                line(3, y, "│", WHITE);
                line(width - 4, y, "│", WHITE);
            }
            int capacity = Math.max(1, height - 9);
            selectedLine = Math.max(0, Math.min(selectedLine, Math.max(0, lines.size() - 1)));
            if (selectedLine < scroll) scroll = selectedLine;
            if (selectedLine >= scroll + capacity) scroll = selectedLine - capacity + 1;
            scroll = Math.min(scroll, Math.max(0, lines.size() - capacity));
            for (int i = 0; i < capacity && i + scroll < lines.size(); i++) {
                String row = lines.get(i + scroll);
                line(4, 5 + i, i + scroll == selectedLine ? "▶" : " ", YELLOW);
                if (view == 1) coloredSqlLine(5, 5 + i, row);
                else if (view == 2) coloredJavaLine(5, 5 + i, row);
                else line(5, 5 + i, row, WHITE);
            }
            line(4, height - 3, "Líneas " + (lines.isEmpty() ? 0 : scroll + 1) + "–"
                + Math.min(lines.size(), scroll + capacity) + "/" + lines.size()
                + "  Selección " + (selectedLine + 1) + "  ↑↓ Mover  PgUp/PgDn", CYAN);
            scrollMarker(width - 5, 5, capacity, lines.size(), scroll);
            screen.refresh();
            KeyStroke key = nextKey();
            if (key == null) continue;
            KeyType type = key.getKeyType();
            char ch = type == KeyType.Character ? key.getCharacter() : '\0';
            if (type == KeyType.Escape || ch == '0') return;
            if (type == KeyType.ArrowUp) selectedLine = Math.max(0, selectedLine - 1);
            if (type == KeyType.ArrowDown) selectedLine = Math.min(Math.max(0, lines.size() - 1), selectedLine + 1);
            if (type == KeyType.PageUp) selectedLine = Math.max(0, selectedLine - capacity);
            if (type == KeyType.PageDown) selectedLine = Math.min(Math.max(0, lines.size() - 1), selectedLine + capacity);
            if (type == KeyType.Home) selectedLine = 0;
            if (type == KeyType.End) selectedLine = Math.max(0, lines.size() - 1);
            if (type == KeyType.ArrowLeft) { current = Math.max(0, current - 1); scroll = selectedLine = 0; }
            if (type == KeyType.ArrowRight) { current = Math.min(entries.size() - 1, current + 1); scroll = selectedLine = 0; }
            if (ch >= '1' && ch <= '4') { view = ch - '0'; scroll = selectedLine = 0; }
        }
    }

    // Coloreamos palabras reservadas, tipos, literales y comentarios de Java.
    private void coloredJavaLine(int x, int y, String source) {
        int position = x;
        for (JavaSyntax.Part part : JavaSyntax.tokenize(source)) {
            TextColor color = switch (part.kind()) {
                case KEYWORD -> SQL_KEYWORD;
                case TYPE -> SQL_FUNCTION;
                case STRING -> SQL_STRING;
                case NUMBER -> SQL_NUMBER;
                case COMMENT -> SQL_COMMENT;
                default -> WHITE;
            };
            line(position, y, part.text(), color);
            position += part.text().length();
        }
    }

    // Pintamos los elementos de SQL sin modificar la sentencia original.
    private void coloredSqlLine(int x, int y, String source) {
        int position = x;
        for (SqlSyntax.Part part : SqlSyntax.tokenize(source)) {
            TextColor color = switch (part.kind()) {
                case KEYWORD -> SQL_KEYWORD;
                case FUNCTION -> SQL_FUNCTION;
                case STRING -> SQL_STRING;
                case NUMBER -> SQL_NUMBER;
                case COMMENT -> SQL_COMMENT;
                default -> WHITE;
            };
            line(position, y, part.text(), color);
            position += part.text().length();
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

    // Dibujamos una barra lateral para indicar qué parte del contenido estamos leyendo.
    private void scrollMarker(int x, int y, int visible, int total, int offset) {
        if (total <= visible || visible < 2) return;
        int thumb = Math.max(1, (int) Math.round((double) visible * visible / total));
        int travel = Math.max(0, visible - thumb);
        int position = (int) Math.round((double) offset * travel / Math.max(1, total - visible));
        for (int i = 0; i < visible && y + i < height - 4; i++) {
            line(x, y + i, i >= position && i < position + thumb ? "█" : "░",
                i >= position && i < position + thumb ? YELLOW : GREY);
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
