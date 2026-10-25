# Evolución de BibliotecaSQL

Este repositorio contiene una **reconstrucción retrospectiva** de los paquetes de código conservados durante el desarrollo didáctico.

**Las fechas de los hitos describen cuándo se trabajaron o comentaron los cambios, no las fechas en que se crearon los commits en GitHub.** Los commits y etiquetas Git se generaron posteriormente a partir de los ZIP conservados. Los intervalos marcados son aproximados.

| Versión | Hito documentado | Cambios principales |
|---|---|---|
| [`v0.1`](../../releases/tag/v0.1) | 2026-10-18 | Primera conexión Java–PostgreSQL y consultas de biblioteca |
| [`v0.2`](../../releases/tag/v0.2) | 2026-10-18 | Arquitectura por capas, menús y visualización didáctica |
| [`v0.3`](../../releases/tag/v0.3) | 2026-10-18 | Préstamos, devoluciones y transacciones JDBC |
| [`v0.4`](../../releases/tag/v0.4) | 2026-10-18/19 | Reportes y comentarios de código para estudiantes |
| [`v0.5`](../../releases/tag/v0.5) | 2026-10-19 | Pruebas automatizadas y trazabilidad SQL |
| [`v0.5.1`](../../releases/tag/v0.5.1) | 2026-10-19 | Corrección de inicialización PostgreSQL en Docker |
| [`v0.6`](../../releases/tag/v0.6) | 2026-10-20 | Splash UV, colores y 21 consultas SQL |
| [`v0.7`](../../releases/tag/v0.7) | 2026-10-20 | Interfaz Borland con Lanterna y búsqueda incremental |
| [`v0.8`](../../releases/tag/v0.8) | 2026-10-20 | Captura SQL desde JDBC y documentación Java |
| [`v0.8.1`](../../releases/tag/v0.8.1) | 2026-10-20 | Coloreado SQL y explicaciones |
| [`v0.8.2`](../../releases/tag/v0.8.2) | 2026-10-20 | Código Java real, coloreado y cuatro vistas didácticas |
| [`v0.8.3`](../../releases/tag/v0.8.3) | 2026-10-20 | Desplazamiento y apuntador del visor |
| [`v0.8.4`](../../releases/tag/v0.8.4) | 2026-10-22 | CRUD de libros y préstamos con ventanas emergentes |
| [`v0.8.5`](../../releases/tag/v0.8.5) | 2026-10-22 | Registro de autores desde formulario de libros |
| [`v0.8.6`](../../releases/tag/v0.8.6) | 2026-10-22 | CRUD de autores y selectores de categorías/editoriales |
| [`v9.0`](../../releases/tag/v9.0) | 2026-10-22 | Normalización de categorías y editoriales |
| [`v9.0.1`](../../releases/tag/v9.0.1) | 2026-10-22 | Alta integrada de categorías/editoriales desde libros |
| [`v9.0.2`](../../releases/tag/v9.0.2) | 2026-10-25 | Tablas unificadas y mejoras de préstamos activos |
| [`v9.0.3`](../../releases/tag/v9.0.3) | 2026-10-25 | Fechas, plazos, historial y reportes de préstamos |
| [`v9.0.4`](../../releases/tag/v9.0.4) | 2026-10-25 | 36 ejercicios y auditoría del laboratorio SQL |
| [`v9.0.5`](../../releases/tag/v9.0.5) | 2026-10-25 | Identidad institucional y créditos del splash screen |

## Cómo comparar el progreso

En GitHub elija **Code → Tags** y abra una versión. Use **Compare** para analizar los cambios entre versiones, por ejemplo `v0.7...v0.8.2` o `v9.0...v9.0.5`.

La etiqueta `v9.0.5` reproduce el paquete entregado de esa versión. La rama `main` añade esta documentación de publicación retrospectiva.

## Advertencia de versiones antiguas

Las etiquetas previas se conservan con fines pedagógicos e históricos: algunas contienen errores conocidos, dependencias iniciales o funcionalidades incompletas. **Para ejecutar el software utilice `v9.0.5`.**

## Fechas y procedencia

La primera versión se generó el domingo 18 de octubre de 2026, y la evolución siguió hasta el domingo 25. No debe presentarse esta reconstrucción como prueba de que GitHub registró los cambios originalmente en esas fechas.
