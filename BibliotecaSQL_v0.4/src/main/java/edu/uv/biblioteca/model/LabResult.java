package edu.uv.biblioteca.model;
import java.util.List;
// Representamos una consulta didáctica junto con sus columnas y filas.
public record LabResult(String title, String sql, String explanation, List<String> columns, List<List<String>> rows) { }
