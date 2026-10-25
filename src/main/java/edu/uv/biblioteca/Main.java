package edu.uv.biblioteca;

import java.sql.*;
import java.util.Scanner;

/** Primer módulo ejecutable: JDBC, consultas parametrizadas y navegación en consola. */
public final class Main {
    private static final Scanner TECLADO = new Scanner(System.in);
    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/biblioteca_operativa");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "admin");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "biblioteca_local");

    public static void main(String[] args) {
        try (Connection cn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            while (true) {
                limpiar();
                System.out.println("+------------------------------------------------------------------+");
                System.out.println("|                  BIBLIOTECASQL  |  VERSION 0.1                    |");
                System.out.println("+------------------------------------------------------------------+");
                System.out.println("  1. Consultar catálogo de libros");
                System.out.println("  2. Buscar libros por título");
                System.out.println("  3. Consultar préstamos activos");
                System.out.println("  4. Estadísticas por categoría (GROUP BY)");
                System.out.println("  0. Salir");
                System.out.print("\nSeleccione una opción: ");
                String opcion = TECLADO.nextLine().trim();
                if ("0".equals(opcion)) break;
                limpiar();
                try {
                    switch (opcion) {
                        case "1" -> libros(cn, null);
                        case "2" -> { System.out.print("Texto a buscar: "); libros(cn, TECLADO.nextLine().trim()); }
                        case "3" -> prestamos(cn);
                        case "4" -> estadisticas(cn);
                        default -> System.out.println("Opción no válida.");
                    }
                } catch (SQLException ex) {
                    System.out.println("Error al consultar PostgreSQL. SQLState: " + ex.getSQLState());
                }
                System.out.print("\nPresione ENTER para regresar al menú...");
                TECLADO.nextLine();
            }
        } catch (SQLException ex) {
            System.err.println("No fue posible conectar con PostgreSQL: " + ex.getMessage());
            System.err.println("Compruebe Docker Compose y las variables DB_URL, DB_USER, DB_PASSWORD.");
        }
    }
    private static void limpiar() { System.out.print("\033[H\033[2J"); System.out.flush(); }
    private static void libros(Connection cn, String filtro) throws SQLException {
        System.out.println("CATÁLOGO DE LIBROS\n");
        String sql = "SELECT id_libro,titulo,categoria,disponible FROM libros" +
          (filtro == null ? " ORDER BY id_libro" : " WHERE titulo ILIKE ? ORDER BY titulo");
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (filtro != null) ps.setString(1, "%" + filtro + "%");
            try (ResultSet rs = ps.executeQuery()) {
                System.out.printf("%-4s  %-38s %-23s %s%n", "ID", "TÍTULO", "CATEGORÍA", "DISP.");
                System.out.println("-".repeat(82));
                while (rs.next()) System.out.printf("%-4d  %-38.38s %-23.23s %s%n",
                  rs.getInt("id_libro"),rs.getString("titulo"),rs.getString("categoria"),rs.getBoolean("disponible") ? "Sí" : "No");
            }
        }
    }
    private static void prestamos(Connection cn) throws SQLException {
        System.out.println("PRÉSTAMOS ACTIVOS\n");
        String sql = "SELECT p.id_prestamo,l.titulo,u.nombre,p.fecha_devolucion " +
          "FROM prestamos p JOIN libros l ON l.id_libro=p.id_libro " +
          "JOIN usuarios u ON u.id_usuario=p.id_usuario " +
          "WHERE p.fecha_devolucion_real IS NULL ORDER BY p.fecha_devolucion";
        try (PreparedStatement ps=cn.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {
            System.out.printf("%-4s %-29s %-23s %s%n","ID","LIBRO","USUARIO","LÍMITE");
            System.out.println("-".repeat(75));
            while(rs.next()) System.out.printf("%-4d %-29.29s %-23.23s %s%n",rs.getInt(1),rs.getString(2),rs.getString(3),rs.getDate(4));
        }
    }
    private static void estadisticas(Connection cn) throws SQLException {
        System.out.println("LIBROS POR CATEGORÍA\n");
        String sql="SELECT categoria,COUNT(*) AS total,COUNT(*) FILTER(WHERE disponible) AS disponibles FROM libros GROUP BY categoria ORDER BY categoria";
        try (PreparedStatement ps=cn.prepareStatement(sql);ResultSet rs=ps.executeQuery()) {
            System.out.printf("%-39s %8s %12s%n","CATEGORÍA","TOTAL","DISPONIBLES");
            System.out.println("-".repeat(64));
            while(rs.next())System.out.printf("%-39.39s %8d %12d%n",rs.getString(1),rs.getLong(2),rs.getLong(3));
        }
    }
}
