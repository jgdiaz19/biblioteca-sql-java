package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.dao.ReportDao;
import edu.uv.biblioteca.service.LibraryService;
import java.sql.SQLException;

// Interpretamos las opciones del menú y delegamos las operaciones al servicio.
public final class MenuController {
    private final ConsoleView view;
    private final LibraryService service;
    private boolean teaching;
    public MenuController(ConsoleView view, LibraryService service) {
        // Guardamos los objetos necesarios para comunicarnos con las otras capas.
        this.view = view;
        this.service = service;
    }
    private Integer positiveId(String label) {
        String input = view.ask(label);
        if (input == null) return null;
        try {
            int id = Integer.parseInt(input);
            // Validamos que el identificador sea un entero mayor que cero.
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Introduzca un identificador entero positivo.");
        }
    }
    public void run() {
        // Mantenemos abierto el menú hasta elegir salir o terminar la entrada.
        while (true) {
            view.menu(teaching);
            String choice = view.ask("\nSeleccione una opción: ");
            if (choice == null || "0".equals(choice)) break;
            if ("D".equalsIgnoreCase(choice)) { teaching = !teaching; continue; }
            view.clear();
            try {
                switch (choice) {
                    case "1" -> {
                        view.heading("CATÁLOGO DE LIBROS");
                        if (teaching) view.sql(BookDao.LIST_SQL, null);
                        view.books(service.listBooks());
                    }
                    case "2" -> {
                        view.heading("BUSCAR LIBROS");
                        String term = view.ask("Texto a buscar: ");
                        if (term == null) return;
                        var result = service.searchBooks(term);
                        if (teaching) view.sql(BookDao.SEARCH_SQL, "%" + term + "%");
                        view.books(result);
                    }
                    case "3" -> {
                        view.heading("PRÉSTAMOS ACTIVOS");
                        if (teaching) view.sql(LoanDao.ACTIVE_SQL, null);
                        view.loans(service.activeLoans());
                    }
                    case "4" -> {
                        view.heading("ESTADÍSTICAS POR CATEGORÍA");
                        if (teaching) view.sql(BookDao.STATS_SQL, null);
                        view.statistics(service.statistics());
                    }
                    case "5" -> {
                        view.heading("REGISTRAR PRÉSTAMO");
                        view.message("Límite: 3 préstamos activos; plazo: 14 días.");
                        Integer userId = positiveId("ID del usuario: ");
                        if (userId == null) return;
                        Integer bookId = positiveId("ID del libro: ");
                        if (bookId == null) return;
                        int loanId = service.borrow(userId, bookId);
                        view.message("Préstamo registrado. Identificador: " + loanId);
                        if (teaching) view.message("Transacción: comprobaciones, bloqueo de filas, INSERT, UPDATE y COMMIT.");
                    }
                    case "6" -> {
                        view.heading("REGISTRAR DEVOLUCIÓN");
                        Integer loanId = positiveId("ID del préstamo: ");
                        if (loanId == null) return;
                        service.returnBook(loanId);
                        view.message("Devolución registrada. El libro vuelve a estar disponible.");
                        if (teaching) view.message("Transacción: bloqueo de filas, actualización del préstamo, UPDATE del libro y COMMIT.");
                    }
                    case "7" -> {
                        view.heading("PRÉSTAMOS VENCIDOS");
                        if (teaching) view.sql(ReportDao.OVERDUE_SQL, null);
                        view.overdue(service.overdue());
                    }
                    case "8" -> {
                        view.heading("PRÉSTAMOS ACTIVOS POR USUARIO");
                        if (teaching) view.sql(ReportDao.USER_COUNTS_SQL, null);
                        view.userCounts(service.userCounts());
                    }
                    case "9" -> laboratory();
                    default -> view.message("Opción no válida. Seleccione un valor del menú.");
                }
            } catch (SQLException ex) {
                view.message("Error de PostgreSQL. SQLState: " + ex.getSQLState());
            } catch (IllegalArgumentException ex) {
                view.message(ex.getMessage());
            }
            view.pause();
        }
        view.message("Hasta pronto.");
    }
    private void laboratory() throws SQLException {
        // Mostramos un submenú para elegir una consulta SQL preparada.
        while (true) {
            view.clear();
            view.laboratoryMenu(service.labTitles());
            String option = view.ask("\nConsulta: ");
            if (option == null || "0".equals(option)) return;
            try {
                int number = Integer.parseInt(option);
                view.clear();
                view.laboratoryResult(service.labRun(number));
            } catch (NumberFormatException ex) {
                view.message("Escriba el número de la consulta.");
            } catch (IllegalArgumentException ex) {
                view.message(ex.getMessage());
            }
            view.pause();
        }
    }
}
