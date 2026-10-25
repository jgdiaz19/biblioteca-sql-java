package edu.uv.biblioteca.model;

// Conservamos los campos que pueden modificarse desde el catálogo de autores.
public record AuthorDetails(int id, String name, String nationality) { }
