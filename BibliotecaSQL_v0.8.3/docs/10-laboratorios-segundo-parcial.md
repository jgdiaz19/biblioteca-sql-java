# Laboratorios SQL del segundo parcial

Se incluyen siete consultas para cada bloque: **A7, A8 y A9**. Los ejemplos usan exclusivamente el esquema original de cuatro tablas: `autores`, `libros`, `usuarios` y `prestamos`.

| Bloque | Contenidos incluidos | Clase |
| --- | --- | --- |
| A7 | Funciones de texto y fecha, agregados, agrupamiento, HAVING y generate_series | `dao/LabDao.java` |
| A8 | INNER/LEFT/RIGHT JOIN, combinaciones de tres tablas y agrupamiento | `dao/LabDao.java` |
| A9 | Subconsultas escalares, IN, EXISTS, NOT EXISTS, ALL, derivadas y correlacionadas | `dao/LabDao.java` |

**Alcance:** es una cobertura temática de los tres bloques, no una afirmación de correspondencia numérica uno a uno con los ejercicios oficiales de A8 y A9. Para conseguir esa correspondencia se deben contrastar los enunciados originales de ambos cuadernos.

## Ruta sugerida de aprendizaje

1. Abrir una consulta del laboratorio y observar el SQL.
2. Predecir las columnas y las filas esperadas.
3. Ejecutarla y comparar los resultados.
4. Localizar `LabDao` y seguir la llamada desde `MenuController` y `LibraryService`.
5. Modificar una consulta en una rama local y explicar el cambio producido.

Las operaciones de préstamo y devolución permanecen separadas de las consultas de solo lectura.
