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
        // Mostramos la presentación institucional antes de abrir el menú.
        view.splash();
        // Mantenemos abierto el menú hasta elegir salir o terminar la entrada.
        while (true) {
            view.menu(teaching);
            String choice = view.ask("\nSeleccione una opción: ");
            if (choice == null || "0".equals(choice)) break;
            if ("D".equalsIgnoreCase(choice)) { teaching = !teaching; continue; }
            if ("C".equalsIgnoreCase(choice)) { view.toggleColors(); continue; }
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
                        view.success("Préstamo registrado. Identificador: " + loanId);
                        if (teaching) view.message("Transacción: comprobaciones, bloqueo de filas, INSERT, UPDATE y COMMIT.");
                    }
                    case "6" -> {
                        view.heading("REGISTRAR DEVOLUCIÓN");
                        Integer loanId = positiveId("ID del préstamo: ");
                        if (loanId == null) return;
                        service.returnBook(loanId);
                        view.success("Devolución registrada. El libro vuelve a estar disponible.");
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
                view.error("Error de PostgreSQL. SQLState: " + ex.getSQLState());
            } catch (IllegalArgumentException ex) {
                view.message(ex.getMessage());
            }
            view.pause();
        }
        view.message("Hasta pronto.");
    }
    private void laboratory() {
        // Separamos las actividades A7, A8 y A9 para facilitar la navegación.
        while (true) {
            view.clear();
            view.labGroups();
            String option = view.ask("\nSeleccione la actividad: ");
            if (option == null || "0".equals(option)) return;
            int activity;
            switch (option) {
                case "1" -> activity = 7;
                case "2" -> activity = 8;
                case "3" -> activity = 9;
                default -> {
                    view.error("Seleccione una opción entre 0 y 3.");
                    view.pause();
                    continue;
                }
            }
            labActivity(activity);
        }
    }
    private void labActivity(int activity) {
        while (true) {
            view.clear();
            view.heading("LABORATORIO A" + activity);
            view.laboratoryMenu(service.labTitles(activity));
            String option = view.ask("\nSeleccione la consulta: ");
            if (option == null || "0".equals(option)) return;
            view.clear();
            try {
                int number = Integer.parseInt(option);
                view.laboratoryResult(service.labRun(activity, number));
            } catch (SQLException ex) {
                view.error("No se pudo ejecutar la consulta. SQLState: " + ex.getSQLState());
            } catch (NumberFormatException ex) {
                view.error("Escriba el número de una consulta.");
            } catch (IllegalArgumentException ex) {
                view.error(ex.getMessage());
            }
            view.pause();
        }
    }
}
