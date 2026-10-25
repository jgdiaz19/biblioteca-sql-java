package edu.uv.biblioteca.model;
import java.time.LocalDate;
// Agrupamos los datos que necesita el reporte de préstamos vencidos.
public record OverdueLoan(int id, String title, String user, LocalDate dueDate, long daysLate) { }
