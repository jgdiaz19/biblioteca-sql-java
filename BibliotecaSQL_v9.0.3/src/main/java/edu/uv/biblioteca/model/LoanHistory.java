package edu.uv.biblioteca.model;
import java.time.LocalDate;
// Fecha límite y devolución real son datos diferentes.
public record LoanHistory(int id, int userId, String user, int bookId, String title,
                          LocalDate loanDate, LocalDate dueDate, LocalDate returnedDate) { }
