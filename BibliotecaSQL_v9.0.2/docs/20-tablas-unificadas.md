# Tablas y navegación unificadas (v9.0.2)

Los catálogos comparten una tabla que dibuja encabezados, selecciona filas con `▶` y permite filtrar registros. Las flechas desplazan el cursor y Page Up/Page Down avanzan por bloques. El índice original del registro se conserva aunque la tabla esté filtrada.

En autores, categorías y editoriales: `1` o `/` busca, `4` agrega, `5` modifica, `6` elimina y ENTER muestra detalles. Se solicita confirmación antes de borrar. Las claves foráneas siguen protegiendo los registros relacionados.

En préstamos activos, `4` registra un préstamo, `5` modifica la fecha límite, `6` registra la devolución y `7` cancela el préstamo activo con confirmación. El ID solo se usa internamente.

El catálogo de libros mantiene dos estrategias de búsqueda: Java filtra datos ya cargados; PostgreSQL ejecuta consultas parametrizadas `ILIKE`. La opción `2` alterna ambas estrategias.

## Pruebas manuales sugeridas

1. Buscar un autor y modificar el resultado resaltado.
2. Buscar una categoría y cancelar su eliminación.
3. Intentar eliminar una editorial utilizada por un libro. Debe mostrar el error.
4. Filtrar préstamos y devolver el seleccionado.
5. Verificar la columna categoría del catálogo de libros en una terminal de 80 columnas.
