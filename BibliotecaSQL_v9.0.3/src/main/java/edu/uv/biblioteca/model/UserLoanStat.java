package edu.uv.biblioteca.model;
// Contamos préstamos históricos, activos y devueltos por usuario.
public record UserLoanStat(int userId, String user, long totalLoans, long activeLoans, long returnedLoans) { }
