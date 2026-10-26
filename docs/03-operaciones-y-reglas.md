# Operaciones y reglas de negocio

La aplicación permite modificar el catálogo de libros y registrar préstamos sin solicitar al usuario que memorice las claves primarias. La interfaz muestra nombres y listas; Java utiliza los identificadores correspondientes al comunicarse con PostgreSQL.

## 1. Administración de libros y catálogos

Desde **Catálogo de libros** se pueden consultar, buscar, agregar, modificar y eliminar ejemplares. Las operaciones de alta y modificación utilizan formularios emergentes. En ellos se puede seleccionar un autor, una categoría o una editorial registrada, o capturar un valor nuevo.

El flujo de guardado combina tres responsabilidades:

| Componente | Responsabilidad |
|---|---|
| `ClassicTui.editBook()` | Capturar los valores y solicitar confirmación |
| `LibraryService.saveBookWithSelections()` | Verificar los datos y autorizar la operación sobre la base operativa |
| `BookDao.saveWithSelections()` | Resolver las relaciones y guardar los cambios en una transacción |

Cuando se crea un autor, categoría o editorial desde el formulario, el valor no se conserva hasta confirmar el registro del libro. Si la inserción falla, se revierte el conjunto de cambios mediante `rollback()`.

Las operaciones de eliminación están sujetas a las claves foráneas de PostgreSQL: no puede borrarse una categoría, una editorial o un autor que tenga libros relacionados; tampoco puede borrarse un libro con préstamos históricos.

La [referencia técnica del catálogo](07-catalogo-libros-java.md) permite seguir estas operaciones en el código Java.

## 2. Registro de préstamos

Antes de registrar un préstamo, `LoanTransactionService` comprueba que se cumplan las siguientes condiciones:

| Condición | Regla |
|---|---|
| Usuario | Debe existir y estar activo, o registrarse desde el formulario |
| Préstamos abiertos | Máximo de **tres** por usuario |
| Libro | Debe estar disponible y sin otro préstamo activo |
| Plazo | De **1 a 365 días**; el formulario propone 14 días |
| Fecha inicial | Se toma la fecha actual del sistema |
| Fecha límite | Se calcula sumando el plazo seleccionado |
| Consistencia | La inserción del préstamo y la actualización de disponibilidad se confirman juntas |

Si se registra un usuario nuevo desde la pantalla de préstamo, el alta del usuario y el préstamo se ejecutan dentro de una sola transacción. Si no puede prestarse el ejemplar, el usuario tampoco queda registrado como resultado de ese intento.

Para prevenir conflictos, el servicio utiliza bloqueos `SELECT ... FOR UPDATE` sobre los registros pertinentes. Además, el índice `uq_prestamo_activo_libro` de PostgreSQL impide dos préstamos abiertos para el mismo ejemplar.

## 3. Devoluciones, plazos y cancelaciones

**Devolución.** Se selecciona un préstamo activo. El sistema guarda la fecha de devolución real y marca el libro como disponible dentro de una misma transacción.

**Cambio de fecha límite.** Permite actualizar `fecha_devolucion` en un préstamo abierto, siempre que la fecha propuesta no sea anterior al inicio.

**Cancelación.** Permite eliminar un préstamo activo tras confirmar la operación y liberar el libro. Esta cancelación elimina físicamente el préstamo y, por tanto, no conserva un historial de cancelaciones. Los préstamos ya devueltos no pueden cancelarse mediante esta operación. Es una simplificación propia del ejercicio didáctico.

En la implementación, `setAutoCommit(false)` agrupa los cambios; `commit()` los confirma y `rollback()` los deshace cuando aparece una excepción. La integridad no debe depender únicamente de las validaciones visibles en la terminal.

## 4. Reportes disponibles

| Pantalla | Contenido |
|---|---|
| Préstamos activos | Usuario, libro, fecha inicial y fecha límite |
| Registrar devolución | Préstamos abiertos en una tabla seleccionable |
| Préstamos vencidos | Fecha inicial, fecha límite y días transcurridos desde el vencimiento |
| Estadísticas por categoría | Total, disponibles y prestados; `ENTER` abre el detalle de los libros |
| Préstamos por usuario | Total, activos y devueltos; `ENTER` muestra el historial |

La condición `fecha_devolucion_real IS NULL` identifica préstamos activos. Para determinar si están vencidos se añade `fecha_devolucion < CURRENT_DATE`.

Los reportes dependen de los datos actuales de `biblioteca_operativa`. Por esa razón, pueden cambiar cuando se registren préstamos, devoluciones o nuevos libros.

## 5. Actividades de verificación

1. Registre un libro con un autor, una categoría y una editorial nuevos. Compruebe que las relaciones se guardaron correctamente.
2. Intente registrar otro libro con el mismo ISBN. Verifique que la operación se rechaza y que no quedan registros relacionados incompletos.
3. Registre un préstamo de siete días. Compare la fecha inicial con la fecha límite.
4. Intente prestar de nuevo el mismo libro mientras el préstamo anterior está abierto.
5. Registre la devolución y compruebe que el ejemplar vuelve a estar disponible y el historial conserva la fecha real.
6. Intente eliminar un autor o una categoría con libros asociados; explique la restricción de integridad.

Las pruebas deben realizarse con datos ficticios y sobre **`biblioteca_operativa`**.
