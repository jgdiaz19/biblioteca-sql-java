# Laboratorio SQL — catálogo y revisión de v9.0.4

Se amplía el conjunto de 21 a **36 consultas** (12 por actividad), sin ejecutar sentencias de escritura.
La base operativa mantiene las tablas normalizadas `libros`, `autores`, `usuarios`,
`prestamos`, `categorias` y `editoriales`, y la vista `libros_consulta`.

## Matriz de consultas

| Ejercicio | Nombre | Tema |
|---|---|---|
| A7-01 | Texto: UPPER y LENGTH | Funciones |
| A7-02 | Texto: TRIM y CONCAT | Funciones |
| A7-03 | Fechas: EXTRACT | Funciones |
| A7-04 | Agregación por categoría | Funciones |
| A7-05 | Filtrado con HAVING | Funciones |
| A7-06 | Serie de valores | Funciones |
| A7-07 | Resumen de precios | Funciones |
| A7-08 | COALESCE: editoriales no registradas | Funciones |
| A7-09 | CASE: estado de disponibilidad | Funciones |
| A7-10 | ROUND: precios con incremento | Funciones |
| A7-11 | Conteo de disponibles por categoría | Funciones |
| A7-12 | Días entre préstamo y fecha límite | Funciones |
| A8-01 | INNER JOIN: libro y autor | JOIN |
| A8-02 | JOIN: historial de préstamos | JOIN |
| A8-03 | LEFT JOIN: autores sin libros | JOIN |
| A8-04 | LEFT JOIN: usuarios y préstamos | JOIN |
| A8-05 | RIGHT JOIN: autores y libros | JOIN |
| A8-06 | JOIN y agregación | JOIN |
| A8-07 | JOIN: préstamos sin devolver | JOIN |
| A8-08 | JOIN: libros y categorías normalizadas | JOIN |
| A8-09 | LEFT JOIN: categorías sin libros | JOIN |
| A8-10 | LEFT JOIN: libros y editoriales opcionales | JOIN |
| A8-11 | FULL JOIN: autores y libros | JOIN |
| A8-12 | JOIN: préstamos vencidos | JOIN |
| A9-01 | Subconsulta escalar: precio medio | Subconsulta |
| A9-02 | IN: autores con libros | Subconsulta |
| A9-03 | NOT EXISTS: libros sin historial | Subconsulta |
| A9-04 | EXISTS: usuarios con préstamos | Subconsulta |
| A9-05 | ALL: libros más caros | Subconsulta |
| A9-06 | Subconsulta en FROM | Subconsulta |
| A9-07 | Correlacionada: precio frente a categoría | Subconsulta |
| A9-08 | ANY: precio superior a algún libro | Subconsulta |
| A9-09 | NOT EXISTS: usuarios sin préstamos activos | Subconsulta |
| A9-10 | IN: libros de categorías con dos o más títulos | Subconsulta |
| A9-11 | Subconsulta escalar: préstamos del usuario | Subconsulta |
| A9-12 | EXISTS: categorías con libros disponibles | Subconsulta |

## Criterios de revisión

- Se revisaron nombres de tablas y columnas contra `02-esquema.sql` y `05-normalizar-catalogos.sql`.
- El laboratorio no incluye INSERT, UPDATE, DELETE, DDL ni acciones de cambio.
- La condición **préstamo activo** es `fecha_devolucion_real IS NULL`.
- Una fecha límite anterior a `CURRENT_DATE` indica **vencimiento** solo si no se devolvió el libro.
- Un libro corresponde a una fila en `libros`: los totales por categoría cuentan libros, no copias adicionales.
- Los LEFT JOIN preservan registros sin correspondencia; COUNT(columnas del lado derecho) evita contar filas nulas.
- Las explicaciones distinguen entre `fecha_devolucion` (fecha límite) y `fecha_devolucion_real`.
- La consulta ANY sobre una categoría específica podría devolver cero filas si no hay libros de esa categoría.
- La consulta ALL compara precios solo cuando los valores son no nulos, ya que `precio` es NOT NULL.
- Los resultados dependen de los cambios realizados por el usuario en `biblioteca_operativa` y de la fecha actual.

## Prueba reproducible en el equipo docente

```bash
mvn test
bash scripts/auditar-laboratorio.sh
mvn compile exec:java
```

La auditoría de integración ejecuta todas las consultas en una transacción de solo lectura
contra la base operativa del proyecto, sin aplicar cambios. Requiere Docker en funcionamiento.
La compilación sin PostgreSQL **no demuestra** que todas las consultas produzcan el resultado lógico esperado;
es importante revisar los resultados obtenidos y contrastarlos con los datos vigentes.
