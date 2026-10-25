# Plan de trabajo retrospectivo

Registro preparado a partir de la evolución del proyecto. **Estos elementos no son issues de GitHub todavía**; pueden publicarse como tareas cerradas una vez revisados.

Estado sugerido: **Completado históricamente (pendiente de comprobar en CI)**.

| Tarea | Versiones | Resumen |
|---|---|---|
| Infraestructura Java + PostgreSQL | v0.1–v0.2 | Conexión JDBC, Maven, datos iniciales, menú y separación de capas. |
| Transacciones de préstamo y devolución | v0.3–v0.4 | Validaciones de disponibilidad y rollback; casos de uso y reportes. |
| Pruebas e inicialización Docker | v0.5–v0.5.1 | JUnit, base de pruebas y corrección del error 126 al inicializar PostgreSQL. |
| Identidad UV y consola clásica | v0.6–v0.7 | Menús, splash UV, colores Borland y controles portables. |
| Buscador incremental de catálogos | v0.7 | Filtrado Java y PostgreSQL con navegación por teclado. |
| Modo didáctico SQL y Java | v0.8–v0.8.2 | Captura de SQL JDBC, explicación, fragmentos Java y coloreado de sintaxis. |
| Desplazamiento del visor y listas | v0.8.3–v0.8.4 | Apuntador visible y navegación entre filas y fragmentos. |
| CRUD de libros y préstamos | v0.8.4 | Formularios emergentes, selección visual y confirmaciones. |
| Catálogo de autores y alta integrada | v0.8.5–v0.8.6 | Administración de autores y altas desde libros. |
| Normalización de catálogos | v9.0–v9.0.1 | Claves foráneas, migración y formularios de selección/alta. |
| Tablas unificadas y detalle de préstamos | v9.0.2–v9.0.3 | Búsqueda, fechas, plazos, devoluciones, estadísticas e historial. |
| Ampliación y auditoría del laboratorio SQL | v9.0.4 | 36 ejercicios A7/A8/A9 y comprobación de solo lectura. |
| Splash institucional y créditos | v9.0.5 | Corrección de textos y versión mostrada en pantalla. |

## Campos recomendados en GitHub Projects

`Estado` (Pendiente/En curso/Completado), `Versión`, `Módulo`, `Tipo` (Funcionalidad/Corrección/Documentación/Pruebas), `Fecha del hito` y `Evidencia`.

El estado de una implementación histórica no equivale a una prueba de integración satisfactoria; registre validaciones pendientes como tareas abiertas.
