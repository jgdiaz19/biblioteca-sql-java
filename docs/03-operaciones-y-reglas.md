# Operaciones y reglas de negocio

**Versión:** BibliotecaSQL v9.0.5. Las operaciones se realizan en `biblioteca_operativa`. La interfaz usa ventanas emergentes y selección visual de registros, evitando pedir identificadores de base de datos al usuario.

## Catálogos

El catálogo de libros permite **consultar, buscar, agregar, modificar y eliminar** registros, cuando las restricciones lo permiten. La opción **2** del menú principal administra **autores, categorías y editoriales** mediante tablas con búsqueda, alta, modificación y eliminación.

Al agregar o modificar un libro:

1. Seleccione un autor existente o capture uno nuevo.
2. Elija la categoría y la editorial en sus listas; puede registrar valores nuevos desde la misma operación.
3. Capture los demás datos del libro y confirme antes de guardar.
4. Revise el mensaje de resultado o error.

El alta de libros con nuevos valores relacionados se coordina mediante `LibraryService.saveBookWithSelections(...)` y `BookDao.saveWithSelections(...)`. Los cambios forman **una transacción**: ante un error de ISBN, por ejemplo, no deben quedar autor, categoría o editorial recién creados sin el libro.

Las claves foráneas impiden eliminar un autor, categoría o editorial cuando hay libros que los utilizan; tampoco se puede eliminar un libro si existen préstamos que lo referencien.

## Préstamos y devolución

| Regla | Funcionamiento |
|---|---|
| Usuario | Debe estar activo, o darse de alta como nuevo usuario en el formulario |
| Límite | Máximo **3 préstamos activos** por usuario |
| Ejemplar | Debe estar disponible y sin otro préstamo abierto |
| Plazo | De **1 a 365 días**, con 14 días sugeridos inicialmente |
| Fechas | Se registra el inicio actual y se calcula la fecha límite según el plazo |
| Consistencia | Registrar préstamo y cambiar disponibilidad se ejecutan dentro de una transacción |

`LoanTransactionService` utiliza `SELECT ... FOR UPDATE` para bloquear los registros y evitar conflictos entre operaciones concurrentes. El índice `uq_prestamo_activo_libro` protege también contra dos préstamos abiertos del mismo ejemplar.

**Devolver** un libro registra `fecha_devolucion_real` y actualiza `disponible` en la misma transacción. **Cambiar la fecha límite** afecta préstamos abiertos y no permite una fecha anterior al préstamo. **Cancelar** un préstamo activo lo elimina físicamente y libera el libro tras confirmación; los préstamos ya devueltos no se pueden cancelar por esa vía.

La cancelación física es una simplificación didáctica: los préstamos cancelados no permanecen en el historial; un sistema real probablemente necesitaría auditoría de cancelaciones.

```java
// Agrupamos todas las operaciones antes de confirmar.
connection.setAutoCommit(false);
try {
    // Realizamos las validaciones y actualizaciones.
    connection.commit();
} catch (SQLException | RuntimeException e) {
    connection.rollback();
    throw e;
} finally {
    connection.setAutoCommit(true);
}
```

El fragmento es un esquema explicativo; estudie la implementación real en `LoanTransactionService.java`.

## Reportes

| Pantalla | Información |
|---|---|
| Préstamos activos | Usuario, libro, fecha de préstamo y fecha límite |
| Registrar devolución | Tabla de préstamos abiertos, seleccionables |
| Préstamos vencidos | Fecha de préstamo, vencimiento y días de retraso |
| Estadísticas por categoría | Total, disponibles y prestados; `ENTER` abre títulos y prestatarios |
| Préstamos por usuario | Total, activos y devueltos; `ENTER` abre el historial con fechas |

Un préstamo está **activo** cuando `fecha_devolucion_real IS NULL`. Está **vencido** cuando sigue abierto y `fecha_devolucion < CURRENT_DATE`.

Si un libro aparece marcado como prestado pero no tiene un préstamo activo asociado, el detalle puede mostrar **«Sin usuario activo»** en lugar de inventar un prestatario.

## Práctica de comprobación

1. Registre un libro con categoría y editorial nuevas; compruebe las relaciones.
2. Pruebe un ISBN repetido y compruebe que no persistan datos parciales.
3. Registre un préstamo por 7 días con usuario nuevo y revise las fechas.
4. Intente prestar el mismo ejemplar nuevamente: debe rechazarse.
5. Devuelva el libro y revise su historial y disponibilidad.
6. Intente eliminar un catálogo al que pertenecen libros y explique el error de integridad.

Utilice datos ficticios, no información personal real. Nunca realice estas actividades sobre la base académica.
