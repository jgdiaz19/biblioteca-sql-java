# Laboratorio SQL: A7, A8 y A9

**Versión:** BibliotecaSQL v9.0.5. Abra la opción **9. Laboratorios SQL** del menú principal.

El laboratorio ofrece **36 consultas de solo lectura**, 12 por actividad. Cada ejercicio permite ver resultados y, con el modo didáctico activo, estudiar SQL, código Java y explicaciones. Las consultas residen en `src/main/java/edu/uv/biblioteca/dao/LabDao.java`.

## A7. Funciones y procesamiento de conjuntos

| Ejercicio | Tema | Qué conviene observar |
|---|---|---|
| A7-01 | Texto: UPPER y LENGTH | Mayúsculas y longitud de cadenas |
| A7-02 | Texto: TRIM y CONCAT | Limpieza y concatenación |
| A7-03 | Fechas: EXTRACT | Extracción de componentes |
| A7-04 | Agregación por categoría | `COUNT`, `AVG`, `GROUP BY` |
| A7-05 | Filtrado con HAVING | Restricciones a grupos |
| A7-06 | Serie de valores | `generate_series()` |
| A7-07 | Resumen de precios | Agregados numéricos |
| A7-08 | COALESCE: editoriales no registradas | Tratamiento de `NULL` |
| A7-09 | CASE: estado de disponibilidad | Expresiones condicionales |
| A7-10 | ROUND: precios con incremento | Cálculos y redondeo |
| A7-11 | Conteo de disponibles por categoría | Agregación condicional |
| A7-12 | Días entre préstamo y fecha límite | Diferencias temporales |

## A8. Relaciones entre tablas

| Ejercicio | Tema | Qué conviene observar |
|---|---|---|
| A8-01 | INNER JOIN: libro y autor | Claves foráneas |
| A8-02 | JOIN: historial de préstamos | Unir varias tablas |
| A8-03 | LEFT JOIN: autores sin libros | Filas sin correspondencia |
| A8-04 | LEFT JOIN: usuarios y préstamos | Inclusión de usuarios sin préstamos |
| A8-05 | RIGHT JOIN: autores y libros | Preservación del lado derecho |
| A8-06 | JOIN y agregación | Grupos sobre relaciones |
| A8-07 | JOIN: préstamos sin devolver | Filtrar préstamos abiertos |
| A8-08 | JOIN: libros y categorías normalizadas | Relación con categorías |
| A8-09 | LEFT JOIN: categorías sin libros | Ausencia de libros |
| A8-10 | LEFT JOIN: libros y editoriales opcionales | Relación opcional |
| A8-11 | FULL JOIN: autores y libros | Filas de ambos lados |
| A8-12 | JOIN: préstamos vencidos | Relaciones y fechas |

## A9. Subconsultas

| Ejercicio | Tema | Qué conviene observar |
|---|---|---|
| A9-01 | Subconsulta escalar: precio medio | Comparar con un agregado |
| A9-02 | IN: autores con libros | Pertenencia |
| A9-03 | NOT EXISTS: libros sin historial | Ausencia de préstamos anteriores |
| A9-04 | EXISTS: usuarios con préstamos | Existencia de relaciones |
| A9-05 | ALL: libros más caros | Comparación con todos |
| A9-06 | Subconsulta en FROM | Tabla derivada |
| A9-07 | Correlacionada: precio frente a categoría | Referencia a fila externa |
| A9-08 | ANY: precio superior a algún libro | Comparación con alguno |
| A9-09 | NOT EXISTS: usuarios sin préstamos activos | Ausencia de préstamos abiertos |
| A9-10 | IN: libros de categorías con dos o más títulos | Subconsulta con agrupación |
| A9-11 | Subconsulta escalar: préstamos del usuario | Agregación correlacionada |
| A9-12 | EXISTS: categorías con libros disponibles | Existencia correlacionada |

## Procedimiento para estudiar cada ejemplo

1. Elija el bloque y seleccione un ejercicio con las flechas y `ENTER`.
2. Identifique las columnas, las tablas, las condiciones y las relaciones antes de analizar la salida.
3. En el modo didáctico, vea primero el **SQL**, después el **fragmento Java** y sus explicaciones.
4. Busque la sentencia en `LabDao.java`. Allí se prepara la consulta y se recorre el `ResultSet`; los encabezados se obtienen con `ResultSetMetaData`.
5. Compare el resultado con los datos de PostgreSQL. Si modifica la consulta, trabaje en una rama propia y vuelva a ejecutar las pruebas.

### Distinciones conceptuales importantes

- `NOT EXISTS` sobre **todo el historial** no significa lo mismo que sobre los **préstamos activos**.
- `fecha_devolucion` es la fecha límite y `fecha_devolucion_real` es la fecha efectiva.
- En `LEFT JOIN`, pasar una condición de `ON` a `WHERE` puede excluir filas que debían preservarse.
- `CURRENT_DATE` depende del momento de ejecución; las cifras también pueden cambiar si se modificó la base operativa.
- Una sentencia que se ejecuta sin errores no necesariamente responde correctamente al enunciado.

El laboratorio **cubre temas** de A7, A8 y A9; no se afirma una equivalencia uno a uno con la numeración de los ejercicios originales sin comparar los cuadernos definitivos.

## Auditar las 36 consultas

Desde la raíz del proyecto, con Java, Maven y Docker disponibles:

```bash
bash scripts/auditar-laboratorio.sh
```

El script inicia PostgreSQL si es necesario y llama a `LaboratoryAudit`, que recorre las consultas contra `biblioteca_operativa` con conexión de solo lectura.

**La auditoría de ejecución detecta errores de PostgreSQL, no valida automáticamente la interpretación de todas las salidas.** Revise además valores nulos, cardinalidades, fechas y filtros.
