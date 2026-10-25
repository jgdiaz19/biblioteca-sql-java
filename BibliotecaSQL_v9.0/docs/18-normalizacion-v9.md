# Normalización de categorías y editoriales

Anteriormente `libros.categoria` y `libros.editorial` guardaban texto. Esto permitía introducir nombres duplicados o escribir variantes distintas del mismo valor.

La migración `database/05-normalizar-catalogos.sql` crea `categorias(id_categoria, nombre)` y `editoriales(id_editorial, nombre)`, traslada los valores existentes, agrega las claves foráneas en `libros` y elimina los textos redundantes en la base operativa.

Para leer los nombres se utiliza `libros_consulta`, una vista que relaciona `libros`, `categorias` y `editoriales` mediante `JOIN` y `LEFT JOIN`. El `LEFT JOIN` permite libros sin editorial.

## Práctica propuesta

1. Registrar una categoría desde el menú 2.
2. Registrar una editorial nueva.
3. Agregar un libro seleccionando ambos valores desde sus listas.
4. Modificar el nombre de la categoría y comprobar que todos los libros relacionados reflejan el cambio.
5. Intentar eliminar una categoría usada y observar la protección de clave foránea.
6. Consultar el laboratorio SQL y comparar la vista con la tabla normalizada.

## Limitaciones

La base académica conserva el esquema original de A7–A9; no debe migrarse. La migración existente debe ejecutarse solo una vez preparado el respaldo. `biblioteca_operativa` no debe confundirse con `biblioteca_universitaria`.
