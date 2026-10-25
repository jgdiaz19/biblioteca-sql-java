package edu.uv.biblioteca.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Conserva las sentencias ejecutadas para poder estudiarlas desde la consola. */
public final class SqlTrace {
    private static final int MAX_ENTRIES = 40;
    private static final List<Entry> HISTORY = new ArrayList<>();

    private SqlTrace() { }

    public record Entry(String sql, List<String> parameters, String sourceClass, int sourceLine, String sourceMethod) { }

    // Devolvemos una copia para que la interfaz no modifique el historial.
    public static synchronized List<Entry> snapshot() {
        return List.copyOf(HISTORY);
    }

    // Iniciamos un nuevo recorrido antes de entrar en una operación de la interfaz.
    public static synchronized void clear() {
        HISTORY.clear();
    }

    // Registramos solamente las sentencias que llegan al método execute de JDBC.
    private static synchronized void add(String sql, Map<Integer, String> values, StackTraceElement origin) {
        List<String> parameters = new ArrayList<>();
        values.forEach((index, value) -> parameters.add(index + " = " + value));
        HISTORY.add(new Entry(sql.strip(), List.copyOf(parameters),
            origin.getClassName(), origin.getLineNumber(), origin.getMethodName()));
        if (HISTORY.size() > MAX_ENTRIES) {
            HISTORY.remove(0);
        }
    }

    /** Prepara la sentencia y observa su ejecución, sin cambiar el SQL enviado a PostgreSQL. */
    public static PreparedStatement prepare(Connection connection, String sql) throws SQLException {
        PreparedStatement actual = connection.prepareStatement(sql);
        Map<Integer, String> values = new TreeMap<>();
        // Identificamos el método Java que preparó esta consulta.
        StackTraceElement origin = StackWalker.getInstance().walk(frames -> frames
            .filter(frame -> !frame.getClassName().equals(SqlTrace.class.getName()))
            .findFirst()
            .map(StackWalker.StackFrame::toStackTraceElement)
            .orElse(new StackTraceElement("", "", "", -1)));

        // Interceptamos los métodos setX para recordar los parámetros de búsqueda.
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[] {PreparedStatement.class},
                (proxy, method, arguments) -> {
                    String name = method.getName();
                    if (arguments != null && arguments.length >= 2
                            && name.startsWith("set") && arguments[0] instanceof Integer index) {
                        // No mostramos datos binarios ni cadenas demasiado extensas.
                        String value = String.valueOf(arguments[1]);
                        values.put(index, value.length() > 120 ? value.substring(0, 120) + "…" : value);
                    }
                    if (name.equals("clearParameters")) {
                        values.clear();
                    }
                    if (name.equals("executeQuery") || name.equals("executeUpdate")
                            || name.equals("executeLargeUpdate") || name.equals("execute")) {
                        add(sql, values, origin);
                    }
                    try {
                        return method.invoke(actual, arguments);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                });
    }
}
