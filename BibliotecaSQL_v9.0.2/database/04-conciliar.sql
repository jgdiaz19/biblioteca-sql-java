-- Solo base operativa: reflejar préstamo activo en disponibilidad.
UPDATE libros l SET disponible = NOT EXISTS (
 SELECT 1 FROM prestamos p WHERE p.id_libro=l.id_libro AND p.fecha_devolucion_real IS NULL
);
-- Solo puede existir un préstamo activo por libro.
CREATE UNIQUE INDEX uq_prestamo_activo_libro ON prestamos(id_libro) WHERE fecha_devolucion_real IS NULL;
