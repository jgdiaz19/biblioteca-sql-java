package edu.uv.biblioteca.config;

// Guardamos los datos necesarios para conectarnos a PostgreSQL.
public record DatabaseConfig(String url, String user, String password) {
    public static DatabaseConfig fromEnvironment() {
        // Tomamos las variables configuradas y usamos valores locales cuando no existen.
        return new DatabaseConfig(
            get("DB_URL", "jdbc:postgresql://localhost:5432/biblioteca_operativa"),
            get("DB_USER", "admin"),
            get("DB_PASSWORD", "biblioteca_local"));
    }
    private static String get(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
