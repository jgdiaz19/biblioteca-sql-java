# Registro de autores y libros

La tabla `libros` tiene una clave foránea `id_autor` que apunta a `autores`. El autor puede ser nuevo, pero siempre debe existir cuando se inserta el libro.

## Desde la aplicación

1. Entre al catálogo y elija **4 Agregar** o seleccione un libro y elija **5 Modificar**.
2. En la lista de autores elija **Registrar nuevo autor** o uno de los existentes.
3. Para un autor nuevo capture **nombre** y **nacionalidad** (opcional).
4. Complete los campos del libro y confirme. El autor solo se guarda después de confirmar.
5. Si ocurre un error, se informa el motivo sin dejar medio registro en la base.

## Recorrido de la implementación

- `ClassicTui.editBook()` recoge los datos y presenta las ventanas.
- `LibraryService` valida los campos y protege la base académica.
- `BookDao.saveWithNewAuthor()` abre la transacción, inserta el autor, usa el ID generado, guarda el libro y confirma con `COMMIT`.
- Si una operación falla, `ROLLBACK` deshace ambos cambios.

No se creó una nueva tabla ni se modificaron las relaciones originales.

## Pruebas manuales sugeridas

1. Registrar un libro con autor nuevo y comprobar ambas tablas.
2. Registrar un libro con autor existente y comprobar que no se duplica.
3. Intentar repetir un ISBN y comprobar que el autor nuevo no quedó registrado.
4. Modificar un libro y asignarle un nuevo autor.
5. Cancelar el formulario y verificar que no se guardó ningún registro.
6. Intentar registrar un autor con nombre ya existente: debe pedir que se elija de la lista.
