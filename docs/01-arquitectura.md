# Arquitectura de BibliotecaSQL

## 1. Organización general

BibliotecaSQL separa la presentación, las reglas de negocio y el acceso a los datos. Esta división facilita identificar dónde debe realizarse una modificación y evita mezclar el código de la interfaz con las sentencias SQL.

```text
Teclado y pantalla
       │
       ▼
ClassicTui (interfaz de consola con Lanterna)
       │
       ▼
LibraryService (coordinación y validación)
       │
       ├── BookDao, AuthorDao, CatalogDao
       ├── LoanDao, ReportDao, LabDao
       └── LoanTransactionService
                    │
                    ▼
             JDBC / PostgreSQL
```

El método `main()` de [`Main.java`](../src/main/java/edu/uv/biblioteca/Main.java) crea la conexión JDBC y prepara los objetos que necesita la aplicación. La configuración se obtiene mediante `DatabaseConfig.fromEnvironment()`.

## 2. Responsabilidades de las clases

| Componente | Función |
|---|---|
| `ui/ClassicTui.java` | Presentar menús, tablas, formularios y mensajes; procesar teclas |
| `service/LibraryService.java` | Validar entradas y comunicar la interfaz con las operaciones de datos |
| `service/LoanTransactionService.java` | Coordinar préstamos, devoluciones y cambios que requieren transacciones |
| `dao/BookDao.java` | Ejecutar consultas y modificaciones del catálogo de libros |
| `dao/AuthorDao.java`, `CatalogDao.java` | Administrar autores, categorías y editoriales |
| `dao/LoanDao.java`, `ReportDao.java` | Consultar préstamos y elaborar reportes |
| `dao/LabDao.java` | Contener y ejecutar las consultas de A7, A8 y A9 |
| `model/` | Representar datos mediante tipos `record` de Java |
| `dao/SqlTrace.java` | Registrar las sentencias JDBC ejecutadas para su estudio |

En el repositorio también existen `MenuController.java` y `ConsoleView.java`, procedentes de etapas anteriores. **La interfaz principal de v9.0.5 es `ClassicTui`**, que se instancia desde `Main.java`.

## 3. Recorrido de una consulta

Cuando el usuario abre el catálogo, `ClassicTui.tableBooks()` solicita los registros mediante `LibraryService.listBooks()`. El servicio invoca `BookDao.list()`, que ejecuta la consulta SQL y transforma el `ResultSet` en una lista de objetos `Book`. Finalmente, `ClassicTui` presenta la tabla y administra la navegación.

El flujo de datos puede resumirse así:

```text
tableBooks() → listBooks() → BookDao.list() → PreparedStatement
                                                │
                                                ▼
                                          PostgreSQL
                                                │
                                                ▼
                                 ResultSet → List<Book> → Tabla
```

La interfaz trabaja con objetos Java, no con filas JDBC. Así, un cambio en el formato visual no requiere modificar necesariamente la consulta SQL.

## 4. Operaciones que modifican datos

Las operaciones de préstamo y devolución necesitan comprobar varias condiciones y actualizar más de una tabla. Por eso se realizan en `LoanTransactionService`, donde se emplean transacciones con `commit()` y `rollback()`.

En el registro de libros, `BookDao.saveWithSelections()` puede crear un autor, una categoría o una editorial y guardar el libro dentro de una misma transacción. El objetivo es evitar registros relacionados incompletos si falla alguna de las operaciones.

## 5. Acceso a PostgreSQL mediante JDBC

Las clases DAO utilizan principalmente `PreparedStatement` para preparar sentencias y enviar valores. Para consultar datos se emplea `executeQuery()`; para operaciones `UPDATE` o `DELETE`, `executeUpdate()`. Las consultas que utilizan `RETURNING` pueden devolver un `ResultSet` incluso después de un `INSERT`.

El uso de `try-with-resources` permite cerrar `PreparedStatement` y `ResultSet` al finalizar sus bloques. `SqlTrace.prepare()` envuelve la preparación normal para registrar las sentencias ejecutadas cuando se activa su consulta didáctica.

## 6. Ejercicio de lectura

Localice `Main.java`, `ClassicTui.tableBooks()`, `LibraryService.listBooks()` y `BookDao.list()`. Anote qué recibe y devuelve cada método, identifique el punto donde se ejecuta SQL y explique por qué `BookDao` no necesita conocer la posición del cursor en la pantalla.

La [guía del catálogo de libros](07-catalogo-libros-java.md) desarrolla este recorrido con fragmentos del código y con el registro, la modificación y la eliminación de libros.

Esta arquitectura es una simplificación didáctica: la ejecución en consola utiliza una conexión durante la sesión y no incorpora por sí misma las medidas de una aplicación multiusuario de producción.
