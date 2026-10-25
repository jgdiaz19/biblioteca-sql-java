# Guía de pruebas de BibliotecaSQL

## Propósito

Comprobar que las operaciones se ejecuten completas, que los errores no dejen cambios parciales y que la base académica conserve sus datos.

## Preparación

1. Instale Java 17, Maven y Docker Compose.
2. Desde la raíz del repositorio, ejecute `bash scripts/probar-integracion.sh` en Linux, macOS o Git Bash.
3. El script crea una instancia temporal de PostgreSQL en el puerto **55433**, ejecuta las pruebas y elimina sus propios contenedores y volúmenes.
4. Si hay un error, revise el reporte de Surefire en `target/surefire-reports/`.

**Importante:** el script elimina solamente el volumen creado para pruebas. No ejecuta `docker compose down -v` sobre el proyecto habitual. No conecte las pruebas a otras instancias: los casos de integración recrean las tablas de la base de pruebas.

## Comprobaciones automáticas

| Prueba | Resultado esperado |
|---|---|
| Alta de préstamo | El préstamo aparece abierto y el libro deja de estar disponible. |
| Devolución | Se guarda la fecha real y el libro vuelve a estar disponible. |
| Préstamo duplicado | Se rechaza y no aparece una segunda operación. |
| Usuario inactivo | Se rechaza y no cambia el inventario. |
| Límite de préstamos | El cuarto préstamo del mismo usuario se rechaza. |
| Fallo en actualización | Se revierte el préstamo mediante `ROLLBACK`. |
| Segunda devolución | Se rechaza y se conserva el estado anterior. |
| Base académica | La operación de escritura es rechazada. |
| Consola | La entrada se limpia y las tablas mantienen sus bordes. |

## Prueba manual de la consola

Con `docker compose up -d --wait` y `mvn compile exec:java`, compruebe las opciones 1 a 9 y D. Revise títulos largos, tablas vacías, entradas inválidas y navegación. Una secuencia de captura automatizada no sustituye la revisión visual de la terminal real.

## Interpretación

Un resultado `BUILD SUCCESS` con pruebas ejecutadas indica que los casos probados pasaron. Si no define `DB_TEST_URL`, JUnit omite las pruebas de integración: una compilación correcta no demuestra el funcionamiento con PostgreSQL. Consulte el número de pruebas ejecutadas y omitidas.
