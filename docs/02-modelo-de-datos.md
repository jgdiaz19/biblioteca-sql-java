# Modelo de datos y normalización

## 1. Bases de datos utilizadas

BibliotecaSQL mantiene dos bases independientes:

| Base | Propósito | Características |
|---|---|---|
| `biblioteca_universitaria` | Conservar los datos de las actividades originales | Tablas `autores`, `libros`, `usuarios` y `prestamos` |
| `biblioteca_operativa` | Ejecutar las operaciones del sistema | Catálogos normalizados, préstamos, reportes y vista `libros_consulta` |

La aplicación se conecta de manera predeterminada a **`biblioteca_operativa`**. La base académica debe mantenerse sin modificaciones para que los ejercicios con resultados esperados continúen siendo reproducibles.

## 2. Estructura relacional

En la base operativa se utilizan las siguientes relaciones:

- Un **autor** puede tener varios libros; cada libro referencia un autor mediante `id_autor`.
- Una **categoría** puede agrupar varios libros; cada libro referencia una categoría mediante `id_categoria`.
- Una **editorial** puede estar asociada con varios libros. `id_editorial` es opcional.
- Un **usuario** puede tener varios préstamos; cada préstamo referencia un usuario.
- Un **libro** puede aparecer en varios préstamos históricos, pero solo en uno activo a la vez.

Cada registro de `libros` representa **un ejemplar**. El proyecto no administra inventario con varias copias del mismo registro. El indicador `disponible` se actualiza durante los préstamos y las devoluciones.

## 3. Normalización de categorías y editoriales

El esquema académico conserva `categoria` y `editorial` como texto en `libros`. En la base operativa, la versión v9 separó estos valores en las tablas `categorias` y `editoriales`. Esta decisión reduce la duplicación de nombres y permite administrar los catálogos sin modificar los libros individualmente.

La vista `libros_consulta` presenta los nombres a partir de sus relaciones:

```sql
SELECT
    l.id_libro,
    l.titulo,
    c.nombre AS categoria,
    e.nombre AS editorial
FROM libros AS l
JOIN categorias AS c
    ON c.id_categoria = l.id_categoria
LEFT JOIN editoriales AS e
    ON e.id_editorial = l.id_editorial;
```

El `LEFT JOIN` preserva libros sin editorial. Las tablas de categoría y editorial incluyen restricciones para nombres no vacíos e índices que impiden duplicados que solo difieren por el uso de mayúsculas.

La vista se utiliza para **consultar** información; las altas y modificaciones se realizan sobre las tablas originales, utilizando los identificadores correspondientes.

## 4. Fechas de préstamo

La tabla `prestamos` distingue tres campos:

| Columna | Significado |
|---|---|
| `fecha_prestamo` | Fecha de inicio |
| `fecha_devolucion` | Fecha límite acordada |
| `fecha_devolucion_real` | Fecha efectiva de devolución, nula mientras el préstamo esté activo |

Las columnas se almacenan como `DATE` en PostgreSQL y se muestran en formato **DD-MM-AAAA** en la terminal.

Un índice único parcial, `uq_prestamo_activo_libro`, impide que un libro tenga dos préstamos abiertos (`fecha_devolucion_real IS NULL`). El script de conciliación también establece el valor inicial de `disponible` según los préstamos registrados.

## 5. Inicialización y migración

En una instalación con volumen nuevo, `database/01-inicializar.sql` crea las dos bases y ejecuta los siguientes archivos:

| Archivo | Función |
|---|---|
| `02-esquema.sql` | Crear las tablas del modelo original |
| `03-datos.sql` | Insertar los datos de ejemplo |
| `04-conciliar.sql` | Conciliar disponibilidad e incorporar el índice de préstamo activo en la base operativa |
| `05-normalizar-catalogos.sql` | Transformar categorías y editoriales en catálogos relacionados; solo en la base operativa |

La inicialización automática ocurre al crear el volumen. Volver a ejecutar `docker compose up` **no restablece los datos** si el volumen ya existe.

Para comprobar el contenido:

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT COUNT(*) FROM libros;"
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT titulo, categoria, editorial FROM libros_consulta LIMIT 5;"
```

El conjunto de referencia incluye 24 libros. La cantidad en la base operativa puede cambiar tras altas y bajas.

**Migración desde versiones anteriores:** si se conserva un volumen anterior a v9, revise su esquema y cree un respaldo antes de ejecutar `bash scripts/migrar-v9.sh`. La instalación nueva de v9.0.5 ya incorpora la normalización y no necesita ese paso. Nunca ejecute la migración sobre `biblioteca_universitaria` ni elimine volúmenes para forzarla.
