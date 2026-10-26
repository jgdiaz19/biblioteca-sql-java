# Pruebas, auditoría y mantenimiento

**Versión:** BibliotecaSQL v9.0.5. Compruebe por separado compilación, pruebas automatizadas, consultas SQL y comportamiento visual.

## Arranque y verificación

Desde la raíz del proyecto, con Docker iniciado:

```bash
docker compose up -d --wait
docker compose ps
mvn compile exec:java
```

Para investigar fallos de PostgreSQL:

```bash
docker compose logs --tail=80 postgres
```

En el primer arranque se crea el esquema y se cargan datos de ejemplo. Los arranques posteriores conservan el volumen.

## Pruebas JUnit

```bash
mvn test
```

Algunas pruebas de integración están condicionadas por `DB_TEST_URL`. Si esa variable no se configura, **pueden omitirse**. Un `BUILD SUCCESS` no garantiza que se hayan ejecutado todas las pruebas de PostgreSQL. Revise el número de pruebas ejecutadas y omitidas en `target/surefire-reports/`.

Para ejecutar la batería de integración con una instancia independiente:

```bash
bash scripts/probar-integracion.sh
```

Use macOS/Linux o Git Bash/WSL en Windows para ejecutar archivos `.sh`. El script inicia PostgreSQL de pruebas en el puerto **55433**, establece `DB_TEST_URL`, ejecuta JUnit y elimina únicamente sus contenedores y volumen temporales. **No conecte estas pruebas a un volumen con datos que quiera conservar.**

Las pruebas contemplan: alta de préstamo, devolución, rechazo de usuario inactivo, límite de tres préstamos, duplicados, reversión ante error y bloqueo de escritura en la base académica.

## Auditoría SQL

```bash
bash scripts/auditar-laboratorio.sh
```

`LaboratoryAudit` comprueba la ejecución de las 36 consultas en `biblioteca_operativa` mediante conexión de solo lectura. Evalúe también si las salidas tienen **sentido lógico**, especialmente `NULL`, cardinalidades, fechas y `JOIN`. Consulte [04. Laboratorio SQL](04-laboratorio.md).

## Pruebas manuales recomendadas

| Caso | Resultado esperado |
|---|---|
| Catálogos con datos y vacíos | Marcos, encabezados, búsqueda y selección visibles |
| Alta de libro con autor/categoría/editorial nuevos | Relaciones correctas sin registros duplicados |
| ISBN duplicado | Error sin altas parciales |
| Eliminar autor, categoría o editorial con libros asociados | Rechazo por integridad referencial |
| Préstamo con plazo de 7 días | Fecha inicial y fecha límite correctas |
| Préstamo repetido del mismo ejemplar | Rechazo sin doble préstamo |
| Devolución | Fecha real guardada y libro disponible |
| Historial por usuario | Totales, activos y devueltos coherentes |
| Visor SQL/Java | Sintaxis legible y desplazamiento funcionando |
| Terminal 80 × 24 | Tablas y ventanas dentro del marco |

No introduzca datos personales reales para estas prácticas.

## Conservar datos y respaldar

```bash
# Detenemos los servicios sin borrar sus volúmenes.
docker compose down

# Examinamos los volúmenes existentes.
docker volume ls

# Realizamos un respaldo lógico, con PostgreSQL iniciado.
docker compose up -d --wait
docker compose exec -T postgres pg_dump -U admin -d biblioteca_operativa > respaldo_biblioteca_operativa.sql
```

**No ejecute `docker compose down -v` si necesita conservar los datos.** Ese parámetro elimina los volúmenes del proyecto. Cambiar el nombre de la carpeta puede cambiar también el nombre del proyecto Compose y crear un volumen distinto.

En instalaciones nuevas de v9.0.5 no se requiere migración manual. Si procede de una versión anterior, consulte [02. Modelo de datos](02-modelo-de-datos.md) y realice primero un respaldo.

## Solución de problemas

| Problema | Qué revisar |
|---|---|
| `mvn` no encontrado | Instalar Maven, abrir otra terminal y comprobar `mvn -version` |
| Docker no responde | Iniciar Docker Desktop o Docker Engine |
| Puerto 5432 ocupado | Localizar otro PostgreSQL; ajustar puerto y `DB_URL` de ser necesario |
| `Connection refused` | Estado de `docker compose ps`, logs y URL JDBC |
| Contraseña incorrecta con volumen existente | Cambiar la variable de Compose no actualiza por sí solo la contraseña guardada |
| Interfaz con marcos cortados | Ampliar terminal y utilizar fuente monoespaciada UTF-8 |
| El visor Java no encuentra el archivo | Ejecutar Maven desde la raíz que contiene `src/main/java` |
| Auditoría SQL termina bien pero el resultado no coincide | Revisar datos, filtros, `NULL` y fechas |

## Alcance

Esta documentación corresponde a un **software didáctico**. Para utilizarlo con datos reales sería necesario reforzar autenticación, autorización, auditoría, privacidad, configuración y pruebas concurrentes.
