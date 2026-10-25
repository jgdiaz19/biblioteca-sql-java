package edu.uv.biblioteca;

import edu.uv.biblioteca.config.DatabaseConfig;
import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LabDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.dao.ReportDao;
import edu.uv.biblioteca.service.LibraryService;
import edu.uv.biblioteca.service.LoanTransactionService;
import edu.uv.biblioteca.ui.ConsoleView;
import edu.uv.biblioteca.ui.MenuController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Iniciamos la aplicación y conectamos las clases de cada capa.
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        // Leemos la configuración de conexión del entorno de ejecución.
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        try (Connection connection = DriverManager.getConnection(config.url(), config.user(), config.password());
             ConsoleView view = new ConsoleView(System.in, System.out)) {
            // Inicializamos los objetos que consultan PostgreSQL y aplican las reglas.
            LibraryService service = new LibraryService(new BookDao(connection), new LoanDao(connection),
                new ReportDao(connection), new LabDao(connection), new LoanTransactionService(connection));
            new MenuController(view, service).run();
        } catch (SQLException ex) {
            System.err.println("No se pudo establecer la conexión con PostgreSQL. SQLState=" + ex.getSQLState());
            System.err.println("Compruebe Docker Compose y las variables DB_URL, DB_USER y DB_PASSWORD.");
        }
    }
}
