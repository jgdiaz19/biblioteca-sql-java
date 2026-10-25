package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.service.LibraryService;
import java.sql.SQLException;

/** Controlador: recibe opciones, llama servicios y presenta resultados. */
public final class MenuController {
    private final ConsoleView view;
    private final LibraryService service;
    private boolean teaching;
    public MenuController(ConsoleView view, LibraryService service) {
        this.view=view; this.service=service;
    }
    private Integer positiveId(String label) {
        String input = view.ask(label);
        if (input == null) return null;
        try {
            int id = Integer.parseInt(input);
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Introduzca un identificador entero positivo.");
        }
    }
    public void run() {
        while (true) {
            view.menu(teaching);
            String choice = view.ask("\nSeleccione una opción: ");
            if (choice == null || "0".equals(choice)) break;
            if ("7".equals(choice)) { teaching = !teaching; continue; }
            view.clear();
            try {
                switch (choice) {
                    case "1" -> { view.heading("CATÁLOGO DE LIBROS");
                        if (teaching) view.sql(BookDao.LIST_SQL, null);
                        view.books(service.listBooks()); }
                    case "2" -> { view.heading("BUSCAR LIBROS");
                        String term = view.ask("Texto a buscar: ");
                        if (term == null) return;
                        var result = service.searchBooks(term);
                        if (teaching) view.sql(BookDao.SEARCH_SQL, "%" + term + "%");
                        view.books(result); }
                    case "3" -> { view.heading("PRÉSTAMOS ACTIVOS");
                        if (teaching) view.sql(LoanDao.ACTIVE_SQL, null);
                        view.loans(service.activeLoans()); }
                    case "4" -> { view.heading("ESTADÍSTICAS POR CATEGORÍA");
                        if (teaching) view.sql(BookDao.STATS_SQL, null);
                        view.statistics(service.statistics()); }
                    case "5" -> { view.heading("REGISTRAR PRÉSTAMO");
                        view.message("Regla: máximo 3 préstamos activos por usuario; plazo de 14 días.");
                        Integer userId = positiveId("ID del usuario: ");
                        if (userId == null) return;
                        Integer bookId = positiveId("ID del libro: ");
                        if (bookId == null) return;
                        int loanId = service.borrow(userId, bookId);
                        view.message("Préstamo registrado correctamente. ID: " + loanId);
                        if (teaching) view.message("JDBC: BEGIN, SELECT ... FOR UPDATE, INSERT ... RETURNING, UPDATE, COMMIT."); }
                    case "6" -> { view.heading("REGISTRAR DEVOLUCIÓN");
                        Integer loanId = positiveId("ID del préstamo: ");
                        if (loanId == null) return;
                        service.returnBook(loanId);
                        view.message("Devolución registrada. El libro vuelve a estar disponible.");
                        if (teaching) view.message("JDBC: BEGIN, SELECT ... FOR UPDATE, UPDATE préstamos, UPDATE libros, COMMIT."); }
                    default -> view.message("Opción no válida. Seleccione un número del menú.");
                }
            } catch (SQLException e) {
                view.message("Error en PostgreSQL. SQLState: " + e.getSQLState());
            } catch (IllegalArgumentException e) {
                view.message(e.getMessage());
            }
            view.pause();
        }
        view.message("Hasta pronto.");
    }
}
