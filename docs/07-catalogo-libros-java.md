# Catálogo de libros: referencia técnica de la implementación Java

**BibliotecaSQL v9.0.5** · **Módulo:** Catálogo de libros · **Tecnologías:** Java 17, JDBC, Lanterna y PostgreSQL.

## 1. Propósito de esta guía

Este documento explica cómo se implementaron las consultas y las operaciones de administración del catálogo de libros. Su finalidad es que el estudiante pueda localizar el código, identificar la responsabilidad de cada clase y comprender el recorrido de los datos desde la pantalla hasta PostgreSQL.

La explicación se basa en los archivos de la versión v9.0.5. Los fragmentos de código se han seleccionado del proyecto; en algunos se ajustaron los saltos de línea y la sangría para mejorar su lectura, **sin modificar la lógica mostrada**. Para conocer la implementación completa debe consultarse el archivo original.

Se recomienda haber revisado la [arquitectura](01-arquitectura.md) y el [modelo de datos](02-modelo-de-datos.md).

## 2. Archivos que intervienen

| Archivo | Responsabilidad |
|---|---|
| [`Main.java`](../src/main/java/edu/uv/biblioteca/Main.java) | Crea la conexión y los objetos de las distintas capas |
| [`ClassicTui.java`](../src/main/java/edu/uv/biblioteca/ui/ClassicTui.java) | Presenta el catálogo, interpreta teclas y abre formularios |
| [`LibraryService.java`](../src/main/java/edu/uv/biblioteca/service/LibraryService.java) | Valida la información y coordina las operaciones |
| [`BookDao.java`](../src/main/java/edu/uv/biblioteca/dao/BookDao.java) | Ejecuta consultas, altas, modificaciones y bajas |
| [`Book.java`](../src/main/java/edu/uv/biblioteca/model/Book.java) | Representa los campos que aparecen en la tabla de consulta |
| [`BookDetails.java`](../src/main/java/edu/uv/biblioteca/model/BookDetails.java) | Agrupa los campos utilizados en alta y modificación |
| [`SqlTrace.java`](../src/main/java/edu/uv/biblioteca/dao/SqlTrace.java) | Registra el SQL ejecutado para el modo didáctico |

La comunicación habitual sigue esta secuencia:

```text
Usuario
  │
  ▼
ClassicTui.tableBooks() / ClassicTui.editBook()
  │
  ▼
LibraryService
  │
  ▼
BookDao
  │
  ▼
PreparedStatement ──► PostgreSQL
  │                       │
  └──── ResultSet ◄───────┘
  │
  ▼
Objetos Book o BookDetails
  │
  ▼
Tabla, formulario o mensaje en la terminal
```

La interfaz se ocupa de presentar información. La clase DAO concentra las sentencias SQL. El servicio establece validaciones y comprueba que las escrituras se realicen en la base operativa.

## 3. Los modelos de datos en Java

La aplicación utiliza `record`, incorporado al lenguaje Java para representar grupos de datos de forma concisa.

El listado trabaja con cuatro campos:

```java
public record Book(
    int id,
    String title,
    String category,
    boolean available
) { }
```

El formulario necesita más información, por lo que utiliza `BookDetails`:

```java
public record BookDetails(
    int id,
    String title,
    String isbn,
    int year,
    String category,
    String publisher,
    int pages,
    BigDecimal price,
    boolean available,
    int authorId
) { }
```

Estos objetos **no son tablas de PostgreSQL**. Son representaciones Java que transportan información entre las clases. La diferencia entre ambos modelos permite que una tabla muestre pocas columnas y que un formulario edite el registro completo.

`BigDecimal` se utiliza para el precio en lugar de `double`, ya que ofrece una representación decimal apropiada para importes monetarios.

## 4. Consulta de todos los libros

El listado comienza cuando `ClassicTui.tableBooks()` llama a `service.listBooks()`. El servicio delega la operación en `BookDao.list()`.

En `BookDao`, la consulta está definida mediante un bloque de texto de Java:

```java
public static final String LIST_SQL = """
    SELECT id_libro, titulo, categoria, disponible
    FROM libros_consulta
    ORDER BY id_libro
    """;
```

La sentencia lee la vista `libros_consulta`. Esta vista reúne los nombres de las categorías y editoriales con las claves foráneas de la tabla real `libros`. El listado solo necesita cuatro columnas y las obtiene en orden de identificador.

Los métodos de acceso son:

```java
public List<Book> list() throws SQLException {
    return query(LIST_SQL, null);
}

public List<Book> search(String term) throws SQLException {
    return query(SEARCH_SQL, "%" + term + "%");
}
```

Ambos reutilizan el método privado `query()`. En el caso del listado, el segundo argumento es `null` porque la sentencia no utiliza parámetros.

### ¿Cómo se transforma el resultado SQL en objetos Java?

El método `query()` prepara la consulta, la ejecuta y recorre las filas devueltas:

```java
List<Book> books = new ArrayList<>();

try (PreparedStatement statement = SqlTrace.prepare(connection, sql)) {
    if (value != null) {
        statement.setString(1, value);
    }

    try (ResultSet rs = statement.executeQuery()) {
        while (rs.next()) {
            books.add(new Book(
                rs.getInt("id_libro"),
                rs.getString("titulo"),
                rs.getString("categoria"),
                rs.getBoolean("disponible")
            ));
        }
    }
}
return books;
```

El código anterior conserva el funcionamiento de `BookDao.query()`, con una distribución en varias líneas para facilitar su análisis.

Observe cuatro operaciones:

1. **`new ArrayList<>()`:** crea una lista inicialmente vacía.
2. **`SqlTrace.prepare(...)`:** prepara la sentencia JDBC y permite registrar su ejecución.
3. **`executeQuery()`:** obtiene un `ResultSet`, que contiene las filas devueltas por PostgreSQL.
4. **`while (rs.next())`:** avanza por cada fila y construye un objeto `Book` con los valores correspondientes.

El bloque `try-with-resources` cierra las sentencias y los resultados cuando termina su uso. La interfaz recibe una `List<Book>` y no necesita manejar directamente un `ResultSet`.

## 5. Búsqueda incremental: Java y PostgreSQL

El catálogo ofrece dos estrategias de búsqueda. En la terminal se alternan mediante **2. Cambiar filtro**.

### Filtrado en memoria

Al abrir la pantalla, `tableBooks()` recupera la lista completa:

```java
List<Book> complete = service.listBooks();
List<Book> filtered = complete;
```

En el modo Java, el término se compara con las propiedades de los objetos cargados:

```java
String term = search.toLowerCase(Locale.ROOT);

filtered = complete.stream()
    .filter(b -> (b.title() + " " + b.category() + " " + b.id())
        .toLowerCase(Locale.ROOT)
        .contains(term))
    .toList();
```

Esta operación no realiza otra consulta a PostgreSQL por cada carácter. El filtrado se produce sobre la colección existente. Es adecuado para estudiar `Stream`, expresiones lambda y comparación de cadenas.

### Búsqueda en PostgreSQL

En el modo SQL, el método `BookDao.search()` utiliza la siguiente sentencia:

```sql
SELECT
    id_libro,
    titulo,
    categoria,
    disponible
FROM libros_consulta
WHERE CONCAT_WS(' ', id_libro::text, titulo, categoria) ILIKE ?
ORDER BY titulo;
```

`CONCAT_WS` reúne los campos de búsqueda; `ILIKE` permite coincidencias sin distinguir mayúsculas; y `?` representa un parámetro de `PreparedStatement`.

El término se prepara de esta forma:

```java
public List<Book> search(String term) throws SQLException {
    return query(SEARCH_SQL, "%" + term + "%");
}
```

En `query()`, `statement.setString(1, value)` asocia el texto al primer parámetro. Los símbolos `%` permiten encontrar coincidencias parciales.

**Distinción importante:** en el modo PostgreSQL, la aplicación vuelve a ejecutar una consulta cuando cambia el texto; en el modo Java, solo filtra los objetos que ya tiene en memoria. Ambos mecanismos pueden producir las mismas coincidencias, pero no siguen el mismo recorrido de ejecución.

La concatenación de identificadores SQL no debe confundirse con la concatenación de valores introducidos por el usuario. En este caso, las entradas de búsqueda se envían como parámetros, no se insertan dentro del texto de la sentencia SQL.

## 6. Presentación y navegación en la terminal

`ClassicTui.tableBooks()` administra las variables de búsqueda y selección:

| Variable | Función |
|---|---|
| `complete` | Lista completa recuperada desde el servicio |
| `filtered` | Registros que coinciden con el filtro actual |
| `search` | Texto escrito por el usuario |
| `searching` | Indica si el teclado está capturando el filtro |
| `sqlMode` | Determina si el filtro se ejecuta en Java o PostgreSQL |
| `selected` | Posición del libro seleccionado |
| `offset` | Primera fila que se muestra dentro del área visible |

Las flechas modifican `selected`; cuando la selección supera el área visible, se ajusta `offset`. El método vuelve a dibujar el contenido mediante Lanterna.

El método también interpreta las operaciones `4` (alta), `5` (modificación) y `6` (eliminación). La selección se utiliza para obtener el identificador interno del libro; el usuario no tiene que escribirlo.

## 7. Captura y validación de un libro

`ClassicTui.editBook()` se utiliza tanto para agregar como para modificar. Recibe un `BookDetails` previo cuando se está editando un registro; para agregar recibe `null`.

La pantalla solicita autor, título, ISBN, año de publicación, categoría, editorial, páginas y precio. Autor, categoría y editorial pueden seleccionarse desde las listas correspondientes. Cuando sea necesario, el formulario permite incorporar valores nuevos.

Los datos se reúnen en `BookDetails` y se envían al servicio. Allí, `validateBook()` aplica las siguientes condiciones:

| Campo | Validación principal |
|---|---|
| Título | Obligatorio; máximo 180 caracteres |
| ISBN | Obligatorio; máximo 20 caracteres |
| Año | Entre 1450 y el año actual más uno |
| Categoría | Obligatoria; máximo 80 caracteres |
| Editorial | Opcional; máximo 100 caracteres |
| Páginas | Número mayor que cero |
| Precio | No negativo, máximo 999999.99 y hasta dos decimales |
| Autor | Debe resolverse una referencia válida |

La unicidad del ISBN se protege además con la restricción `UNIQUE` de PostgreSQL. Las validaciones de Java facilitan mensajes comprensibles, pero no reemplazan las restricciones de la base de datos.

**Detalle de implementación.** Cuando se captura un autor nuevo, el formulario asigna provisionalmente el valor `1` al campo `authorId` para pasar la validación previa. Ese número **no es el identificador definitivo**. El método de guardado registra primero al autor y utiliza el identificador generado por PostgreSQL. Es una solución del proyecto que debe comprenderse como provisional; una evolución posterior podría representar explícitamente el estado «autor pendiente de registrar» sin utilizar un identificador temporal.

## 8. Registro transaccional del libro

El método utilizado por el formulario es:

```java
service.saveBookWithSelections(
    book,
    create,
    newAuthor ? authorName : null,
    nationality
);
```

En `LibraryService`, la operación valida el libro y, si corresponde, el autor. Después comprueba que la conexión se dirija a `biblioteca_operativa` e invoca `BookDao.saveWithSelections()`.

El DAO realiza la operación dentro de una transacción:

```text
Inicio de transacción
   │
   ├── Registrar autor nuevo, si es necesario
   ├── Localizar o registrar categoría
   ├── Localizar o registrar editorial, si corresponde
   ├── Crear o actualizar el libro con sus claves foráneas
   │
   ├── ¿Todas las operaciones finalizaron correctamente?
   │      ├── Sí → COMMIT
   │      └── No → ROLLBACK
   ▼
Resultado o mensaje de error
```

Antes de comenzar, el método comprueba `connection.getAutoCommit()` para evitar iniciar su operación cuando ya existe otra transacción activa. Después ejecuta `connection.setAutoCommit(false)`.

El método auxiliar `ensureCatalog()` busca una categoría o editorial por nombre normalizado; si no existe, realiza su inserción y obtiene el nombre registrado. Los identificadores reales se resuelven con `catalogId()`, antes de insertar o actualizar el libro.

### Inserción en PostgreSQL

`BookDao.insert()` utiliza una sentencia equivalente a este fragmento del proyecto:

```sql
INSERT INTO libros (
    titulo, isbn, anio_publicacion, id_categoria,
    id_editorial, numero_paginas, precio, disponible, id_autor
)
VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?)
RETURNING id_libro;
```

Los parámetros se asignan mediante `values(stmt, b)`. El identificador del nuevo libro se obtiene desde el `ResultSet` producido por `RETURNING id_libro`. La disponibilidad inicial se establece en `TRUE`.

Si falla alguna instrucción, el DAO ejecuta `rollback()` y comunica el error. Esto evita, por ejemplo, que permanezca registrada una categoría nueva cuando el libro no pudo guardarse debido a un ISBN duplicado.

## 9. Modificación de un libro

Para modificar, el usuario selecciona una fila y pulsa `5`. La interfaz solicita los datos completos mediante `service.bookDetails(id)`, que utiliza `BookDao.details()` sobre `libros_consulta`.

Después el formulario presenta los valores actuales y permite cambiarlos. `BookDao.update()` ejecuta una sentencia `UPDATE libros ... WHERE id_libro = ?`. Si el número de filas modificadas es distinto de uno, la aplicación informa que el registro ya no existe.

La sentencia de actualización **no modifica `disponible`**. Ese estado corresponde a la gestión de préstamos y devoluciones, no al formulario bibliográfico.

## 10. Eliminación y restricciones de integridad

La eliminación comienza cuando el usuario selecciona un libro y pulsa `6`. La pantalla solicita confirmación antes de llamar al servicio.

`BookDao.delete()` ejecuta:

```sql
DELETE FROM libros
WHERE id_libro = ?;
```

El identificador procede del registro seleccionado y se envía con `stmt.setInt(1, id)`. La aplicación comprueba que se haya eliminado exactamente una fila.

Si el libro aparece en `prestamos`, aunque esos préstamos ya estén devueltos, **PostgreSQL impide su eliminación por la clave foránea**. La aplicación presenta el error, sin eliminar el historial. Esta validación ilustra la diferencia entre seleccionar una operación en la interfaz y poder realizarla conforme a las reglas de integridad.

## 11. Consultas relacionadas con las estadísticas

Aunque el catálogo tiene su propia pantalla, `BookDao` también participa en los reportes por categoría. La consulta `STATS_SQL` utiliza:

```sql
SELECT
    categoria,
    COUNT(*) AS total,
    COUNT(*) FILTER (WHERE disponible) AS disponibles
FROM libros_consulta
GROUP BY categoria
ORDER BY categoria;
```

El detalle de categoría se obtiene mediante `categoryBooks()`, que relaciona los libros con los préstamos abiertos y sus usuarios. Permite estudiar `LEFT JOIN` y comprender por qué un ejemplar puede figurar como no disponible.

## 12. Cómo estudiar y comprobar el módulo

Se propone la siguiente secuencia de trabajo:

1. Abra el catálogo y localice la consulta `LIST_SQL` en `BookDao.java`. Identifique de qué vista se obtiene cada columna.
2. Busque un título utilizando primero Java y después PostgreSQL. Compare las llamadas ejecutadas mediante el modo didáctico.
3. Registre un libro con un autor y una categoría nuevos. Localice `editBook()`, `saveBookWithSelections()` y `saveWithSelections()`.
4. Compruebe que el registro utiliza las claves foráneas esperadas:

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT id_libro, titulo, categoria, editorial FROM libros_consulta ORDER BY id_libro DESC LIMIT 5;"
```

5. Intente registrar el mismo ISBN con otro título y compruebe que el error no deja un autor o categoría sin libro.
6. Modifique un dato bibliográfico y observe que la disponibilidad no cambia.
7. Intente eliminar un libro que aparezca en el historial de préstamos. Identifique qué restricción impide la operación.

### Preguntas para la discusión

- ¿Por qué el formulario utiliza `BookDetails` mientras que la tabla de resultados trabaja con `Book`?
- ¿Qué responsabilidad tiene `LibraryService` que no debería trasladarse a `ClassicTui`?
- ¿Por qué `RETURNING id_libro` permite recuperar el identificador sin pedirlo al usuario?
- ¿En qué se diferencia filtrar con `Stream.filter()` de consultar con `ILIKE`?
- ¿Qué registros quedarían inconsistentes si el autor, la categoría y el libro se guardaran sin una transacción?
- ¿Por qué la eliminación de un libro con historial de préstamos debe rechazarse?

Para profundizar en las restricciones de la base de datos, consulte el [modelo relacional](02-modelo-de-datos.md); para verificar las operaciones de escritura, consulte [pruebas y mantenimiento](06-pruebas-y-mantenimiento.md).

**Alcance:** esta referencia explica el código existente de v9.0.5. No pretende convertirlo en una implementación de producción ni sustituye la revisión directa de los archivos fuente y de los resultados de PostgreSQL.
