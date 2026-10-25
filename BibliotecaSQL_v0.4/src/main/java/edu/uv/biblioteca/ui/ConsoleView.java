package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.CategoryStat;
import edu.uv.biblioteca.model.LabResult;
import edu.uv.biblioteca.model.Loan;
import edu.uv.biblioteca.model.OverdueLoan;
import edu.uv.biblioteca.model.UserLoanStat;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Concentramos aquí la interacción con el teclado y la pantalla.
public final class ConsoleView implements AutoCloseable {
    private final Scanner scanner;
    private final PrintStream out;
    private final boolean ansi;
    private static final int WIDTH = 88;

    public ConsoleView(InputStream input, PrintStream output) {
        // Inicializamos el lector y comprobamos si podemos limpiar la terminal.
        this.scanner = new Scanner(input, StandardCharsets.UTF_8);
        this.out = output;
        this.ansi = System.console() != null || "true".equalsIgnoreCase(System.getenv("FORCE_ANSI"));
    }
    public void clear() {
        // Utilizamos secuencias ANSI cuando la terminal permite limpiar la pantalla.
        if (ansi) out.print("\033[2J\033[H"); else out.println("\n".repeat(3));
        out.flush();
    }
    public void heading(String title) {
        out.println("╔" + "═".repeat(WIDTH - 2) + "╗");
        out.println("║ " + String.format("%-" + (WIDTH - 4) + "s", fit(title, WIDTH - 4)) + " ║");
        out.println("╚" + "═".repeat(WIDTH - 2) + "╝");
    }
    public void menu(boolean teaching) {
        clear();
        heading("BIBLIOTECASQL · V0.4 · POSTGRESQL");
        out.println("  1. Catálogo de libros");
        out.println("  2. Buscar libros por título");
        out.println("  3. Préstamos activos");
        out.println("  4. Estadísticas por categoría");
        out.println("  5. Registrar préstamo");
        out.println("  6. Registrar devolución");
        out.println("  7. Reporte de préstamos vencidos");
        out.println("  8. Reporte de préstamos por usuario");
        out.println("  9. Laboratorio de consultas SQL");
        out.println("  D. Modo didáctico SQL: " + (teaching ? "ACTIVO" : "INACTIVO"));
        out.println("  0. Salir");
    }
    public String ask(String label) {
        out.print(label);
        if (!scanner.hasNextLine()) return null;
        return scanner.nextLine().trim();
    }
    public void message(String message) { out.println(message); }
    public void pause() { ask("\nPulse ENTER para regresar al menú..."); }
    public void sql(String query, String parameter) {
        out.println("\nCONSULTA SQL:");
        out.println(query.strip() + ";");
        if (parameter != null) out.println("Parámetro 1: " + parameter);
        out.println();
    }
    // Recortamos valores extensos para mantener las columnas alineadas.
    public static String fit(String value, int length) {
        if (value == null) return "";
        String clean = value.replaceAll("[\r\n\t]", " ");
        if (clean.length() <= length) return clean;
        return clean.substring(0, length - 1) + "…";
    }
    public void table(List<String> columns, List<List<String>> rows, int... widths) {
        // Dibujamos líneas y celdas con anchos definidos para cada columna.
        StringBuilder border = new StringBuilder("+");
        for (int width : widths) border.append("-").append("-".repeat(width)).append("-+");
        out.println(border);
        printRow(columns, widths);
        out.println(border);
        for (List<String> row : rows) printRow(row, widths);
        out.println(border);
        out.println("Registros: " + rows.size());
    }
    private void printRow(List<String> cells, int... widths) {
        out.print("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            out.print(" " + String.format("%-" + widths[i] + "s", fit(cell, widths[i])) + " |");
        }
        out.println();
    }
    public void books(List<Book> books) {
        List<List<String>> rows = new ArrayList<>();
        for (Book book : books) rows.add(List.of(String.valueOf(book.id()), book.title(), book.category(), book.available() ? "Sí" : "No"));
        table(List.of("ID", "TÍTULO", "CATEGORÍA", "DISP."), rows, 4, 39, 22, 6);
    }
    public void loans(List<Loan> loans) {
        List<List<String>> rows = new ArrayList<>();
        for (Loan loan : loans) rows.add(List.of(String.valueOf(loan.id()), loan.title(), loan.user(), String.valueOf(loan.dueDate())));
        table(List.of("ID", "LIBRO", "USUARIO", "FECHA LÍMITE"), rows, 4, 33, 24, 12);
    }
    public void statistics(List<CategoryStat> stats) {
        List<List<String>> rows = new ArrayList<>();
        for (CategoryStat item : stats) rows.add(List.of(item.category(), String.valueOf(item.total()), String.valueOf(item.available())));
        table(List.of("CATEGORÍA", "TOTAL", "DISPONIBLES"), rows, 49, 9, 13);
    }
    public void overdue(List<OverdueLoan> loans) {
        List<List<String>> rows = new ArrayList<>();
        for (OverdueLoan loan : loans) rows.add(List.of(String.valueOf(loan.id()), loan.title(), loan.user(), String.valueOf(loan.dueDate()), String.valueOf(loan.daysLate())));
        table(List.of("ID", "LIBRO", "USUARIO", "LÍMITE", "DÍAS"), rows, 4, 28, 22, 12, 6);
    }
    public void userCounts(List<UserLoanStat> stats) {
        List<List<String>> rows = new ArrayList<>();
        for (UserLoanStat stat : stats) rows.add(List.of(String.valueOf(stat.userId()), stat.user(), String.valueOf(stat.activeLoans())));
        table(List.of("ID", "USUARIO", "ACTIVOS"), rows, 6, 53, 10);
    }
    public void laboratoryMenu(List<String> titles) {
        heading("LABORATORIO SQL");
        for (int i = 0; i < titles.size(); i++) out.printf("  %d. %s%n", i + 1, titles.get(i));
        out.println("  0. Regresar al menú principal");
    }
    public void laboratoryResult(LabResult result) {
        heading(result.title().toUpperCase());
        sql(result.sql(), null);
        out.println(result.explanation());
        out.println();
        int[] widths = new int[result.columns().size()];
        // Calculamos un ancho práctico para cada columna sin rebasar la terminal.
        int available = Math.max(8, (WIDTH - result.columns().size() * 3 - 1) / result.columns().size() - 1);
        for (int i = 0; i < widths.length; i++) widths[i] = Math.min(28, Math.max(8, available));
        table(result.columns(), result.rows(), widths);
    }
    // No cerramos System.in porque pertenece al proceso de Java.
    @Override public void close() { }
}
