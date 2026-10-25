# BibliotecaSQL — v0.3

Proyecto didáctico Java 17, JDBC y PostgreSQL para la EE Bases de Datos. La interfaz recuerda las aplicaciones de consola tradicionales: menús numéricos, tablas alineadas, encabezados y limpieza ANSI cuando la terminal es compatible.

Fuente del caso: https://gist.github.com/jgdiaz19/af0426289725f559646fec9654253a5f

## Requisitos

- Java 17 o posterior; Maven 3.9 o posterior
- Docker y Docker Compose
- Terminal compatible con UTF-8 para los bordes Unicode

## Iniciar desde cero

```bash
docker compose up -d
mvn compile exec:java
```

La imagen de PostgreSQL se inicializa automáticamente solo cuando el volumen es nuevo. Espere a que el contenedor esté saludable si la primera conexión falla. `FORCE_ANSI=true` habilita limpieza mediante secuencias ANSI si Java no detecta terminal interactiva. Se permite suprimirla dejando esa variable sin configurar.

En Windows PowerShell:
```powershell
docker compose up -d
mvn compile exec:java
```

## Configuración

Por defecto: `jdbc:postgresql://localhost:5432/biblioteca_operativa`, usuario `admin`, contraseña `biblioteca_local`. Configure opcionalmente `DB_URL`, `DB_USER`, `DB_PASSWORD` como variables de entorno. La contraseña incluida sirve exclusivamente para prácticas en el equipo local. No publique credenciales reales ni abra el puerto de PostgreSQL a Internet.

## Bases independientes

- `biblioteca_universitaria`: datos originales para consultas reproducibles de A7–A9; no modificar.
- `biblioteca_operativa`: copia de datos con indicador de disponibilidad conciliado; preparada para operaciones posteriores.

La base inicial contiene 12 autores, 24 libros, 10 usuarios y 18 préstamos. No hay entidad de ejemplares ni tabla de editoriales en el modelo original. El préstamo está asociado al registro del libro.

## Funciones disponibles

1. Listar catálogo.
2. Buscar título con `PreparedStatement`.
3. Listar préstamos activos mediante `JOIN`.
4. Agrupar libros por categoría y disponibilidad mediante `GROUP BY`.
5. Registrar préstamo mediante transacción JDBC.
6. Registrar devolución mediante transacción JDBC.
7. Activar o desactivar el modo didáctico SQL.

**Esta versión permite modificar exclusivamente `biblioteca_operativa`.** El servicio transaccional verifica el nombre de la base y rechaza escrituras si es distinto; `biblioteca_universitaria` sigue reservada para las consultas de A7–A9.

## Reglas implementadas en v0.3

- RB-01: usuario existente y activo.
- RB-02: no más de tres préstamos activos por usuario.
- RB-03: libro existente, disponible y sin otro préstamo activo.
- RB-04: plazo de catorce días a partir de la fecha del sistema.
- RB-05: una devolución no puede registrarse dos veces ni anteceder al préstamo.
- RB-06: inserción del préstamo y actualización del libro forman una sola transacción; devolución y liberación del libro también.
- RB-07: `SELECT ... FOR UPDATE` protege filas involucradas y el índice parcial `uq_prestamo_activo_libro` impide préstamos activos simultáneos de un mismo libro.

La base operativa inicia con préstamos históricos abiertos de 2026. Para ensayar una operación nueva, consulte primero la opción 1 y seleccione un libro disponible y un usuario activo con menos de tres préstamos abiertos. Se utilizan las fechas locales del equipo Java; no se permite devolver un préstamo fechado en el futuro.

## Secuencia de comprobación manual

1. Consulte el catálogo y anote un identificador de libro disponible.
2. Registre un préstamo para un usuario activo elegible y conserve el ID generado.
3. Compruebe que el libro pasó a no disponible y que el préstamo aparece en el listado de activos.
4. Intente prestar de nuevo el mismo libro; debe rechazarse.
5. Registre la devolución con el ID del préstamo.
6. Compruebe que el libro vuelve a disponible y que el préstamo ya no figura entre los activos.
7. Intente devolver el mismo préstamo otra vez; debe rechazarse.
8. Consulte la base académica por separado para confirmar que sus registros no cambiaron.

Para evitar perder datos, Docker conserva un volumen nombrado. Si ya existe un volumen de v0.2, no es necesario reinicializarlo cuando contiene la tabla `prestamos` y el índice de unicidad activo. Si se eliminan los volúmenes, se pierde el trabajo previo.

**Limitación de validación:** la compilación y pruebas básicas Java fueron revisadas, pero no se completó una ejecución integral real con PostgreSQL en este entorno. Se requiere validarla localmente antes de emplearla en una práctica evaluada.

## Organización

- `config`: variables de conexión.
- `model`: registros inmutables.
- `dao`: SQL y JDBC.
- `service`: validaciones y operaciones de dominio.
- `ui`: vista de consola y navegación.
- `docs`: arquitectura y relación preliminar con contenidos del parcial.
- `database`: scripts de esquema, datos y conciliación.

## Verificación sin dependencias externas

Si dispone de JDK pero no de Maven, puede revisar la sintaxis de las clases Java (la conexión real requiere el controlador PostgreSQL obtenido por Maven):

```bash
javac -encoding UTF-8 -d /tmp/bibliotecasql-check $(find src/main/java -name '*.java')
```

Las clases usan solamente `java.sql` para compilar; el controlador JDBC se carga en ejecución.

## Precauciones

- Evite `docker compose down -v` si necesita conservar el volumen.
- No se ejecuta SQL proporcionado libremente por el usuario; la búsqueda usa parámetros.
- No confunda la fecha de devolución programada (`fecha_devolucion`) con la real (`fecha_devolucion_real`).
