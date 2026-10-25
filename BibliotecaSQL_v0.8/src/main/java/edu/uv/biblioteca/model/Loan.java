package edu.uv.biblioteca.model;
import java.time.LocalDate;
// Almacenamos los campos que devuelve la consulta correspondiente.
public record Loan(int id, String title, String user, LocalDate dueDate) { }
