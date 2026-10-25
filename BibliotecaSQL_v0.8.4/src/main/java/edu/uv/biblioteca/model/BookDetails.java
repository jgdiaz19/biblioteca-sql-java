package edu.uv.biblioteca.model;

import java.math.BigDecimal;

// Reunimos los datos completos que se editan en el formulario de libros.
public record BookDetails(int id, String title, String isbn, int year,
        String category, String publisher, int pages, BigDecimal price,
        boolean available, int authorId) { }
