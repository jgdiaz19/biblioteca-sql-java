# Catálogos de autores, categorías y editoriales

## Catálogo de autores

Desde el menú principal, pulse `2` y seleccione un autor con las flechas. El listado ofrece búsqueda con `/` y desplazamiento con las flechas. La primera opción permite registrar un autor nuevo. Para un autor existente puede consultar, modificar o solicitar su eliminación.

Al guardar, se validan el nombre y la nacionalidad. Los identificadores se manejan internamente mediante JDBC. Si el autor tiene libros asociados, PostgreSQL no permite su eliminación por la clave foránea `libros.id_autor`.

## Categorías y editoriales

Al registrar o modificar un libro, la categoría y la editorial se seleccionan en una lista. La aplicación construye estas listas con `SELECT DISTINCT` sobre los valores presentes en `libros` de la base operativa. Se conservan las tablas originales para que el laboratorio A7–A9 no cambie su esquema.

La opción «Registrar nuevo valor» abre una ventana de captura. Antes de aceptarlo, se eliminan espacios sobrantes y se comprueba que no exista otra opción con el mismo texto sin distinguir mayúsculas. La nueva denominación queda registrada cuando se guarda el libro. Para editorial también se permite «Sin editorial».

**Alcance actual:** las categorías y editoriales son vocabularios derivados de la tabla `libros`, no tablas maestras independientes. Si se borra el último libro de una editorial, esa editorial dejará de aparecer como opción. El control de duplicados nuevos se realiza en Java, no mediante una restricción UNIQUE de PostgreSQL. Para un sistema de producción convendría normalizar a tablas `categorias` y `editoriales` y migrar gradualmente las referencias.

## Lectura del código

- `AuthorDao`: consultas SQL para autores y sus operaciones de alta, modificación y baja.
- `LibraryService`: validaciones y protección de la base de datos académica frente a escritura.
- `BookDao`: consultas `DISTINCT` que alimentan los selectores de categoría y editorial.
- `ClassicTui`: ventanas, controles y selección de registros sin ingresar ID.

## Pruebas de aceptación

1. Registrar un autor nuevo, modificarlo y localizarlo usando `/`.
2. Intentar eliminar un autor asociado a un libro y comprobar que se muestre un error.
3. Registrar un libro seleccionando categoría y editorial existentes.
4. Registrar un libro con una categoría nueva y comprobar que aparece después en la lista.
5. Intentar dar de alta la misma editorial cambiando mayúsculas y comprobar que se solicite elegir el valor existente.
6. Activar el modo didáctico con `D` en el menú principal y examinar las consultas.

Estas pruebas deben ejecutarse con PostgreSQL real en el equipo docente antes de distribuir la versión.
