package edu.uv.biblioteca.dao;

import edu.uv.biblioteca.model.CatalogEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Administramos categorías y editoriales sin mezclar SQL con los menús.
public final class CatalogDao {
    public enum Type {
        CATEGORY("categorias", "id_categoria"),
        PUBLISHER("editoriales", "id_editorial");
        final String table;
        final String key;
        Type(String table, String key) { this.table = table; this.key = key; }
    }

    private final Connection connection;
    public CatalogDao(Connection connection) { this.connection = connection; }

    public List<CatalogEntry> list(Type type) throws SQLException {
        List<CatalogEntry> result = new ArrayList<>();
        String sql = "SELECT " + type.key + ", nombre FROM " + type.table + " ORDER BY nombre";
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) result.add(new CatalogEntry(rs.getInt(1), rs.getString(2)));
        }
        return result;
    }

    public int insert(Type type, String name) throws SQLException {
        String sql = "INSERT INTO " + type.table + " (nombre) VALUES (?) RETURNING " + type.key;
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se pudo crear el registro.");
                return rs.getInt(1);
            }
        }
    }

    public void update(Type type, int id, String name) throws SQLException {
        String sql = "UPDATE " + type.table + " SET nombre = ? WHERE " + type.key + " = ?";
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, id);
            if (stmt.executeUpdate() != 1) throw new IllegalArgumentException("El registro no existe.");
        }
    }

    public void delete(Type type, int id) throws SQLException {
        String sql = "DELETE FROM " + type.table + " WHERE " + type.key + " = ?";
        try (PreparedStatement stmt = SqlTrace.prepare(connection, sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() != 1) throw new IllegalArgumentException("El registro no existe.");
        }
    }
}
