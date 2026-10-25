package edu.uv.biblioteca.model;
import java.time.LocalDate;
// Conservamos las dos fechas para identificar con claridad los préstamos vencidos.
public record OverdueLoan(int id, String title, String user, LocalDate loanDate, LocalDate dueDate, long daysLate) { }
