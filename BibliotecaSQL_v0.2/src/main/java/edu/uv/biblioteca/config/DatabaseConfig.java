package edu.uv.biblioteca.config;

/** Configuración únicamente local de ejemplo; nunca registre contraseñas reales en Git. */
public record DatabaseConfig(String url, String user, String password) {
    public static DatabaseConfig fromEnvironment() {
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
