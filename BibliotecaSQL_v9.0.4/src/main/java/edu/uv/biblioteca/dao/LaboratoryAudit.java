package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.config.DatabaseConfig;
import edu.uv.biblioteca.model.LabResult;
import java.sql.Connection;
import java.sql.DriverManager;

/** Comprueba que todas las consultas se ejecuten en la base operativa. */
public final class LaboratoryAudit {
    private LaboratoryAudit() { }

    public static void main(String[] args) throws Exception {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();
        int errors = 0;

        // Abrimos una conexión de lectura para no cambiar los datos de los estudiantes.
        try (Connection connection = DriverManager.getConnection(
                config.url(), config.user(), config.password())) {
            connection.setReadOnly(true);
            if (!"biblioteca_operativa".equals(connection.getCatalog())) {
                throw new IllegalStateException("La auditoría requiere biblioteca_operativa.");
            }
            LabDao lab = new LabDao(connection);
            for (int activity = 7; activity <= 9; activity++) {
                int count = lab.titles(activity).size();
                for (int number = 1; number <= count; number++) {
                    try {
                        LabResult result = lab.run(activity, number);
                        System.out.printf("OK A%d-%02d: %s (%d filas)%n",
                            activity, number, result.title(), result.rows().size());
                    } catch (Exception error) {
                        errors++;
                        System.err.printf("ERROR A%d-%02d: %s%n",
                            activity, number, error.getMessage());
                    }
                }
            }
        }
        if (errors > 0) throw new IllegalStateException(errors + " consultas fallaron.");
        System.out.println("Se ejecutaron las 36 consultas sin errores de PostgreSQL.");
    }
}
