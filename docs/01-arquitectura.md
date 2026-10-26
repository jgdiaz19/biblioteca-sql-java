# Arquitectura y recorrido del código

**Versión de referencia:** BibliotecaSQL v9.0.5.

## Propósito

Identificar las responsabilidades de cada clase y seguir una operación desde la interfaz de consola hasta PostgreSQL. El proyecto emplea **Java 17**, **JDBC**, **Lanterna** y una separación sencilla entre presentación, servicios, acceso a datos y modelos.

```text
Main
  └── ClassicTui (menús, teclado, formularios y tablas)
        └── LibraryService (coordinación y validaciones)
              ├── BookDao, AuthorDao, CatalogDao
              ├── LoanDao, ReportDao, LabDao
              └── LoanTransactionService (transacciones)
                    └── JDBC / PostgreSQL
```

## Clases principales

| Componente | Responsabilidad |
|---|---|
| `Main.java` | Inicia el sistema, abre la conexión y construye los objetos necesarios |
| `config/DatabaseConfig.java` | Lee la configuración JDBC del entorno |
| `ui/ClassicTui.java` | Presenta ventanas, tablas, menús y formularios; interpreta las teclas |
| `service/LibraryService.java` | Comprueba valores y coordina las operaciones de la aplicación |
| `service/LoanTransactionService.java` | Valida y ejecuta préstamos, devoluciones y cancelaciones como transacciones |
| `dao/BookDao.java` | Consultas y modificaciones de libros |
| `dao/AuthorDao.java`, `CatalogDao.java` | Administración de autores, categorías y editoriales |
| `dao/LoanDao.java`, `ReportDao.java` | Préstamos activos e informes |
| `dao/LabDao.java` | Las 36 consultas del laboratorio A7–A9 |
| `dao/SqlTrace.java` | Registra SQL ejecutado para estudiarlo desde el modo didáctico |
| `ui/JavaSourceView.java` | Localiza fragmentos del archivo Java que preparó la consulta |
| `model/` | Objetos `record` que transportan los valores entre capas |

`MenuController` y `ConsoleView` se conservan en el repositorio, pero **la interfaz actual invocada desde `Main` es `ClassicTui`**. Evite tomar las clases antiguas como punto de partida al estudiar v9.0.5.

## Ejemplo: consultar libros

1. El usuario selecciona el catálogo en la consola.
2. `ClassicTui.tableBooks()` solicita la lista al servicio.
3. `LibraryService.listBooks()` delega en `BookDao.list()`.
4. `BookDao` ejecuta una consulta mediante JDBC sobre la vista `libros_consulta`.
5. Se recorre `ResultSet` para construir objetos `Book`; la interfaz presenta una tabla navegable.

Para buscar, el catálogo permite comparar **filtrado en memoria Java** con **una consulta SQL parametrizada** mediante `ILIKE`. Revise ambos recorridos dentro de `ClassicTui.tableBooks()` y `BookDao`.

## Ejemplo: registrar un préstamo

`ClassicTui.borrow()` permite seleccionar usuario y libro, y capturar el plazo. `LibraryService` invoca `LoanTransactionService`, que valida las reglas de negocio, bloquea los registros correspondientes, inserta el préstamo y cambia la disponibilidad del libro. `commit()` confirma ambos cambios; `rollback()` revierte la operación ante un error.

Los DAO no deberían manejar teclas ni dibujar ventanas, y las clases de interfaz no deberían construir directamente toda la lógica SQL. Esta separación hace posible modificar una capa sin rehacer las demás.

## Guía de lectura de Java y JDBC

- Localice primero el método invocado desde la interfaz; identifique sus parámetros y valor de retorno.
- En el DAO, distinga `prepareStatement()`, los métodos `setX()`, `executeQuery()` o `executeUpdate()`, y el recorrido de `ResultSet`.
- Analice cómo `try-with-resources` cierra sentencias y resultados.
- Para una operación de escritura, identifique dónde comienza y termina la transacción.

**Actividad sugerida:** siga una búsqueda de libro con el modo didáctico, anote las clases participantes y explique por qué usar `PreparedStatement` es preferible a concatenar texto introducido por el usuario.

Este diseño simplifica el caso didáctico al compartir una conexión durante una sesión de consola; no representa una arquitectura multiusuario lista para producción.
