-- Migramos únicamente la base operativa. No ejecutar sobre biblioteca_universitaria.
-- La transacción evita dejar libros a medio migrar si falla alguna sentencia.
BEGIN;
DO $$
BEGIN
    IF current_database() <> 'biblioteca_operativa' THEN
        RAISE EXCEPTION 'Esta migración solo se permite en biblioteca_operativa';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS categorias (
    id_categoria INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre),
    CONSTRAINT ck_categoria_nombre CHECK (length(btrim(nombre)) > 0 AND nombre = btrim(nombre))
);
CREATE TABLE IF NOT EXISTS editoriales (
    id_editorial INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    CONSTRAINT uq_editorial_nombre UNIQUE (nombre),
    CONSTRAINT ck_editorial_nombre CHECK (length(btrim(nombre)) > 0 AND nombre = btrim(nombre))
);
-- Los índices impiden duplicados que solo difieren en mayúsculas/minúsculas.
CREATE UNIQUE INDEX IF NOT EXISTS uq_categorias_normalizado ON categorias (lower(nombre));
CREATE UNIQUE INDEX IF NOT EXISTS uq_editoriales_normalizado ON editoriales (lower(nombre));

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = 'public' AND table_name = 'libros' AND column_name = 'categoria') THEN
        INSERT INTO categorias (nombre)
        SELECT MIN(btrim(categoria))
        FROM libros GROUP BY lower(btrim(categoria))
        ON CONFLICT DO NOTHING;
        INSERT INTO editoriales (nombre)
        SELECT MIN(btrim(editorial))
        FROM libros WHERE nullif(btrim(editorial), '') IS NOT NULL
        GROUP BY lower(btrim(editorial))
        ON CONFLICT DO NOTHING;
        ALTER TABLE libros ADD COLUMN IF NOT EXISTS id_categoria INTEGER;
        ALTER TABLE libros ADD COLUMN IF NOT EXISTS id_editorial INTEGER;
        UPDATE libros l SET id_categoria = c.id_categoria
          FROM categorias c WHERE lower(btrim(l.categoria)) = lower(c.nombre);
        UPDATE libros l SET id_editorial = e.id_editorial
          FROM editoriales e WHERE lower(btrim(l.editorial)) = lower(e.nombre);
        IF EXISTS (SELECT 1 FROM libros WHERE id_categoria IS NULL) THEN
            RAISE EXCEPTION 'Existen libros con categoría sin correspondencia';
        END IF;
        ALTER TABLE libros ALTER COLUMN id_categoria SET NOT NULL;
        ALTER TABLE libros ADD CONSTRAINT fk_libro_categoria FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria);
        ALTER TABLE libros ADD CONSTRAINT fk_libro_editorial FOREIGN KEY (id_editorial) REFERENCES editoriales(id_editorial);
        ALTER TABLE libros DROP COLUMN categoria;
        ALTER TABLE libros DROP COLUMN editorial;
    END IF;
END $$;

-- La vista devuelve nombres legibles sin duplicar información en libros.
CREATE OR REPLACE VIEW libros_consulta AS
SELECT l.id_libro, l.titulo, l.isbn, l.anio_publicacion,
       c.nombre AS categoria, e.nombre AS editorial,
       l.numero_paginas, l.precio, l.disponible, l.id_autor,
       l.id_categoria, l.id_editorial
FROM libros l
JOIN categorias c ON c.id_categoria = l.id_categoria
LEFT JOIN editoriales e ON e.id_editorial = l.id_editorial;
COMMIT;
