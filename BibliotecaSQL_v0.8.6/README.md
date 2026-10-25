# BibliotecaSQL v0.8.6

Aplicación de consola para las prácticas de Bases de Datos de Ingeniería de Software, Universidad Veracruzana.

Java 17 · PostgreSQL · JDBC · Maven · Lanterna · Docker Compose.

## Inicio

```bash
docker compose up -d --wait
mvn compile exec:java
```

**No es necesario eliminar los volúmenes anteriores si los datos ya están inicializados.**

## Cambios de v0.8.6

- Catálogo independiente de autores: consulta, búsqueda, altas, modificaciones y eliminaciones con confirmación.
- El menú principal utiliza la opción `2` para autores. El modo didáctico se activa o desactiva con `D` en el menú principal.
- En el formulario de libros, categoría y editorial se seleccionan de listas obtenidas con `SELECT DISTINCT`.
- Se pueden incorporar valores nuevos, con comprobación de duplicados por texto sin distinguir mayúsculas.
- Se conserva la opción «Sin editorial» y no se modifica el esquema SQL empleado en A7–A9.
- Guía de uso y limitaciones: `docs/17-catalogos-autores-categorias-editoriales.md`.

## Cambios de v0.8.5

- El formulario de libro permite elegir un autor existente o registrar uno nuevo.
- Al registrar un autor nuevo se solicita nombre y nacionalidad (opcional).
- El nuevo autor y el libro se guardan en una sola transacción JDBC: si falla el libro, se revierte también el autor.
- La modificación de un libro permite asociarlo con un autor nuevo o existente.
- Se comprueba si ya existe un autor con el mismo nombre (sin distinguir mayúsculas); en ese caso se solicita elegirlo de la lista.
- No se altera el esquema, ni los datos de `biblioteca_universitaria`, ni es necesario eliminar volúmenes.

## Cambios de v0.8.4

- El visor didáctico tiene selección de línea `▶` que responde a ↑↓, Page Up/Down y Home/End.
- El catálogo incorpora alta (`4`), modificación (`5`) y eliminación (`6`) del libro seleccionado.
- Los formularios se presentan en ventanas emergentes, con selección de autores y confirmación de operaciones.
- Los préstamos se eligen por nombre de usuario y título de libro. Ya no es necesario escribir sus identificadores.
- En préstamos activos se puede consultar, modificar el plazo, registrar devolución y cancelar un préstamo abierto, con confirmaciones.
- Los mensajes de éxito y error se muestran en ventanas emergentes.
- Las escrituras se restringen a `biblioteca_operativa`; las tablas de la base académica permanecen sin modificaciones.

Más información en `docs/15-crud-y-navegacion.md`.

## Consideraciones

- Las altas, modificaciones y bajas de libros respetan las reglas y restricciones del esquema SQL original.
- No es posible eliminar un libro asociado a préstamos históricos, salvo que se diseñe una política de archivado diferente.
- No se permite eliminar préstamos cerrados del historial. La cancelación de un préstamo **activo** sí está disponible y libera el libro atómicamente.
- La compilación de producción fue verificada con `javac` y clases auxiliares para la API de Lanterna. Falta comprobar Maven, la biblioteca real y PostgreSQL en el equipo del docente.

## Material académico

El código utiliza comentarios breves para explicar validaciones, formularios, consultas JDBC, manejo de resultados y transacciones. Se conservan los laboratorios A7, A8 y A9 y los documentos de las versiones anteriores.
