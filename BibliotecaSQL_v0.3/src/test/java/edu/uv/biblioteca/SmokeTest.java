package edu.uv.biblioteca;
import edu.uv.biblioteca.ui.ConsoleView;
import edu.uv.biblioteca.config.DatabaseConfig;
/** Comprobaciones ligeras sin framework externo ni conexión a PostgreSQL. */
public final class SmokeTest {
    public static void main(String[] args) {
        if (!ConsoleView.fit("Título muy largo", 7).equals("Título…")) throw new AssertionError("recorte");
        if (!ConsoleView.fit("Breve", 12).equals("Breve")) throw new AssertionError("texto corto");
        if (!DatabaseConfig.fromEnvironment().url().startsWith("jdbc:postgresql:")) throw new AssertionError("URL JDBC");
        System.out.println("Pruebas básicas correctas");
    }
}
