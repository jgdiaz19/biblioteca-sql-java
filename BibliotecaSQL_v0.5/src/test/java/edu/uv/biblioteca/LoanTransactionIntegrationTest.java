package edu.uv.biblioteca;

import edu.uv.biblioteca.service.LoanTransactionService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;

/** Comprueba préstamos y devoluciones en una base reservada para pruebas. */
@EnabledIfEnvironmentVariable(named = "DB_TEST_URL", matches = "jdbc:postgresql://localhost:55433/biblioteca_operativa")
public class LoanTransactionIntegrationTest {
    private Connection open() throws SQLException {
        // Abrimos únicamente la conexión del contenedor de pruebas.
        return DriverManager.getConnection(System.getenv("DB_TEST_URL"),
            System.getenv().getOrDefault("DB_TEST_USER", "admin"),
            System.getenv().getOrDefault("DB_TEST_PASSWORD", "biblioteca_local"));
    }

    @BeforeEach
    void prepareData() throws SQLException {
        try (Connection connection = open(); Statement statement = connection.createStatement()) {
            // Comprobamos el puerto y la base antes de borrar datos de prueba.
            try (ResultSet r = statement.executeQuery("SELECT current_database(), inet_server_port()")) {
                assertTrue(r.next());
                assertEquals("biblioteca_operativa", r.getString(1));
                assertEquals(5432, r.getInt(2)); // Puerto interno del contenedor PostgreSQL.
            }
            // Restauramos los datos de prueba en una base completamente aislada.
            statement.execute("DROP TRIGGER IF EXISTS prueba_fallo_libro ON libros");
            statement.execute("DROP FUNCTION IF EXISTS fallar_actualizacion_libro()");
            statement.execute("TRUNCATE TABLE prestamos, libros, usuarios, autores RESTART IDENTITY CASCADE");
            statement.execute("INSERT INTO autores(nombre) VALUES ('Autor de prueba')");
            statement.execute("INSERT INTO usuarios(nombre,tipo_usuario,activo) VALUES " +
                "('Usuario activo','Estudiante',TRUE), ('Usuario inactivo','Estudiante',FALSE), " +
                "('Usuario limite','Estudiante',TRUE)");
            for (int i = 1; i <= 7; i++) {
                // Creamos libros independientes para comprobar el límite de préstamos.
                statement.execute("INSERT INTO libros(titulo,isbn,anio_publicacion,categoria,numero_paginas,precio,disponible,id_autor) " +
                    "VALUES ('Libro " + i + "', 'ISBN-TEST-" + i + "', 2026, 'Pruebas', 150, 100, TRUE, 1)");
            }
        }
    }

    @Test
    void registerAndReturnLoan() throws SQLException {
        try (Connection c = open()) {
            LoanTransactionService service = new LoanTransactionService(c);
            int id = service.borrow(1, 1);
            assertTrue(id > 0);
            assertEquals(1, number(c, "SELECT COUNT(*) FROM prestamos WHERE id_libro=1 AND fecha_devolucion_real IS NULL"));
            assertFalse(isAvailable(c, 1));
            service.returnBook(id);
            assertTrue(isAvailable(c, 1));
            assertEquals(0, number(c, "SELECT COUNT(*) FROM prestamos WHERE id_prestamo=" + id + " AND fecha_devolucion_real IS NULL"));
        }
    }

    @Test
    void rejectDuplicateActiveLoanWithoutPartialChanges() throws SQLException {
        try (Connection c = open()) {
            LoanTransactionService service = new LoanTransactionService(c);
            service.borrow(1, 1);
            assertThrows(IllegalArgumentException.class, () -> service.borrow(3, 1));
            assertEquals(1, number(c, "SELECT COUNT(*) FROM prestamos WHERE id_libro=1"));
            assertFalse(isAvailable(c, 1));
            assertTrue(c.getAutoCommit());
        }
    }

    @Test
    void rejectInactiveUserWithoutChangingInventory() throws SQLException {
        try (Connection c = open()) {
            assertThrows(IllegalArgumentException.class, () -> new LoanTransactionService(c).borrow(2, 1));
            assertTrue(isAvailable(c, 1));
            assertEquals(0, number(c, "SELECT COUNT(*) FROM prestamos"));
        }
    }

    @Test
    void enforceThreeActiveLoans() throws SQLException {
        try (Connection c = open()) {
            LoanTransactionService service = new LoanTransactionService(c);
            service.borrow(3, 1);
            service.borrow(3, 2);
            service.borrow(3, 3);
            assertThrows(IllegalArgumentException.class, () -> service.borrow(3, 4));
            assertEquals(3, number(c, "SELECT COUNT(*) FROM prestamos WHERE id_usuario=3"));
            assertTrue(isAvailable(c, 4));
        }
    }

    @Test
    void rejectSecondReturn() throws SQLException {
        try (Connection c = open()) {
            LoanTransactionService service = new LoanTransactionService(c);
            int id = service.borrow(1, 1);
            service.returnBook(id);
            assertThrows(IllegalArgumentException.class, () -> service.returnBook(id));
            assertTrue(isAvailable(c, 1));
        }
    }

    @Test
    void rollbackWhenUpdatingAvailabilityFails() throws SQLException {
        try (Connection c = open()) {
            try (Statement s = c.createStatement()) {
                // Creamos temporalmente un error en la actualización del libro.
                s.execute("CREATE FUNCTION fallar_actualizacion_libro() RETURNS trigger " +
                    "LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'Error de prueba'; END $$");
                s.execute("CREATE TRIGGER prueba_fallo_libro BEFORE UPDATE ON libros " +
                    "FOR EACH ROW EXECUTE FUNCTION fallar_actualizacion_libro()");
            }
            try {
                // Verificamos que el error revierta el INSERT del préstamo.
                assertThrows(SQLException.class, () -> new LoanTransactionService(c).borrow(1, 1));
                assertEquals(0, number(c, "SELECT COUNT(*) FROM prestamos"));
                assertTrue(isAvailable(c, 1));
                assertTrue(c.getAutoCommit());
            } finally {
                try (Statement s = c.createStatement()) {
                    s.execute("DROP TRIGGER IF EXISTS prueba_fallo_libro ON libros");
                    s.execute("DROP FUNCTION IF EXISTS fallar_actualizacion_libro()");
                }
            }
        }
    }

    @Test
    void forbidWritesOnAcademicDatabase() throws SQLException {
        // Abrimos la base académica del mismo contenedor únicamente para probar el bloqueo.
        String url = System.getenv("DB_TEST_URL").replace("/biblioteca_operativa", "/biblioteca_universitaria");
        try (Connection c = DriverManager.getConnection(url,
                System.getenv().getOrDefault("DB_TEST_USER", "admin"),
                System.getenv().getOrDefault("DB_TEST_PASSWORD", "biblioteca_local"))) {
            assertThrows(IllegalArgumentException.class, () -> new LoanTransactionService(c).borrow(1, 1));
        }
    }

    private boolean isAvailable(Connection c, int id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT disponible FROM libros WHERE id_libro=?")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) { assertTrue(r.next()); return r.getBoolean(1); }
        }
    }
    private int number(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            assertTrue(r.next()); return r.getInt(1);
        }
    }
}
