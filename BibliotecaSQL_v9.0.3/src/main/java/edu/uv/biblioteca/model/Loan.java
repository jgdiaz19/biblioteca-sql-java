package edu.uv.biblioteca.model;
import java.time.LocalDate;
// Distinguimos cuándo se entregó el libro y cuándo debe devolverse.
public record Loan(int id, String title, String user, LocalDate loanDate, LocalDate dueDate) { }
