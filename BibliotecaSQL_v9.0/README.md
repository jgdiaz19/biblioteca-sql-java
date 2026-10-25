# BibliotecaSQL v9.0

Aplicación didáctica Java 17 + PostgreSQL + JDBC + Lanterna, Universidad Veracruzana.

## Qué cambia

- Dos catálogos relacionales independientes: `categorias` y `editoriales`.
- La tabla operativa `libros` almacena `id_categoria` (obligatorio) e `id_editorial` (opcional) mediante claves foráneas.
- Se eliminaron las columnas de texto duplicadas `categoria` y `editorial` **solo de la base operativa**.
- La vista `libros_consulta` devuelve sus nombres para consultas y reportes sin duplicarlos.
- Administración con altas, búsqueda, modificación y bajas de autores, categorías y editoriales desde la opción **2. Administrar catálogos**.
- El formulario de libros utiliza valores de esos catálogos. Para crear uno nuevo, registre primero el valor desde **2. Administrar catálogos** y regrese al libro.
- Un catálogo con libros relacionados no puede eliminarse. Una editorial sigue siendo opcional.
- `biblioteca_universitaria` conserva el esquema original y sus 24 libros para reproducir A7–A9.
- Las 21 consultas del laboratorio se ejecutan en `biblioteca_operativa` usando la vista `libros_consulta`; es un esquema de lectura equivalente, no idéntico al esquema original.

## Primera instalación (sin volúmenes previos)

```bash
docker compose up -d --wait
mvn compile exec:java
```

El script de arranque de PostgreSQL crea ambas bases y normaliza únicamente la base operativa.

## Actualizar una instalación existente de v0.8.6

**No ejecute `docker compose down -v`: eliminaría los datos del volumen.**

1. Descomprima v9.0 y abra esa carpeta en la terminal.
2. Confirme que usa el mismo proyecto/volumen Docker Compose anterior. La carpeta nueva normalmente crea **otro nombre de proyecto y otro volumen**, por lo que, si desea conservar los registros de la versión anterior, utilice `docker compose -p NOMBRE_ANTERIOR` en todos los comandos (o establezca `COMPOSE_PROJECT_NAME`). Obtenga el nombre con `docker volume ls`.
3. Arranque PostgreSQL con `docker compose up -d --wait`.
4. Ejecute la migración con `bash scripts/migrar-v9.sh`. El script hace primero un respaldo SQL.
5. Inicie Java con `mvn compile exec:java`.

Si arrancó un volumen **nuevo y vacío**, la normalización ya se aplicó al inicializarlo. En ese caso no hace falta ejecutar el script migrador.

## Comprobaciones

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT count(*) FROM libros;"
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT count(*) FROM categorias;"
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT count(*) FROM editoriales;"
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT id_libro, titulo, categoria, editorial FROM libros_consulta ORDER BY id_libro LIMIT 5;"
docker compose exec postgres psql -U admin -d biblioteca_universitaria -c "SELECT count(*) FROM libros;"
```

## Reglas

- Los registros de categoría/editorial se validan por nombre sin distinguir mayúsculas.
- PostgreSQL protege las relaciones mediante claves foráneas.
- Si intenta eliminar una categoría, editorial o autor que tenga libros asociados, se muestra un error y no se pierde información.
- La capa JDBC conserva la traza de consultas del modo didáctico.

## Estado de validación

El código fuente compiló mediante `javac --release 17` con clases auxiliares de la API de Lanterna. No se ha realizado aún un ensayo integral con Docker/PostgreSQL y Lanterna reales. La migración debe probarse con respaldo antes de utilizarla con datos importantes.
