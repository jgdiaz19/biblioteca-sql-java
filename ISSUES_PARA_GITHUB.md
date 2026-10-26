# Registro retrospectivo de tareas

Este archivo contiene una propuesta de **13 tareas históricas** derivadas de las versiones conservadas de BibliotecaSQL. Puede utilizarse para documentar el desarrollo mediante GitHub Issues. **No debe interpretarse como una lista de incidencias ya creadas en GitHub**.

Cada tarea recoge el propósito del cambio y las versiones en que se incorporó. El hecho de que una función figure en una versión no acredita por sí solo que se hayan ejecutado todas las pruebas de integración.

| Nº | Tarea | Versiones relacionadas | Criterio de revisión |
|---|---|---|---|
| 01 | Configuración inicial de Java y PostgreSQL | v0.1–v0.2 | Conexión JDBC, Maven y separación de capas |
| 02 | Préstamos y devoluciones transaccionales | v0.3–v0.4 | Validación de disponibilidad y reversión de cambios |
| 03 | Pruebas e inicialización de Docker | v0.5–v0.5.1 | Pruebas JUnit y corrección de arranque |
| 04 | Interfaz institucional y terminal clásica | v0.6–v0.7 | Menús, splash y compatibilidad de teclado |
| 05 | Búsqueda incremental de libros | v0.7 | Comparación de filtrado Java y PostgreSQL |
| 06 | Visor didáctico de SQL y Java | v0.8–v0.8.2 | Trazas JDBC, fragmentos de código y explicaciones |
| 07 | Desplazamiento de tablas y visor | v0.8.3–v0.8.4 | Selección visible y navegación por contenido extenso |
| 08 | Operaciones de gestión de libros y préstamos | v0.8.4 | Formularios, validaciones y confirmaciones |
| 09 | Administración de autores | v0.8.5–v0.8.6 | Alta integrada y edición del catálogo |
| 10 | Normalización de categorías y editoriales | v9.0–v9.0.1 | Claves foráneas, vista y migración |
| 11 | Reportes y plazos de préstamo | v9.0.2–v9.0.3 | Tablas, fechas, historial y devoluciones |
| 12 | Ampliación del laboratorio SQL | v9.0.4 | 36 consultas y mecanismo de auditoría |
| 13 | Identidad de la pantalla de inicio | v9.0.5 | Textos institucionales, créditos y versión |

## Recomendaciones para registrar una tarea

La descripción de cada Issue debe incluir el **problema observado**, el **cambio previsto**, la **versión donde aparece** y la **evidencia disponible** (commit, comparación de etiquetas o prueba). Utilice una categoría como `mejora`, `corrección`, `documentación` o `pruebas` según corresponda.

Las tareas de este documento son retrospectivas. Al incorporarlas al tablero, márquelas como **implementadas históricamente** cuando la evidencia sea el código conservado; deje las validaciones pendientes como tareas abiertas. Esto evita confundir implementación con verificación satisfactoria.

Para consultar las fechas y el alcance de cada versión, utilice [HISTORIAL.md](HISTORIAL.md). El diseño del tablero se describe en [PROJECTS_PLAN.md](PROJECTS_PLAN.md).
