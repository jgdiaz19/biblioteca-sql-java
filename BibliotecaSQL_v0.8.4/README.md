# BibliotecaSQL v0.8.4

Aplicación de consola para las prácticas de Bases de Datos de Ingeniería de Software, Universidad Veracruzana.

Java 17 · PostgreSQL · JDBC · Maven · Lanterna · Docker Compose.

## Inicio

```bash
docker compose up -d --wait
mvn compile exec:java
```

**No es necesario eliminar los volúmenes anteriores si los datos ya están inicializados.**

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
