package edu.uv.biblioteca.model;
// Guardamos el total de préstamos abiertos por cada usuario.
public record UserLoanStat(int userId, String user, long activeLoans) { }
