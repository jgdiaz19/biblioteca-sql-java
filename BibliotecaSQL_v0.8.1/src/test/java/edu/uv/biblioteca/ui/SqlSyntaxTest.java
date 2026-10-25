package edu.uv.biblioteca.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SqlSyntaxTest {
    @Test void identificaElementos() {
        var parts = SqlSyntax.tokenize("SELECT COUNT(*) FROM libros WHERE titulo = 'SQL' -- nota");
        assertTrue(parts.stream().anyMatch(p -> p.kind() == SqlSyntax.Kind.KEYWORD && p.text().equals("SELECT")));
        assertTrue(parts.stream().anyMatch(p -> p.kind() == SqlSyntax.Kind.FUNCTION && p.text().equals("COUNT")));
        assertTrue(parts.stream().anyMatch(p -> p.kind() == SqlSyntax.Kind.STRING && p.text().equals("'SQL'")));
        assertTrue(parts.stream().anyMatch(p -> p.kind() == SqlSyntax.Kind.COMMENT));
    }
    @Test void explicaConsultasFueraDelLaboratorio() {
        String explanation = SqlExplanation.describe("SELECT titulo FROM libros WHERE titulo ILIKE ?");
        assertTrue(explanation.contains("SELECT"));
        assertTrue(explanation.contains("ILIKE"));
        assertTrue(explanation.contains("PreparedStatement"));
    }
}
