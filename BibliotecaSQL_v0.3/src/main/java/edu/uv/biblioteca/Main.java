package edu.uv.biblioteca;

import edu.uv.biblioteca.config.DatabaseConfig;
import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.service.LibraryService;
import edu.uv.biblioteca.service.LoanTransactionService;
import edu.uv.biblioteca.ui.ConsoleView;
import edu.uv.biblioteca.ui.MenuController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Punto de entrada: ensambla las capas sin mezclar SQL con la interfaz. */
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        try (Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password());
             ConsoleView view = new ConsoleView(System.in, System.out)) {
            LibraryService service = new LibraryService(new BookDao(connection), new LoanDao(connection), new LoanTransactionService(connection));
            new MenuController(view, service).run();
        } catch (SQLException e) {
            System.err.println("No se pudo conectar con PostgreSQL. SQLState=" + e.getSQLState());
            System.err.println("Verifique Docker Compose y DB_URL, DB_USER y DB_PASSWORD.");
            System.exit(1);
        }
    }
}
