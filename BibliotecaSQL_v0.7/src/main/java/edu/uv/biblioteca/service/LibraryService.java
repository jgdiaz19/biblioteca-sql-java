package edu.uv.biblioteca.service;

import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LabDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.dao.ReportDao;
import edu.uv.biblioteca.model.*;
import java.sql.SQLException;
import java.util.List;

// Reunimos las operaciones que pueden solicitarse desde la interfaz.
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
}
