package edu.uv.biblioteca.ui;

import edu.uv.biblioteca.dao.SqlTrace;

/** Explica cómo se relacionan las consultas SQL con las instrucciones Java. */
public final class LearningGuide {
    private LearningGuide() { }

    public static String javaExplanation(SqlTrace.Entry entry) {
        return "ARCHIVO Y MÉTODO: " + entry.sourceClass() + "." + entry.sourceMethod() + "()\n\n"
            + "1. ORIGEN. La interfaz solicita una operación al servicio. El servicio llama "
            + "al método que aparece en la vista Código Java.\n\n"
            + "2. PREPARACIÓN. SqlTrace.prepare(connection, sql) utiliza "
            + "Connection.prepareStatement(sql) para crear la sentencia JDBC. "
            + "Los signos ? del SQL se sustituyen mediante los métodos setX, si se necesitan.\n\n"
            + "3. EJECUCIÓN. executeQuery() devuelve un ResultSet para una consulta; "
            + "executeUpdate() devuelve la cantidad de filas afectadas para una modificación.\n\n"
            + "4. RESULTADOS. Cuando hay un ResultSet, next() avanza entre las filas "
            + "y getString(), getInt() u otros métodos recuperan las columnas.\n\n"
            + "5. RECURSOS. try-with-resources cierra la sentencia y los resultados "
            + "incluso si PostgreSQL informa un error. En operaciones de préstamo, "
            + "el servicio administra COMMIT y ROLLBACK para mantener la consistencia.\n\n"
            + "PARA ANALIZAR: localice la llamada a SqlTrace.prepare, observe "
            + "dónde se ejecuta la sentencia y siga el valor que devuelve el método.";
    }

    public static String sqlExplanation(SqlTrace.Entry entry, String exerciseExplanation) {
        String general = SqlExplanation.describe(entry.sql());
        if (exerciseExplanation == null || exerciseExplanation.isBlank()) return general;
        return "PROPÓSITO DEL EJERCICIO\n" + exerciseExplanation + "\n\n"
            + "ANÁLISIS DE LA SENTENCIA\n" + general;
    }
}
