# Registro de categorías y editoriales desde libros

Al agregar o modificar un libro, los campos **categoría** y **editorial** abren una ventana emergente con los valores disponibles. El cursor `▶` indica cuál está seleccionado. Para moverse utilice las flechas, `Page Up` y `Page Down`; `ENTER` selecciona y `ESC` cancela.

La primera opción de cada lista permite registrar una categoría o editorial nueva. Se abre una segunda ventana para escribir el nombre. Se eliminan espacios al inicio y al final y se unifican los espacios interiores; además se revisa si el nombre ya existe, sin distinguir mayúsculas. La editorial puede quedar sin especificar.

## ¿Cuándo se guarda el catálogo nuevo?

El nombre recién capturado **no se inserta inmediatamente**. Se conserva temporalmente mientras se llena el resto del formulario. Solo cuando el usuario confirma el libro, `ClassicTui` llama a `LibraryService.saveBookWithSelections(...)`.

El servicio revisa los valores del libro y verifica que la operación se realice sobre `biblioteca_operativa`. Después `BookDao.saveWithSelections(...)` inicia una transacción, busca las categorías y editoriales por nombre, inserta las que faltan, agrega al autor si procede y guarda el libro con las claves foráneas correspondientes. Si falla cualquiera de estas operaciones, JDBC realiza `rollback()`.

## Pruebas sugeridas

1. Agregue un libro con una categoría nueva y una editorial nueva: deben aparecer en los catálogos al confirmar.
2. Abra otro libro y seleccione esos valores de las listas: no deben crearse duplicados.
3. Inicie un alta con una categoría nueva y pulse `ESC` antes de guardar: el catálogo debe permanecer igual.
4. Intente guardar un libro con ISBN duplicado y categoría nueva: la categoría no debe persistir.
5. Modifique un libro y asigne una editorial nueva: el cambio debe quedar registrado y consultarse desde `libros_consulta`.
6. Registre también un autor nuevo en el mismo formulario: los registros deben confirmarse o revertirse juntos.

Los identificadores de categoría, editorial y autor son internos; el usuario trabaja con nombres y listas.
