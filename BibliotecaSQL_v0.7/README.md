# BibliotecaSQL v0.7 — Interfaz clásica interactiva

Proyecto didáctico Java 17, PostgreSQL, JDBC, Maven, Docker Compose y Lanterna.

## Inicio

```bash
docker compose up -d --wait
mvn compile exec:java
```

Abra una terminal de al menos 80 × 24 caracteres. En Windows se recomienda Windows Terminal; en Linux y macOS una terminal compatible con UTF-8.

**La nueva interfaz TUI utiliza Lanterna** para evitar depender de teclas Fn, comandos `clear` y lecturas con ENTER. Los módulos de JDBC, reglas de negocio y laboratorio A7–A9 se conservan del proyecto previo.

## Controles

- Menús: números 1–9, 0 regresar, ↑↓ y ENTER.
- Catálogo: 1 o / para buscar en vivo; 2 para alternar memoria/SQL; ↑↓, PgUp/PgDn para navegar; ENTER para detalles.
- Durante la búsqueda: letras y números editan el filtro; retroceso elimina caracteres; ENTER finaliza la edición y ESC cancela su edición.
- En reportes: flechas y PgUp/PgDn para recorrer filas; 0 o ESC para salir.

La búsqueda SQL utiliza `PreparedStatement` para pasar parámetros a PostgreSQL. El modo en memoria filtra la colección Java sin tocar la base.

## Alcance actual

La búsqueda incremental funciona en todas las tablas. La alternancia entre filtrado Java y PostgreSQL se ofrece en el catálogo de libros; los demás reportes se filtran en memoria. La base de datos académica sigue separada de la operativa. Los 21 ejercicios de A7, A8 y A9 de la versión anterior se conservan.

## Documentación

- `docs/11-interfaz-clasica-v07.md`: teclado, búsqueda y responsabilidades de clases.
- Documentos `docs/01` a `docs/10`: arquitectura, transacciones, pruebas y laboratorios.

## Pruebas

```bash
bash scripts/probar-integracion.sh
```

La interfaz requiere interacción humana para validar sus eventos de teclado; las pruebas de integración existentes siguen cubriendo las reglas de negocio. Ejecutar `mvn test` requiere Maven y los servicios de prueba configurados.

## Nota de validación

El código fuente fue revisado estáticamente, pero en el entorno de elaboración no se dispuso de Maven ni del JAR Lanterna para confirmar la compilación del nuevo módulo. Se debe ejecutar `mvn compile exec:java` y registrar cualquier error antes de usarlo con estudiantes.

## Requisitos

- Java 17 o posterior
- Maven 3.9 o posterior
- Docker con Docker Compose
- Terminal UTF-8 para visualizar los marcos y caracteres de las tablas

## Ejecución

```bash
docker compose up -d
mvn compile exec:java
```

Espere a que PostgreSQL esté listo antes de iniciar Java. El arranque inicial utiliza `database/01-inicializar.sql` y no depende de permisos de ejecución de scripts de shell. La carga automática de datos ocurre únicamente cuando el volumen de Docker es nuevo. Para reiniciar desde cero, consulte primero los datos que desea conservar: `docker compose down -v` elimina el volumen de PostgreSQL.

La conexión predeterminada es `jdbc:postgresql://localhost:5432/biblioteca_operativa`, usuario `admin`, contraseña local de práctica `biblioteca_local`. Puede cambiarla mediante `DB_URL`, `DB_USER` y `DB_PASSWORD`. No utilice estas credenciales en servidores públicos.

Para forzar limpieza ANSI en una terminal compatible, configure `FORCE_ANSI=true`. Sin compatibilidad ANSI se utilizan saltos de línea.

## Opciones

1. Consultar catálogo.
2. Buscar libros por título.
3. Consultar préstamos activos.
4. Consultar estadísticas por categoría.
5. Registrar préstamo.
6. Registrar devolución.
7. Consultar préstamos vencidos.
8. Contar préstamos activos por usuario.
9. Abrir el laboratorio SQL (7 consultas).
D. Activar o desactivar la visualización didáctica de consultas SQL.
0. Salir.

Las dos primeras bases siguen separadas: `biblioteca_universitaria` conserva los datos originales y `biblioteca_operativa` permite operaciones. Los cambios transaccionales se bloquean si el nombre de la base no es `biblioteca_operativa`.

El modelo original incluye `autores`, `libros`, `usuarios` y `prestamos`. No tiene tabla independiente para editoriales ni múltiples ejemplares de un libro.

## Documentación

- `docs/01-arquitectura.md`: organización de clases.
- `docs/02-matriz-sql.md`: relación inicial con los contenidos SQL.
- `docs/03-transacciones.md`: reglas y transacciones de préstamos.
- `docs/04-laboratorio.md`: consultas y ejercicios del laboratorio.
- `docs/05-reportes.md`: reportes de vencidos y usuarios.
- `docs/06-guia-codigo.md`: recorrido para leer el código fuente.
- `docs/07-pruebas.md`: ejecución y alcance de las pruebas.
- `docs/08-matriz-trazabilidad.md`: correspondencia entre consultas y contenidos del segundo parcial.

## Pruebas

Para ejecutar las pruebas de la interfaz y de las transacciones en una base independiente:

```bash
bash scripts/probar-integracion.sh
```

Este script inicia PostgreSQL de pruebas en `127.0.0.1:55433`, ejecuta `mvn test` y elimina el entorno temporal. **No borra los datos del contenedor de trabajo**. Para ejecutar solamente las pruebas de interfaz, utilice `mvn test` sin `DB_TEST_URL`.

Se incluyen diez pruebas JUnit: tres de consola y siete de integración. Las pruebas de integración quedan omitidas si no se ejecutan mediante el entorno de prueba. Esta entrega no se ha ejecutado contra Docker desde el entorno de construcción, por lo que es necesario validar la suite en un equipo con Docker y Maven disponibles.

Lea `docs/07-pruebas.md` para conocer las verificaciones y `docs/08-matriz-trazabilidad.md` para revisar la correspondencia de los contenidos SQL con el código Java.

Fuente de datos: https://gist.github.com/jgdiaz19/af0426289725f559646fec9654253a5f

## Si actualiza desde v0.5 después del error 126

El intento de inicialización anterior pudo haber creado un volumen incompleto. Si es una instalación nueva y no tiene datos que conservar, ejecute `docker compose down -v` **desde la carpeta de la v0.5**, y después inicie esta versión con `docker compose up -d --wait`. El comando `down -v` borra los datos persistentes del proyecto indicado: no lo ejecute si necesita conservarlos. Compruebe el arranque con `docker compose logs postgres` y `docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT COUNT(*) FROM libros;"`. Debe indicar 24 libros.


## Novedades de la versión 0.6

- Splash de apertura con monograma tipográfico `UV` (no es el escudo oficial).
- Limpieza de pantalla al navegar y pausas al mostrar resultados.
- Colores inspirados en la identidad UV (`#18529D` y `#28AD56`) con opción `C` para desactivarlos.
- Tres submenús de laboratorio: A7, A8 y A9; siete consultas de solo lectura por bloque.
- Comentarios cortos en el código y guías en `docs/09-interfaz-institucional.md` y `docs/10-laboratorios-segundo-parcial.md`.

**Uso**: `docker compose up -d --wait` y `mvn compile exec:java`. Si ya existe el volumen de la versión anterior, los datos se conservan; no es necesario usar `down -v`. Para activar ANSI en una terminal sin detección automática: `FORCE_ANSI=true mvn compile exec:java`. Para suprimir colores: `NO_COLOR=1 mvn compile exec:java`.

**Validación**: los ejemplos A8/A9 son temáticos y requieren cotejo posterior con los ejercicios originales. Las pruebas de integración necesitan Docker y Maven.
