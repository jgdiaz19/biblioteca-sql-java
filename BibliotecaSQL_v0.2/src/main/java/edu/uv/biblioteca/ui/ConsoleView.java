package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.Loan;
import edu.uv.biblioteca.model.CategoryStat;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/** Interfaz de terminal sin comandos cls/clear y sin consultas SQL. */
public final class ConsoleView implements AutoCloseable {
    private final Scanner scanner;
    private final PrintStream out;
    private final boolean ansi;

    public ConsoleView(InputStream input, PrintStream output) {
        this.scanner = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
        this.ansi = System.console() != null || "true".equalsIgnoreCase(System.getenv("FORCE_ANSI"));
    }
    public void clear() {
        if (ansi) out.print("\033[2J\033[H");
        else out.println("\n".repeat(3));
        out.flush();
    }
    public void heading(String title) {
        out.println("╔" + "═".repeat(68) + "╗");
        out.printf("║ %-66s ║%n", fit(title, 66));
        out.println("╚" + "═".repeat(68) + "╝");
    }
    public void menu(boolean teaching) {
        clear();
        heading("BIBLIOTECASQL · V0.2 · POSTGRESQL");
        out.println("  1. Catálogo de libros");
        out.println("  2. Buscar libros por título");
        out.println("  3. Préstamos activos");
        out.println("  4. Estadísticas por categoría");
        out.println("  5. Cambiar modo didáctico SQL: " + (teaching ? "ACTIVO" : "INACTIVO"));
        out.println("  0. Salir");
    }
    public String ask(String label) {
        out.print(label);
        if (!scanner.hasNextLine()) return null;
        return scanner.nextLine().trim();
    }
    public void message(String message) { out.println(message); }
    public void pause() { ask("\nPulse ENTER para regresar al menú..."); }
    public void sql(String sql, String parameter) {
        out.println("\n[LABORATORIO SQL] Consulta ejecutada:");
        out.println(sql + ";");
        if (parameter != null) out.println("Parámetro 1 = " + parameter);
        out.println();
    }
    public void books(List<Book> books) {
        out.printf("%-4s | %-37s | %-22s | %s%n", "ID", "TÍTULO", "CATEGORÍA", "DISP.");
        out.println("-".repeat(80));
        for (Book b: books) out.printf("%4d | %-37s | %-22s | %s%n", b.id(),fit(b.title(),37),
                fit(b.category(),22),b.available() ? "Sí" : "No");
        out.println("Total: " + books.size());
    }
    public void loans(List<Loan> loans) {
        out.printf("%-4s | %-30s | %-23s | %s%n", "ID", "LIBRO", "USUARIO", "FECHA LÍMITE");
        out.println("-".repeat(82));
        for (Loan loan: loans) out.printf("%4d | %-30s | %-23s | %s%n",loan.id(),fit(loan.title(),30),
                fit(loan.user(),23),loan.dueDate());
        out.println("Total: " + loans.size());
    }
    public void statistics(List<CategoryStat> stats) {
        out.printf("%-43s | %7s | %12s%n", "CATEGORÍA", "TOTAL", "DISPONIBLES");
        out.println("-".repeat(70));
        for (CategoryStat stat: stats) out.printf("%-43s | %7d | %12d%n",fit(stat.category(),43),
                stat.total(),stat.available());
        out.println("Categorías: " + stats.size());
    }
    /** Elimina saltos de línea y recorta sin desalinear al imprimir cadenas largas. */
    public static String fit(String value, int length) {
        if (value == null) return "";
        String single = value.replaceAll("[\r\n\t]", " ");
        if (single.length() <= length) return single;
        return single.substring(0,length-1) + "…";
    }
    @Override public void close() { /* System.in no debe cerrarse en bibliotecas reutilizables */ }
}
