# Préstamos, fechas y reportes — v9.0.3

## Fechas

La tabla `prestamos` conserva tres valores diferentes:

- `fecha_prestamo`: día en que se entregó el ejemplar.
- `fecha_devolucion`: fecha límite pactada al registrar el préstamo.
- `fecha_devolucion_real`: fecha de devolución efectiva; `NULL` mientras el préstamo permanece activo.

PostgreSQL sigue almacenando estas columnas como `DATE`. La interfaz utiliza `dd-MM-aaaa` para leer y mostrar fechas. El sistema fija el día de inicio al registrar la operación y permite elegir un plazo de 1 a 365 días (14 por defecto). La fecha límite se obtiene a partir de la fecha inicial más los días capturados. El campo de modificación de plazo acepta `DD-MM-AAAA` y valida que la nueva fecha no sea anterior al préstamo.

## Estadísticas por categoría

En el resumen se muestran totales, disponibles y prestados. ENTER abre los libros de esa categoría y el nombre del usuario que tiene un préstamo abierto, cuando existe. Cada fila de `libros` representa un ejemplar; no se maneja una columna de existencias múltiples.

Si `disponible = FALSE` y no existe préstamo activo, se muestra `Sin usuario activo` para señalar una inconsistencia que merece revisión, en lugar de inventar un prestatario.

## Préstamos por usuario

Los totales distinguen préstamos activos (sin fecha de devolución real) y devueltos (con fecha real). ENTER abre el historial de ese usuario, incluidos los préstamos ya cerrados. Los préstamos cancelados mediante eliminación física no forman parte de este historial; por ello, **total = activos + devueltos** para los registros existentes.

## Alta de usuario al prestar

La selección ofrece `+ Registrar nuevo usuario`. Se capturan nombre, tipo y carrera opcional mediante ventanas. La confirmación final guarda al usuario y al préstamo dentro de una única transacción. Al producirse un error, `ROLLBACK` revierte ambas inserciones. Si se utiliza un usuario existente, se conserva la validación de usuario activo y el límite de tres préstamos abiertos.

## Comprobaciones recomendadas

1. Crear un préstamo con plazo de 7 días y verificar ambas fechas.
2. Registrar préstamo con usuario nuevo y comprobar que aparece en el historial.
3. Intentar prestar un ejemplar ocupado; comprobar que no aparece un usuario nuevo sin préstamo.
4. Registrar una devolución desde la tabla y verificar que desaparece de activos, pero aparece en el historial del usuario.
5. Abrir una categoría con libros prestados y verificar los nombres de los prestatarios.
6. Revisar el reporte de vencidos: debe presentar fecha de préstamo, vencimiento y días de retraso.
7. Abrir los reportes en una terminal de 80 × 24 y comprobar las columnas y el desplazamiento.

Estas pruebas deben ejecutarse en `biblioteca_operativa`, sin modificar `biblioteca_universitaria`.
