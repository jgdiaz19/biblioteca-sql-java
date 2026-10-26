# Historial de desarrollo de BibliotecaSQL

Este registro reúne las versiones conservadas de BibliotecaSQL y los cambios que motivaron cada entrega. Su finalidad es permitir el análisis de la evolución del código, el modelo de datos, la interfaz y las pruebas.

**Procedencia de las fechas.** El historial Git fue reconstruido posteriormente a partir de paquetes de código y de los hitos documentados durante el trabajo. Las fechas de la tabla son referencias al periodo de desarrollo; **no corresponden necesariamente a la creación original de commits en GitHub**. Las fechas indicadas como intervalos son aproximadas.

## Versiones conservadas

| Versión | Fecha o periodo del hito | Cambio documentado |
|---|---|---|
| [v0.1](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.1) | 18 oct. 2026 | Conexión inicial entre Java y PostgreSQL; consultas del caso Biblioteca |
| [v0.2](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.2) | 18 oct. 2026 | Separación de presentación, servicios y acceso a datos |
| [v0.3](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.3) | 18 oct. 2026 | Préstamos, devoluciones y transacciones JDBC |
| [v0.4](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.4) | 18–19 oct. 2026 | Reportes, laboratorio y documentación inicial del código |
| [v0.5](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.5) | 19 oct. 2026 | Pruebas automatizadas y trazabilidad de consultas |
| [v0.5.1](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.5.1) | 19 oct. 2026 | Corrección del script de inicialización de PostgreSQL en Docker |
| [v0.6](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.6) | 20 oct. 2026 | Presentación institucional y primer laboratorio ampliado |
| [v0.7](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.7) | 20 oct. 2026 | Interfaz de terminal con Lanterna y búsqueda incremental |
| [v0.8](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8) | 20 oct. 2026 | Registro del SQL ejecutado desde JDBC |
| [v0.8.1](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.1) | 20 oct. 2026 | Resaltado de sintaxis SQL y explicaciones |
| [v0.8.2](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.2) | 20 oct. 2026 | Visualización de fragmentos Java y cuatro vistas didácticas |
| [v0.8.3](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.3) | 20 oct. 2026 | Navegación y desplazamiento en el visor |
| [v0.8.4](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.4) | 22 oct. 2026 | Formularios y operaciones de gestión de libros y préstamos |
| [v0.8.5](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.5) | 22 oct. 2026 | Alta de autores desde el registro de libros |
| [v0.8.6](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v0.8.6) | 22 oct. 2026 | Administración de autores y selección de categorías y editoriales |
| [v9.0](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0) | 22 oct. 2026 | Normalización de categorías y editoriales en la base operativa |
| [v9.0.1](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0.1) | 22 oct. 2026 | Alta transaccional de categorías y editoriales desde el libro |
| [v9.0.2](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0.2) | 25 oct. 2026 | Tablas de catálogos y préstamos con navegación uniforme |
| [v9.0.3](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0.3) | 25 oct. 2026 | Fechas, plazos, devoluciones e historial por usuario |
| [v9.0.4](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0.4) | 25 oct. 2026 | Laboratorio de 36 consultas y script de auditoría SQL |
| [v9.0.5](https://github.com/jgdiaz19/biblioteca-sql-java/tree/v9.0.5) | 25 oct. 2026 | Ajuste de identidad institucional, créditos y versión visible |

## Cómo comparar dos etapas

GitHub permite examinar una etiqueta y comparar su contenido con otra. Las comparaciones siguientes ilustran cambios de distinta naturaleza:

- [v0.1 y v0.7](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.1...v0.7): desarrollo desde la primera conexión hasta la interfaz clásica.
- [v0.7 y v0.8.2](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.7...v0.8.2): incorporación del visor de SQL y Java.
- [v0.8.6 y v9.0](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.8.6...v9.0): modificación del modelo relacional.
- [v9.0 y v9.0.5](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v9.0...v9.0.5): consolidación de catálogos, préstamos e informes.

Al estudiar una diferencia, identifique primero el problema que se intentó resolver. Después revise qué archivos cambiaron, si fue necesario modificar la base de datos y qué comprobaciones se añadieron.

## Alcance del registro

Las versiones antiguas se mantienen como evidencia de decisiones y correcciones. Algunas contienen problemas que motivaron las entregas siguientes; **no se recomiendan para ejecutar las prácticas actuales**. La referencia funcional de los estudiantes es `v9.0.5`.

La rama `main` puede incorporar cambios posteriores en documentación y mantenimiento sin modificar la etiqueta histórica de la versión.
