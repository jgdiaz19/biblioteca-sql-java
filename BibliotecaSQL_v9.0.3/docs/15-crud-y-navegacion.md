# BibliotecaSQL v0.8.4: formularios y navegación

## Navegación en el visor didáctico

El indicador `▶` sigue la línea seleccionada. Las flechas arriba y abajo cambian la selección incluso cuando el texto cabe completamente en la pantalla. Al sobrepasar el borde visible, el contenido se desplaza. También están disponibles Page Up, Page Down, Home y End. Las flechas izquierda y derecha cambian de sentencia SQL registrada. La barra lateral aparece cuando hay más líneas que espacio visible.

El código Java se distribuye en líneas visuales para que pueda consultarse en ventanas estrechas. El número mostrado en el contador corresponde a una línea visual, no necesariamente a una línea del archivo original.

## Catálogo de libros

Abrir el catálogo desde el menú principal. Las operaciones disponibles son:

- `4`: alta de libro. Primero se selecciona un autor y luego se capturan los campos restantes en ventanas emergentes.
- `5`: modificación del libro seleccionado con las flechas.
- `6`: eliminar el libro seleccionado, previa confirmación con `S` o `N`.
- `1` o `/`: filtrar el catálogo; `2`: alternar entre búsqueda local y PostgreSQL.

Los campos de edición corresponden al esquema original: título, ISBN, año, categoría, editorial, páginas, precio y autor. El campo `disponible` no se modifica manualmente desde el formulario: depende de los préstamos y devoluciones. El identificador del libro se obtiene de la fila elegida y nunca debe escribirse a mano.

Las operaciones de escritura requieren la base `biblioteca_operativa`. Una eliminación puede fallar si existen registros de préstamos que referencian el libro; este comportamiento protege la integridad referencial.

## Préstamos

En `Registrar préstamo`, seleccionar un usuario activo y después un libro disponible. Se pide confirmación antes de guardar. En `Registrar devolución`, seleccionar un préstamo activo de la lista y confirmar.

En `Préstamos activos`, escoger un registro y realizar una de estas operaciones: consultar detalle, modificar la fecha límite, registrar devolución o cancelar un préstamo activo. La cancelación elimina el préstamo abierto y vuelve a poner el libro como disponible dentro de una misma transacción. No es posible cancelar un préstamo que ya fue devuelto.

La baja de un préstamo histórico no se ofrece. Se conserva así la trazabilidad de los préstamos ya cerrados.

## Clases nuevas y modificadas

- `BookDetails`: valores completos del formulario de libros.
- `Author` y `LibraryUser`: muestran nombres en los selectores.
- `BookDao`: consulta completa, inserción, actualización y eliminación de libros.
- `LoanDao`: lista de usuarios activos.
- `LibraryService`: validación y coordinación de operaciones.
- `LoanTransactionService`: devolución, modificación de plazo y cancelación transaccional.
- `ClassicTui`: ventanas, selección de registros, confirmaciones y navegación del visor.

## Validaciones pendientes de ejecutar en el equipo del docente

1. Crear y modificar un libro, verificar los datos en PostgreSQL.
2. Intentar dar de alta un ISBN existente y comprobar el mensaje.
3. Eliminar un libro sin préstamos e intentar eliminar otro con préstamos asociados.
4. Prestar un libro usando los selectores, modificar el plazo y devolverlo.
5. Cancelar un préstamo activo y verificar que el libro vuelva a estar disponible.
6. Recorrer SQL y Java con flechas, tanto si hay pocas líneas como si hay varias páginas.
7. Ejecutar `mvn test` con las dependencias reales y los tests de integración previstos.

No se debe ejecutar `docker compose down -v` sobre datos que se desee conservar.
