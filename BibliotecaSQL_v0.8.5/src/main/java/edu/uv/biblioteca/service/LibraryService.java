package edu.uv.biblioteca.service;

import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LabDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.dao.ReportDao;
import edu.uv.biblioteca.model.*;
import java.sql.SQLException;
import java.util.List;

// Reunimos las operaciones que pueden solicitarse desde la interfaz.
// Esta clase comunica los menús con los objetos que acceden a PostgreSQL.
public final class LibraryService {
    private final BookDao books;
    private final LoanDao loans;
    private final ReportDao reports;
    private final LabDao laboratory;
    private final LoanTransactionService transactions;
    public LibraryService(BookDao books, LoanDao loans, ReportDao reports,
                          LabDao laboratory, LoanTransactionService transactions) {
        // Inicializamos las dependencias que se utilizarán en cada operación.
        this.books = books;
        this.loans = loans;
        this.reports = reports;
        this.laboratory = laboratory;
        this.transactions = transactions;
    }
    public int borrow(int userId, int bookId) throws SQLException { return transactions.borrow(userId, bookId); }
    public void returnBook(int loanId) throws SQLException { transactions.returnBook(loanId); }
    public List<Book> listBooks() throws SQLException { return books.list(); }
    public List<Book> searchBooks(String term) throws SQLException {
        // Evitamos consultas con búsquedas vacías o demasiado extensas.
        if (term == null || term.isBlank()) throw new IllegalArgumentException("Escriba un título para buscar.");
        if (term.length() > 120) throw new IllegalArgumentException("La búsqueda es demasiado larga.");
        return books.search(term.trim());
    }
    public List<Loan> activeLoans() throws SQLException { return loans.active(); }
    public List<CategoryStat> statistics() throws SQLException { return books.stats(); }
    public List<OverdueLoan> overdue() throws SQLException { return reports.overdue(); }
    public List<UserLoanStat> userCounts() throws SQLException { return reports.userCounts(); }
    public List<String> labTitles() { return laboratory.titles(); }
    public LabResult labRun(int number) throws SQLException { return laboratory.run(number); }
    public List<String> labTitles(int activity) { return laboratory.titles(activity); }
    public LabResult labRun(int activity, int number) throws SQLException { return laboratory.run(activity, number); }
    // Validamos el formulario antes de enviar los valores al DAO.
    private void validateBook(BookDetails b) {
        if (b.title() == null || b.title().isBlank() || b.title().length() > 180)
            throw new IllegalArgumentException("El título es obligatorio y admite hasta 180 caracteres.");
        if (b.isbn() == null || b.isbn().isBlank() || b.isbn().length() > 20)
            throw new IllegalArgumentException("El ISBN es obligatorio y admite hasta 20 caracteres.");
        if (b.category() == null || b.category().isBlank() || b.category().length() > 80)
            throw new IllegalArgumentException("La categoría es obligatoria y admite hasta 80 caracteres.");
        if (b.publisher() != null && b.publisher().length() > 100)
            throw new IllegalArgumentException("La editorial admite hasta 100 caracteres.");
        if (b.year() < 1450 || b.year() > java.time.Year.now().getValue() + 1)
            throw new IllegalArgumentException("Revise el año de publicación.");
        if (b.pages() <= 0 || b.price() == null || b.price().signum() < 0 || b.price().scale() > 2
                || b.price().compareTo(new java.math.BigDecimal("999999.99")) > 0 || b.authorId() <= 0)
            throw new IllegalArgumentException("Revise páginas, precio y autor.");
    }

    public BookDetails bookDetails(int id) throws SQLException { return books.details(id); }
    public List<Author> authors() throws SQLException { return books.authors(); }
    public List<LibraryUser> activeUsers() throws SQLException { return loans.users(); }

    public int addBook(BookDetails b) throws SQLException {
        validateBook(b);
        transactions.requireOperationalDatabase();
        return books.insert(b);
    }

    // Validamos los datos de un autor que se registra desde el formulario de libro.
    private void validateAuthor(String name, String nationality) {
        if (name == null || name.isBlank() || name.trim().length() > 120)
            throw new IllegalArgumentException("El nombre del autor es obligatorio (máximo 120 caracteres).");
        if (nationality != null && nationality.trim().length() > 60)
            throw new IllegalArgumentException("La nacionalidad admite hasta 60 caracteres.");
    }

    // Registramos un nuevo autor y su libro sin dejar operaciones parciales.
    public int addBookWithNewAuthor(BookDetails book, String name, String nationality)
            throws SQLException {
        validateBook(book);
        validateAuthor(name, nationality);
        transactions.requireOperationalDatabase();
        return books.insertWithNewAuthor(book, name, nationality);
    }

    // Cambiamos el autor del libro creando su registro dentro de la transacción.
    public void updateBookWithNewAuthor(BookDetails book, String name, String nationality)
            throws SQLException {
        validateBook(book);
        validateAuthor(name, nationality);
        transactions.requireOperationalDatabase();
        books.updateWithNewAuthor(book, name, nationality);
    }

    public void updateBook(BookDetails b) throws SQLException {
        validateBook(b);
        transactions.requireOperationalDatabase();
        books.update(b);
    }

    public void deleteBook(int id) throws SQLException {
        transactions.requireOperationalDatabase();
        books.delete(id);
    }

    public void changeDueDate(int loanId, java.time.LocalDate date) throws SQLException {
        transactions.changeDueDate(loanId, date);
    }

    public void cancelLoan(int loanId) throws SQLException { transactions.cancelLoan(loanId); }
}
