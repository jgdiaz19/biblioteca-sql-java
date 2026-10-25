package edu.uv.biblioteca.model;
// Almacenamos los campos que devuelve la consulta correspondiente.
public record Book(int id, String title, String category, boolean available) { }
