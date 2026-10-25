# BibliotecaSQL v9.0.5

Aplicación didáctica Java 17 + PostgreSQL + JDBC + Lanterna, Universidad Veracruzana.

## Qué cambia

- Dos catálogos relacionales independientes: `categorias` y `editoriales`.
- La tabla operativa `libros` almacena `id_categoria` (obligatorio) e `id_editorial` (opcional) mediante claves foráneas.
- Se eliminaron las columnas de texto duplicadas `categoria` y `editorial` **solo de la base operativa**.
- La vista `libros_consulta` devuelve sus nombres para consultas y reportes sin duplicarlos.
- Administración con altas, búsqueda, modificación y bajas de autores, categorías y editoriales desde la opción **2. Administrar catálogos**.
- El formulario de libros utiliza valores de esos catálogos. Desde el mismo formulario puede seleccionar **+ Registrar nueva categoría** o **+ Registrar nueva editorial**. Se abre una ventana emergente; el catálogo nuevo se guarda únicamente al confirmar el libro.
- Un catálogo con libros relacionados no puede eliminarse. Una editorial sigue siendo opcional.
- `biblioteca_universitaria` conserva el esquema original y sus 24 libros para reproducir A7–A9.
- Las 21 consultas del laboratorio se ejecutan en `biblioteca_operativa` usando la vista `libros_consulta`; es un esquema de lectura equivalente, no idéntico al esquema original.

## Ajuste v9.0.3

- Ventanas emergentes navegables para elegir o registrar categoría y editorial durante el alta o modificación de libros.
- `ESC` cancela la selección; un valor nuevo solo se almacena al confirmar el libro.
- Autor, categoría, editorial y libro nuevos se guardan en una misma transacción. Si falla una parte, se revierte toda la operación.
- No se modifican las tablas ni se necesita ejecutar una migración adicional desde v9.0.

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


## Cambios de la versión 9.0.3

- Catálogo de libros: se reduce el ancho del título y se reserva espacio para mostrar categoría. La opción 2 se llama **Cambiar filtro** y alterna entre búsqueda en Java (memoria) y PostgreSQL (ILIKE).
- Autores, categorías y editoriales: tablas con búsqueda incremental (`1` o `/`), cursor `▶`, navegación, detalle con ENTER, alta (`4`), modificación (`5`) y eliminación (`6`).
- Préstamos activos: tabla con búsqueda y acceso a registrar (`4`), modificar fecha (`5`), devolver (`6`) y cancelar (`7`).
- Los identificadores se obtienen internamente de la fila seleccionada. Las confirmaciones y restricciones SQL se conservan.
- La base académica y el esquema operativo no cambian; esta versión no necesita migración.


## Novedades de la v9.0.3

- Los préstamos activos muestran fecha de inicio y fecha límite en formato día-mes-año.
- El registro de préstamos permite crear usuarios y elegir un plazo de 1 a 365 días.
- Las devoluciones se seleccionan desde una tabla con fechas diferenciadas.
- Estadísticas por categoría y préstamos por usuario incluyen detalles accesibles con ENTER.
- El reporte de vencidos separa fecha de préstamo y vencimiento.
- No se requieren migraciones adicionales del esquema para esta actualización.

Consulta `docs/21-prestamos-reportes-y-fechas.md` para los casos de prueba.


## Laboratorio SQL ampliado (v9.0.4)

- 36 consultas de solo lectura: 12 para A7, 12 para A8 y 12 para A9.
- La selección del ejercicio tiene apuntador, flechas, búsqueda con `/`, Page Up/Down y ENTER.
- En el modo didáctico se puede consultar SQL, Java y las explicaciones desde las tablas de resultados.
- Los ejercicios leen la base `biblioteca_operativa`. Los que emplean `libros_consulta` leen una vista, no la tabla física.
- No se modifica ningún dato, tabla o volumen en esta actualización.
- Revisar `docs/22-auditoria-laboratorio-sql.md` para conocer limitaciones y verificaciones pendientes.

## Actualización v9.0.5

- La pantalla de bienvenida identifica a la Facultad de Negocios y Tecnologías, Campus Ixtaczoquitlán, y a la Universidad Veracruzana.
- Se añadió el crédito «Designed and Developed by JEGD».
- Se actualizó a v9.0.5 el número de versión mostrado en la pantalla de bienvenida y el proyecto Maven.
- No hay cambios en la base de datos, consultas ni reglas de negocio.


## Historia y seguimiento del desarrollo

Este repositorio tiene un historial Git **reconstruido** desde los paquetes de versiones. Consulte [la cronología](HISTORIAL.md), [el plan de tareas](PROJECTS_PLAN.md) y [las instrucciones de publicación](PUBLICAR_EN_GITHUB.md).
