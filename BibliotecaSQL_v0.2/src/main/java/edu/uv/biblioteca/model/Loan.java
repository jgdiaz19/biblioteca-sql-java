package edu.uv.biblioteca.model;
import java.time.LocalDate;
public record Loan(int id, String title, String user, LocalDate dueDate) { }
