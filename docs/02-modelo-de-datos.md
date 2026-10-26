# Modelo de datos y normalización

**Versión:** v9.0.5. La aplicación trabaja con **dos bases de datos independientes** para conservar los ejercicios originales y permitir operaciones de escritura.

## Dos bases y sus propósitos

| Base | Uso | Características |
|---|---|---|
| `biblioteca_universitaria` | Ejercicios académicos A7–A9 | Esquema original con `autores`, `libros`, `usuarios` y `prestamos` |
| `biblioteca_operativa` | Aplicación BibliotecaSQL | Estructura normalizada, catálogos independientes, préstamos y reportes |

El sistema se conecta por defecto a **`biblioteca_operativa`** y comprueba ese nombre antes de ciertas escrituras. No modifique la base académica durante las prácticas.

## Esquema relacional operativo

- `autores` (1) → (N) `libros`, mediante `id_autor`.
- `categorias` (1) → (N) `libros`, mediante `id_categoria`.
- `editoriales` (1) → (N) `libros`, mediante `id_editorial` (opcional).
- `libros` (1) → (N) `prestamos`, mediante `id_libro`.
- `usuarios` (1) → (N) `prestamos`, mediante `id_usuario`.

Cada fila de `libros` representa **un ejemplar**; no existe un campo que indique múltiples copias. El atributo `disponible` muestra si el ejemplar puede prestarse.

## Qué cambió con la normalización de v9

En la base original, `categoria` y `editorial` eran columnas de texto de `libros`. La migración creó `categorias(id_categoria, nombre)` y `editoriales(id_editorial, nombre)` y sustituyó el texto repetido por claves foráneas. Ambas tablas tienen controles para evitar nombres vacíos y duplicados que solo varían en mayúsculas.

Para presentar los nombres se creó la vista `libros_consulta`. Su lógica se basa en relaciones como:

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

`LEFT JOIN` conserva libros que no tienen editorial. La vista facilita las **consultas de lectura**; las altas y modificaciones se realizan sobre las tablas reales.

## Fechas de los préstamos

| Columna | Significado |
|---|---|
| `fecha_prestamo` | Día en que inicia el préstamo |
| `fecha_devolucion` | **Fecha límite** acordada |
| `fecha_devolucion_real` | Fecha en la que efectivamente se devuelve; `NULL` si permanece abierto |

PostgreSQL almacena las fechas como `DATE`, aunque la interfaz las presenta en formato **DD-MM-AAAA**.

El script `database/04-conciliar.sql` ajusta la disponibilidad de los libros según los préstamos abiertos y crea un índice único parcial que evita dos préstamos activos simultáneos para un mismo libro.

## Scripts de inicialización

Con un **volumen PostgreSQL nuevo**, Docker ejecuta `database/01-inicializar.sql`, que crea ambas bases. Los archivos utilizados son:

| Archivo | Función |
|---|---|
| `02-esquema.sql` | Crea las cuatro tablas originales y sus restricciones |
| `03-datos.sql` | Inserta los datos de ejemplo |
| `04-conciliar.sql` | Ajusta disponibilidad e índice de préstamo activo en la base operativa |
| `05-normalizar-catalogos.sql` | Normaliza categorías y editoriales únicamente en la base operativa |

La inicialización automática **no se repite** al ejecutar `docker compose up` sobre un volumen ya existente.

## Comprobar el esquema

Desde la raíz del repositorio, con Docker iniciado:

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT COUNT(*) FROM libros;"
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT titulo, categoria, editorial FROM libros_consulta LIMIT 5;"
docker compose exec postgres psql -U admin -d biblioteca_universitaria -c "SELECT COUNT(*) FROM libros;"
```

La carga de referencia contiene 24 libros. La base operativa puede mostrar más o menos según las operaciones realizadas.

## Migración desde versiones anteriores

La versión 9.0.5 recién instalada **no necesita migración manual**. Si conserva un volumen operativo anterior a v9, realice un respaldo, confirme que está utilizando el volumen correcto y, solo entonces, ejecute:

```bash
bash scripts/migrar-v9.sh
```

El script genera primero un respaldo con `pg_dump` y aplica la migración en `biblioteca_operativa`. Nunca use el script sobre `biblioteca_universitaria`, ni elimine el volumen para forzar una migración.
