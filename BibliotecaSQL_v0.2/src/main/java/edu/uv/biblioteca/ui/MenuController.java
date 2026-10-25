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
    public void run() {
        while (true) {
            view.menu(teaching);
            String choice = view.ask("\nSeleccione una opción: ");
            if (choice == null || "0".equals(choice)) break;
            if ("5".equals(choice)) { teaching = !teaching; continue; }
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
