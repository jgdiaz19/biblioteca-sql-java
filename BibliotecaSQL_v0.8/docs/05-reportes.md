# Reportes de BibliotecaSQL

## Préstamos vencidos

La consulta incluye solo préstamos con `fecha_devolucion_real IS NULL` y `fecha_devolucion < CURRENT_DATE`. Calculamos los días de retraso con la resta de fechas en PostgreSQL.

El resultado depende de la fecha actual del servidor de PostgreSQL. Por eso puede cambiar de un día a otro sin modificar los datos.

## Préstamos por usuario

Partimos de `usuarios` y usamos `LEFT JOIN` para mostrar también a quienes no tienen ningún préstamo abierto. `COUNT(p.id_prestamo)` devuelve cero cuando no hay coincidencias. El filtro de préstamo activo permanece dentro de `ON`; si se coloca en `WHERE`, puede excluir usuarios sin préstamos.

## Revisión sugerida

Compare los reportes con consultas ejecutadas directamente en `psql`. Compruebe también que registrar una devolución cambia el listado de vencidos y la cantidad de préstamos activos.
