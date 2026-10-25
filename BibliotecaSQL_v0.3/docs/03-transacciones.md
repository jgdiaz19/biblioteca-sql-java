# Transacciones y reglas de negocio (v0.3)

## Registro de préstamo

1. Comprobar que la conexión apunta a `biblioteca_operativa`.
2. Iniciar transacción (`setAutoCommit(false)`).
3. Bloquear al usuario con `SELECT ... FOR UPDATE` y validar su estado.
4. Contar préstamos activos del usuario; límite configurable en el servicio: 3.
5. Bloquear el libro y validar su disponibilidad.
6. Rechazar si existe un préstamo abierto del libro.
7. Insertar el préstamo y obtener su ID con `RETURNING`.
8. Marcar el libro como no disponible.
9. Confirmar (`commit`); ante cualquier error, revertir (`rollback`).

## Devolución

1. Comprobar que la conexión apunta a `biblioteca_operativa`.
2. Bloquear el préstamo; rechazar identificadores inexistentes y préstamos cerrados.
3. Comprobar que la fecha de devolución real no antecede al préstamo.
4. Bloquear el libro correspondiente.
5. Registrar la fecha real y liberar el libro en una transacción indivisible.

## Restricciones del esquema

`uq_prestamo_activo_libro` es un índice único parcial que impide dos préstamos con `fecha_devolucion_real IS NULL` sobre el mismo libro. Las claves foráneas y restricciones `CHECK` mantienen integridad referencial y temporal.

## Comportamiento concurrente

Dos préstamos sobre el mismo libro se serializan mediante el bloqueo del registro de libros, y la restricción parcial actúa como defensa adicional. Dos préstamos simultáneos del mismo usuario se serializan al bloquear la fila de usuarios antes de consultar su número de préstamos activos. La devolución bloquea primero el préstamo y después el libro. La aplicación comparte una conexión de consola; para múltiples terminales se usan conexiones independientes.

## Debate para estudiantes

- ¿Por qué consultar `disponible` sin bloqueo no es suficiente?
- ¿En qué se diferencia `fecha_devolucion` de `fecha_devolucion_real`?
- ¿Qué sucede si falla la segunda sentencia `UPDATE` antes de `commit`?
- ¿Por qué el límite de tres préstamos no se implementa únicamente en el menú?
