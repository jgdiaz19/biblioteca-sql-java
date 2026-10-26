# Pruebas y mantenimiento del proyecto

Comprobar que BibliotecaSQL compila es distinto de verificar que sus operaciones son correctas. Esta guía reúne las pruebas disponibles y las precauciones necesarias para trabajar sin perder los datos del laboratorio.

## 1. Comprobación inicial

Desde la carpeta del proyecto, con Docker iniciado:

```bash
docker compose up -d --wait
docker compose ps
mvn compile exec:java
```

Si PostgreSQL no inicia correctamente, revise los registros:

```bash
docker compose logs --tail=80 postgres
```

La inicialización automática crea tablas y datos cuando el volumen es nuevo. Reiniciar el contenedor sobre un volumen existente no restablece los registros.

## 2. Pruebas automatizadas

Para ejecutar las pruebas de Maven:

```bash
mvn test
```

Las pruebas de integración requieren la variable `DB_TEST_URL` con una dirección específica. **Cuando no está configurada, pueden omitirse pruebas**; revise las cantidades ejecutadas y omitidas en `target/surefire-reports/`.

Para probar transacciones y reglas de negocio sobre una instancia independiente:

```bash
bash scripts/probar-integracion.sh
```

El script utiliza `docker-compose.test.yml`, el puerto **55433** y un volumen temporal. Al finalizar elimina los recursos de ese entorno de pruebas, no los de la aplicación habitual. En Windows debe ejecutarse desde Git Bash o WSL.

Entre los casos contemplados se encuentran el préstamo duplicado, el usuario inactivo, el límite de préstamos, la devolución repetida y la reversión mediante `rollback()`.

## 3. Auditoría de las consultas SQL

```bash
bash scripts/auditar-laboratorio.sh
```

Esta auditoría recorre las 36 consultas del laboratorio con una conexión de solo lectura. Permite detectar errores de ejecución, pero no sustituye la revisión de los resultados esperados, especialmente al trabajar con `JOIN`, subconsultas, valores nulos y fechas.

## 4. Comprobaciones manuales

| Caso | Comportamiento que debe observarse |
|---|---|
| Alta de libro con autor y catálogos nuevos | Los registros relacionados quedan correctamente asociados |
| Intento de registrar un ISBN duplicado | Se rechaza el alta sin dejar registros parciales |
| Eliminación de autor o categoría con libros | PostgreSQL impide la operación |
| Préstamo de siete días | Las fechas de inicio y límite son coherentes |
| Nuevo préstamo sobre un libro ya prestado | El sistema lo rechaza |
| Devolución de un ejemplar | Se registra la fecha real y cambia la disponibilidad |
| Historial por usuario | Coinciden el total, los activos y los devueltos |
| Modo didáctico | Se visualizan consultas SQL y fragmentos Java |
| Pantalla de 80 × 24 | Las tablas y ventanas permiten navegar sin perder información esencial |

Utilice registros ficticios. Cuando documente un error, conserve el comando utilizado y el mensaje completo, pero no incluya contraseñas.

## 5. Volúmenes y respaldos

Para detener PostgreSQL sin borrar los datos:

```bash
docker compose down
```

Si necesita conservar una copia lógica de la base operativa:

```bash
docker compose up -d --wait
docker compose exec -T postgres pg_dump -U admin -d biblioteca_operativa > respaldo_biblioteca_operativa.sql
```

**No utilice `docker compose down -v` sobre un entorno cuyos datos necesite conservar.** La opción `-v` elimina los volúmenes del proyecto. Si cambia el nombre de la carpeta, compruebe `docker volume ls` antes de asumir que se perdieron los registros.

## 6. Diagnóstico básico

| Problema | Revisión |
|---|---|
| Maven no aparece en la terminal | Compruebe `mvn -version` y la instalación de Maven |
| No se conecta a Docker | Inicie Docker Desktop o Docker Engine |
| Puerto `5432` ocupado | Localice la instancia que utiliza ese puerto y, si procede, ajuste `DB_URL` |
| PostgreSQL rechaza las credenciales | Verifique la configuración; cambiar la variable de contraseña no modifica automáticamente un volumen inicializado |
| La terminal no dibuja bien la interfaz | Ajuste el tamaño, el tipo de letra y la terminal |
| El visor no encuentra el archivo Java | Ejecute Maven desde la raíz del proyecto |
| La consulta devuelve resultados inesperados | Compruebe los datos, los filtros, las relaciones y las fechas |

La versión v9.0.5 se utiliza como **referencia didáctica**. No incorpora todos los controles de seguridad, privacidad, autorización y auditoría necesarios para operar un sistema bibliotecario real.
