# BibliotecaSQL — v0.2

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
5. Activar o desactivar la vista didáctica del SQL y parámetros.

**Esta versión solo consulta información.** Préstamos, devoluciones y CRUD se implementarán después, con reglas de negocio y transacciones comprobadas.

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
