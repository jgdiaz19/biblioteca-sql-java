# Modo didáctico y lectura del código

## Cómo utilizarlo

1. Inicie PostgreSQL y la aplicación con los comandos del README.
2. En el menú principal seleccione **9. Modo didáctico**.
3. Entre en **1. Catálogo**, **2. Préstamos**, **3. Estadísticas** o **8. Laboratorios SQL**.
4. En la tabla presione **3** para abrir la ventana SQL. En las pantallas de confirmación de préstamos y devoluciones también puede pulsar **3**.
5. Recorra las sentencias con **←** y **→**. Use **↑**, **↓**, **PgUp** y **PgDn** para desplazarse. Pulse **0** o **ESC** para cerrar.
6. En los laboratorios, pulse **2** dentro de la ventana para mostrar la explicación del ejercicio.

## Lo que aparece

La ventana muestra el texto de la sentencia entregada a `prepareStatement` y registra los parámetros capturados por los métodos `setX` al momento de llamar a `executeQuery`, `executeUpdate` o `execute`. Si una operación utiliza varias sentencias, es posible recorrerlas.

La ventana **no** muestra un plan de ejecución ni sustituye los marcadores `?` por valores dentro del SQL. Los parámetros aparecen por separado, del mismo modo que JDBC los administra. No se registran credenciales de conexión.

## Recorrido recomendado

1. `Main`: crea la conexión y organiza las dependencias.
2. `ClassicTui`: recibe una tecla y presenta las tablas.
3. `LibraryService`: delega la operación solicitada.
4. `BookDao`, `LoanDao`, `ReportDao` o `LabDao`: preparan el SQL y leen `ResultSet`.
5. `SqlTrace`: observa las operaciones reales del `PreparedStatement` sin cambiar el SQL.
6. `LoanTransactionService`: comprueba reglas de negocio y utiliza `COMMIT` o `ROLLBACK`.

## Ejercicio: comparar búsquedas

Abra el catálogo, active la búsqueda con `1`, escriba parte de un título y presione `ENTER`. Con el modo **Java** la lista se filtra en memoria y no se ejecuta una consulta por cada carácter. Cambie con `2` al modo **PostgreSQL** y repita la búsqueda. Presione `3` para observar la sentencia con `ILIKE ?` y el parámetro utilizado.

## Observaciones

- El historial tiene un máximo de 40 sentencias por operación para evitar un crecimiento indefinido.
- Al entrar en una opción nueva del menú principal se limpia el historial.
- Los valores de parámetros muy extensos se recortan en la visualización.
- Se mantienen las 21 consultas de A7, A8 y A9, pendientes de cotejar una a una con las actividades originales.
