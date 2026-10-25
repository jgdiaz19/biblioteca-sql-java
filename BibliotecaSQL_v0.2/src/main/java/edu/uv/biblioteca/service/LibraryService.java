package edu.uv.biblioteca.service;

import edu.uv.biblioteca.dao.BookDao;
import edu.uv.biblioteca.dao.LoanDao;
import edu.uv.biblioteca.model.Book;
import edu.uv.biblioteca.model.Loan;
import edu.uv.biblioteca.model.CategoryStat;
import java.sql.SQLException;
import java.util.List;

/** Capa de servicios: punto único para validaciones y futuras reglas de negocio. */
public final class LibraryService {
    private final BookDao books;
    private final LoanDao loans;
    public LibraryService(BookDao books, LoanDao loans) { this.books = books; this.loans = loans; }
    public List<Book> listBooks() throws SQLException { return books.list(); }
    public List<Book> searchBooks(String term) throws SQLException {
        if (term == null || term.isBlank()) throw new IllegalArgumentException("Escriba un título para buscar.");
        if (term.length() > 120) throw new IllegalArgumentException("La búsqueda es demasiado larga.");
        return books.search(term.trim());
    }
    public List<Loan> activeLoans() throws SQLException { return loans.active(); }
    public List<CategoryStat> statistics() throws SQLException { return books.stats(); }
}
