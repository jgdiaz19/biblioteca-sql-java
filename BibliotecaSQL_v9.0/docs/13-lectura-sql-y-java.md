# Modo didáctico: SQL y Java

La ventana didáctica ofrece cuatro vistas: `1` SQL ejecutado, `2` código Java del proyecto, `3` explicación SQL y `4` explicación Java. Las flechas izquierda y derecha cambian la sentencia de la operación; las flechas arriba y abajo desplazan el texto. `0` regresa.

La aplicación identifica el archivo, el método y la línea desde los que se preparó cada sentencia JDBC. Para presentar el código auténtico de ese método, lee el archivo correspondiente dentro de `src/main/java`. **Ejecute Maven desde la raíz del proyecto** para que dicha carpeta esté disponible.

La vista de Java muestra un fragmento cercano a la llamada de preparación. Si la consulta fue construida en una constante o método diferente, puede navegar al archivo y buscar su nombre en VS Code. No se muestra un ejemplo ficticio como si fuera el código ejecutado.

La explicación Java ofrece una guía de lectura de `PreparedStatement`, `ResultSet`, procesamiento de filas y liberación de recursos. En las transacciones, estudie además el uso de `commit()` y `rollback()` en `LoanTransactionService`.

El historial conserva los parámetros internamente para pruebas, pero **la interfaz didáctica no los muestra como panel independiente**. En lugar de ello, los alumnos pueden seguir los métodos `setString`, `setInt`, etc. directamente en el código Java.

Los resaltadores utilizan un análisis léxico ligero por línea, adecuado para este laboratorio; no reemplazan un parser completo de Java ni PostgreSQL.

La vista Java es un fragmento contextual del archivo original y no pretende reproducir siempre el método completo. El encabezado muestra la ruta de origen para abrir el archivo completo desde VS Code.
