# BibliotecaSQL v0.8

Aplicación didáctica de gestión bibliotecaria con Java 17, JDBC, PostgreSQL, Docker Compose y una interfaz clásica de texto mediante Lanterna.

## Inicio

```bash
docker compose up -d --wait
mvn compile exec:java
```

Abra una terminal UTF-8 de al menos **80 columnas y 24 filas**. Se recomienda Windows Terminal, Terminal de macOS, una terminal de Linux o la terminal integrada de VS Code.

## Navegación

- **1–9**: seleccionar una opción del menú principal; **0** o **ESC**: regresar.
- **1** o **/**: activar el buscador incremental de una tabla.
- **2**, en el catálogo: alternar entre búsqueda en memoria y PostgreSQL.
- **9**, en el menú principal: activar o desactivar el modo didáctico.
- **3**, en una tabla o en la confirmación de un préstamo: mostrar las sentencias SQL ejecutadas, si el modo didáctico está activo.
- **← / →**: recorrer sentencias dentro de la ventana SQL; **↑ / ↓**: desplazar el contenido.
- **2**, dentro de la ventana SQL de un laboratorio: mostrar u ocultar la explicación.

## Cambios en esta versión

- Se corrigió la visualización didáctica: ahora se registran las sentencias que pasan por JDBC y se consultan sin perder los resultados de la tabla.
- Se muestran los parámetros de `PreparedStatement` por separado y se puede recorrer la secuencia SQL de préstamos y devoluciones.
- Se pasaron a bloques multilínea las consultas del catálogo y de préstamos activos.
- Se añadieron comentarios orientados a la lectura de la implementación y la guía `docs/12-modo-didactico-y-lectura-java.md`.
- Se conservan los 21 ejemplos SQL de A7, A8 y A9, la navegación numérica, las reglas de préstamo y las dos bases de datos independientes.

## Código que conviene revisar

`src/main/java/edu/uv/biblioteca/Main.java` — inicialización.  
`ui/ClassicTui.java` — eventos de teclado y presentación.  
`service/LibraryService.java` — coordinación entre componentes.  
`dao/BookDao.java` — consultas del catálogo.  
`dao/SqlTrace.java` — registro de las sentencias ejecutadas.  
`service/LoanTransactionService.java` — transacciones.

## Pruebas

```bash
bash scripts/probar-integracion.sh
```

El entorno de creación permite verificar Java 17 con un compilador y clases auxiliares, pero no ejecutar aquí Maven con Lanterna real ni Docker. Valide `mvn compile` y las interacciones en su equipo antes de utilizar el proyecto en clase.

## Datos

La base `biblioteca_universitaria` conserva el conjunto académico. Los préstamos y devoluciones se permiten únicamente sobre `biblioteca_operativa`. No ejecute `docker compose down -v` sin respaldar previamente cualquier cambio: esa opción elimina el volumen de datos.
