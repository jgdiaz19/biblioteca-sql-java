package edu.uv.biblioteca.model;
// Mostramos la ubicación del ejemplar: en biblioteca o con un usuario.
public record CategoryBook(int bookId, String title, boolean available, String borrower) { }
