package edu.uv.biblioteca.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import org.junit.jupiter.api.Test;

class SqlTraceTest {
    @Test
    void capturesExecutedSqlAndBoundParameter() throws Exception {
        SqlTrace.clear();

        // Simulamos JDBC para comprobar el registro sin necesitar PostgreSQL.
        PreparedStatement statement = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[] {PreparedStatement.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("executeUpdate")) return 1;
                    return null;
                });

        Connection connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[] {Connection.class},
                (proxy, method, args) -> statement);

        try (PreparedStatement prepared = SqlTrace.prepare(connection,
                "UPDATE libros SET disponible = ? WHERE id_libro = ?")) {
            prepared.setBoolean(1, false);
            prepared.setInt(2, 7);
            prepared.executeUpdate();
        }

        assertEquals(1, SqlTrace.snapshot().size());
        assertTrue(SqlTrace.snapshot().get(0).sql().contains("UPDATE libros"));
        assertEquals(java.util.List.of("1 = false", "2 = 7"),
                SqlTrace.snapshot().get(0).parameters());
    }
}
