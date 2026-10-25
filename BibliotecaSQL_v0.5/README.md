# BibliotecaSQL — versión 0.5

Aplicación didáctica de consola desarrollada en Java 17, PostgreSQL y JDBC. El objetivo es relacionar los contenidos del segundo parcial de Bases de Datos con un sistema bibliotecario tradicional.

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

Espere a que PostgreSQL esté listo antes de iniciar Java. La carga automática de datos ocurre únicamente cuando el volumen de Docker es nuevo. Para reiniciar desde cero, consulte primero los datos que desea conservar: `docker compose down -v` elimina el volumen de PostgreSQL.

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
