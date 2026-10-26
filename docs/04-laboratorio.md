# Laboratorio SQL del segundo parcial

La opción **9. Laboratorios SQL** reúne **36 consultas** de lectura que permiten aplicar los contenidos de las actividades A7, A8 y A9. Cada actividad contiene 12 ejemplos. Las sentencias se encuentran en [`LabDao.java`](../src/main/java/edu/uv/biblioteca/dao/LabDao.java).

El propósito del laboratorio es analizar una consulta completa: su objetivo, las tablas que intervienen, las condiciones utilizadas y la interpretación de sus resultados. Los ejercicios se agrupan por **contenido temático**; su numeración no debe considerarse una reproducción literal de los enunciados de los cuadernos originales.

## A7. Funciones y procesamiento de conjuntos

| Ejercicio | Consulta | Concepto principal |
|---|---|---|
| A7-01 | Texto: UPPER y LENGTH | Transformación de cadenas |
| A7-02 | Texto: TRIM y CONCAT | Limpieza y concatenación |
| A7-03 | Fechas: EXTRACT | Componentes de una fecha |
| A7-04 | Agregación por categoría | `COUNT`, `AVG` y `GROUP BY` |
| A7-05 | Filtrado con HAVING | Condiciones sobre grupos |
| A7-06 | Serie de valores | `generate_series()` |
| A7-07 | Resumen de precios | Funciones de agregación |
| A7-08 | COALESCE: editoriales no registradas | Tratamiento de nulos |
| A7-09 | CASE: estado de disponibilidad | Expresiones condicionales |
| A7-10 | ROUND: precios con incremento | Operaciones numéricas |
| A7-11 | Conteo de disponibles por categoría | Agregación condicional |
| A7-12 | Días entre préstamo y fecha límite | Operaciones con fechas |

## A8. Consultas entre tablas

| Ejercicio | Consulta | Concepto principal |
|---|---|---|
| A8-01 | INNER JOIN: libro y autor | Relación entre dos tablas |
| A8-02 | JOIN: historial de préstamos | Relaciones múltiples |
| A8-03 | LEFT JOIN: autores sin libros | Filas sin correspondencia |
| A8-04 | LEFT JOIN: usuarios y préstamos | Conservación de usuarios |
| A8-05 | RIGHT JOIN: autores y libros | Conservación del lado derecho |
| A8-06 | JOIN y agregación | Agrupación sobre relaciones |
| A8-07 | JOIN: préstamos sin devolver | Relaciones y filtrado |
| A8-08 | JOIN: libros y categorías normalizadas | Uso de claves foráneas |
| A8-09 | LEFT JOIN: categorías sin libros | Categorías sin registros asociados |
| A8-10 | LEFT JOIN: libros y editoriales opcionales | Relación opcional |
| A8-11 | FULL JOIN: autores y libros | Conservación de ambos lados |
| A8-12 | JOIN: préstamos vencidos | Relaciones y condiciones de fecha |

## A9. Subconsultas

| Ejercicio | Consulta | Concepto principal |
|---|---|---|
| A9-01 | Subconsulta escalar: precio medio | Comparación con un agregado |
| A9-02 | IN: autores con libros | Pertenencia |
| A9-03 | NOT EXISTS: libros sin historial | Ausencia de registros históricos |
| A9-04 | EXISTS: usuarios con préstamos | Existencia de registros |
| A9-05 | ALL: libros más caros | Comparación universal |
| A9-06 | Subconsulta en FROM | Tabla derivada |
| A9-07 | Correlacionada: precio frente a categoría | Referencia a fila externa |
| A9-08 | ANY: precio superior a algún libro | Comparación existencial |
| A9-09 | NOT EXISTS: usuarios sin préstamos activos | Ausencia de préstamos abiertos |
| A9-10 | IN: libros de categorías con dos o más títulos | Subconsulta agrupada |
| A9-11 | Subconsulta escalar: préstamos del usuario | Agregación en subconsulta |
| A9-12 | EXISTS: categorías con libros disponibles | Existencia correlacionada |

## Procedimiento de análisis

Se recomienda estudiar cada ejercicio con la siguiente secuencia:

1. **Interpretación.** Formule con sus propias palabras qué información debe producir la consulta.
2. **Estructura.** Identifique tablas, columnas, condiciones, agrupamientos y ordenamientos.
3. **Predicción.** Anticipe qué registros deberían aparecer; considere los posibles valores `NULL`.
4. **Ejecución.** Consulte el resultado en BibliotecaSQL y confróntelo con la predicción.
5. **Implementación Java.** Localice la sentencia en `LabDao.java` y examine cómo se ejecuta con `PreparedStatement` y cómo se procesan los registros de `ResultSet`.

El modo didáctico permite consultar el SQL y un fragmento del código Java empleado; para ver el método completo utilice el editor de código.

## Aspectos que requieren atención

Una consulta sobre **préstamos históricos** no necesariamente devuelve lo mismo que una sobre **préstamos activos**. En este modelo, `fecha_devolucion` es la fecha límite, mientras que `fecha_devolucion_real` registra la devolución efectiva.

También debe analizarse la posición de los filtros cuando se utiliza `LEFT JOIN`: una condición en `WHERE` puede descartar filas sin correspondencia que un `LEFT JOIN` pretendía conservar. Las consultas que utilizan `CURRENT_DATE` cambian conforme avanza el tiempo, aun cuando nadie modifique los registros.

## Auditoría de ejecución

Desde la raíz del repositorio, con Docker y Maven disponibles:

```bash
bash scripts/auditar-laboratorio.sh
```

El script ejecuta las 36 consultas contra `biblioteca_operativa` utilizando una conexión configurada como de solo lectura. **Que todas las sentencias se ejecuten sin excepciones no implica que sus resultados sean correctos para cualquier interpretación del problema.** La revisión lógica sigue siendo necesaria.

Consulte la [guía de pruebas](06-pruebas-y-mantenimiento.md) para distinguir entre compilación, auditoría y validación de resultados.
