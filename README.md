# BibliotecaSQL — Proyecto didáctico Java y PostgreSQL

**Versión 0.1 (base funcional inicial).** Caso de estudio de la EE Bases de Datos (Ingeniería de Software).

Fuente de datos: https://gist.github.com/jgdiaz19/af0426289725f559646fec9654253a5f

## Requisitos
- Docker y Docker Compose
- Java 17 o superior
- Maven 3.9 o superior

## Ejecución

```bash
docker compose up -d
mvn compile exec:java
```

La aplicación se conecta por defecto a `jdbc:postgresql://localhost:5432/biblioteca_operativa` con el usuario `admin` y la contraseña **solo local de ejemplo** `biblioteca_local`. Cambie esas credenciales si el entorno deja de ser local. Para personalizar, configure `DB_URL`, `DB_USER` y `DB_PASSWORD` en el entorno antes de ejecutar Maven.

## Bases de datos
- `biblioteca_universitaria`: mantiene el dataset académico oficial, sin modificaciones.
- `biblioteca_operativa`: mismo dataset inicial, con disponibilidad conciliada a partir de los préstamos que no tienen devolución real.

**Advertencia:** la inicialización Docker solo se ejecuta al crear el volumen. El archivo `04-conciliar.sql` se utiliza exclusivamente sobre la base operativa. No reinicialice volúmenes con datos que desee conservar.

## Alcance v0.1
- Conexión JDBC real, menú de consola, catálogo, búsqueda por título con `PreparedStatement`, listado con `JOIN` y estadística con `GROUP BY`.
- No incluye todavía operaciones de alta, modificación, préstamos ni devoluciones; llegarán en las versiones siguientes.
- El menú usa secuencias ANSI para limpiar pantalla. Se recomienda usar la terminal integrada de VS Code, Windows Terminal, macOS Terminal o una terminal Linux compatible con ANSI.

## Nota sobre datos históricos
El Gist fija datos de septiembre de 2026; las fechas de vencimiento son parte del caso académico, no una simulación actualizada al día de ejecución. El esquema no tiene tabla `ejemplares` ni `editoriales`. Cada libro tiene un booleano `disponible`.

## Verificación
En la base académica deben existir 12 autores, 24 libros, 10 usuarios y 18 préstamos. La misma cantidad se conserva en la base operativa; lo que cambia inicialmente es el indicador de disponibilidad.

## Trabajo posterior
Separar el código en UI, servicio y DAO; implementar transacciones de préstamo/devolución; modo didáctico SQL; trazabilidad A7/A8/A9 y pruebas automatizadas.
