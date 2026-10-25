# BibliotecaSQL — Sistema de gestión bibliotecaria en consola

**Versión funcional de referencia: v9.0.5** · **Universidad Veracruzana** · Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán.

BibliotecaSQL es un proyecto didáctico de **Java 17 + PostgreSQL + JDBC**. Su interfaz de texto, construida con **Lanterna**, recupera la apariencia de los entornos clásicos de desarrollo en DOS: fondo azul, ventanas con marcos, menús numéricos y navegación por teclado. Incluye catálogos, préstamos, devoluciones, reportes y un laboratorio SQL relacionado con las actividades A7, A8 y A9 de Bases de Datos.

> **Para estudiantes:** utilicen la rama `main` o la versión **v9.0.5** para ejecutar la aplicación. Las etiquetas anteriores se conservan para estudiar la evolución del software y pueden contener errores ya corregidos. El historial Git fue **reconstruido retrospectivamente** a partir de entregas conservadas; las fechas de los hitos no pretenden ser las fechas originales de publicación en GitHub. Consulte [HISTORIAL.md](HISTORIAL.md).

## 1. Qué aprenderá

- Conectar una aplicación Java de consola a PostgreSQL mediante JDBC.
- Comprender las responsabilidades de interfaz, servicios, DAO y base de datos.
- Utilizar `PreparedStatement`, `ResultSet` y transacciones (`COMMIT`/`ROLLBACK`).
- Trabajar con relaciones, claves foráneas, búsquedas, agrupamiento, `JOIN` y subconsultas.
- Examinar el SQL ejecutado y el código Java desde el **modo didáctico**.
- Analizar cómo cambiaron la interfaz, el modelo de datos, las pruebas y las reglas de negocio entre versiones.

## 2. Requisitos antes de comenzar

Se necesita una computadora con **Windows 10/11, Linux o macOS**, conexión a Internet durante la instalación y una terminal.

| Herramienta | Requisito | ¿Para qué sirve? |
|---|---|---|
| **Java JDK 17** | Recomendado: JDK 17 | Compilar y ejecutar la aplicación |
| **Apache Maven** | Maven 3.x | Descargar dependencias y ejecutar Java y pruebas |
| **Docker + Docker Compose** | Docker Desktop en Windows/macOS o Docker Engine con Compose Plugin en Linux | Iniciar PostgreSQL sin instalarlo directamente |
| **Git** | Recomendado | Clonar el repositorio y consultar versiones |
| **VS Code** | Opcional | Leer y modificar el código Java y SQL |

Descargas oficiales: [JDK (Eclipse Temurin)](https://adoptium.net/temurin/releases/?version=17), [Maven](https://maven.apache.org/download.cgi), [Docker](https://docs.docker.com/get-docker/), [Git](https://git-scm.com/downloads) y [VS Code](https://code.visualstudio.com/). En VS Code resulta útil *Extension Pack for Java*.

**Compruebe los requisitos** en la terminal:

```bash
java -version
javac -version
mvn -version
docker --version
docker compose version
git --version
```

Debe poder ejecutar `java`, `javac`, `mvn` y `docker`. Si falta alguno, instálelo y **abra una terminal nueva**. En Windows asegúrese de que Docker Desktop esté **iniciado**; en Linux el servicio Docker debe estar funcionando y el usuario debe tener permisos para utilizarlo.

### Indicaciones por sistema operativo

**Windows:** use **PowerShell** o la terminal integrada de VS Code. Instale Docker Desktop, JDK y Maven. Compruebe que los comandos anteriores funcionen. Para ejecutar scripts `.sh` use **Git Bash** o WSL; no intente ejecutarlos directamente en PowerShell. El proyecto Java se ejecuta con los mismos comandos `docker compose` y `mvn` en PowerShell.

**macOS:** instale Docker Desktop y el JDK 17. Si utiliza Homebrew, puede instalar Maven con `brew install maven` y Git con `brew install git`. Compruebe `mvn -version` y que Docker Desktop esté abierto. No se requieren teclas `Fn` para usar BibliotecaSQL.

**Linux:** instale OpenJDK 17, Maven, Git y Docker Engine con el complemento Compose mediante el gestor de paquetes de su distribución. Asegúrese de poder ejecutar `docker compose version`. Según su configuración, puede ser necesario configurar permisos del grupo `docker`; consulte la documentación de Docker antes de utilizar `sudo`.

## 3. Obtener los archivos

**Opción A: clonar con Git (recomendada)**

```bash
git clone https://github.com/jgdiaz19/biblioteca-sql-java.git
cd biblioteca-sql-java
```

**Opción B: descargar ZIP desde GitHub:** abra el repositorio, seleccione **Code → Download ZIP**, extraiga el archivo y **abra una terminal dentro de la carpeta extraída**. También podrá descargar la versión oficial desde **Releases → v9.0.5**, cuando se haya publicado su Release.

**Importante:** ejecute todos los comandos siguientes desde la carpeta que contiene los archivos `pom.xml` y `docker-compose.yml`. No los ejecute dentro de `src` ni `database`.

## 4. Iniciar PostgreSQL con Docker

1. Abra **Docker Desktop** (Windows/macOS) o compruebe que Docker Engine funciona (Linux).
2. Abra una terminal en la carpeta del proyecto.
3. Ejecute:

```bash
docker compose up -d --wait
```

El primer inicio puede tardar más porque Docker debe descargar la imagen `postgres:17`. El proyecto crea un contenedor PostgreSQL y un volumen persistente de datos. El parámetro `-d` libera la terminal y `--wait` espera el estado de arranque/salud configurado en Compose.

Verifique el resultado:

```bash
docker compose ps
```

El servicio `postgres` debería aparecer iniciado y, una vez finalizada la comprobación, saludable (`healthy`). Consulte los últimos registros en caso de error:

```bash
docker compose logs --tail=80 postgres
```

**Las bases creadas en un volumen nuevo son:**

- `biblioteca_universitaria`: datos y esquema originales de prácticas A7–A9. No se utiliza para operaciones de escritura.
- `biblioteca_operativa`: base normalizada que utiliza la aplicación, con catálogos, libros, préstamos y reportes.

La base operativa contiene tablas `autores`, `categorias`, `editoriales`, `libros`, `usuarios`, `prestamos`, y la vista `libros_consulta`. En ella, `id_categoria` e `id_editorial` relacionan `libros` con los catálogos correspondientes.

### Comprobar que PostgreSQL contiene los datos

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT COUNT(*) FROM libros;"
docker compose exec postgres psql -U admin -d biblioteca_universitaria -c "SELECT COUNT(*) FROM libros;"
```

En una instalación nueva del conjunto de datos de ejemplo, la base académica incluye **24 libros**. La base operativa puede cambiar cuando usted agregue o elimine registros. Puede inspeccionar las relaciones:

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT id_libro, titulo, categoria, editorial FROM libros_consulta ORDER BY id_libro LIMIT 5;"
```

## 5. Ejecutar BibliotecaSQL

Con PostgreSQL iniciado y desde la **carpeta principal del repositorio**, ejecute:

```bash
mvn compile exec:java
```

En la primera ejecución, Maven descarga automáticamente las bibliotecas necesarias (controlador JDBC, Lanterna y otras dependencias). Necesita acceso a Internet para esta descarga inicial. Cuando se abra la pantalla de bienvenida, pulse la tecla indicada para entrar al menú principal.

**Recomendaciones de visualización:** amplíe la terminal a **80 columnas × 24 filas o más**, use fuente monoespaciada y codificación UTF-8. La terminal integrada de VS Code puede funcionar, pero si los marcos, teclas o colores no se ven bien, pruebe **Windows Terminal**, **Terminal de macOS** o la terminal habitual de su distribución Linux.

### Si la conexión a PostgreSQL falla

Verifique primero:

```bash
docker compose ps
docker compose logs --tail=80 postgres
```

Los valores de demostración configurados por defecto para **uso local** son:

| Parámetro | Valor de ejemplo |
|---|---|
| Servidor de PostgreSQL | `localhost` |
| Puerto | `5432` |
| Base para la aplicación | `biblioteca_operativa` |
| Usuario | `admin` |
| Contraseña de laboratorio | `biblioteca_local` |

La aplicación lee `DB_URL`, `DB_USER` y `DB_PASSWORD` del entorno cuando es necesario. Esos datos son **solo para el laboratorio local**: no reutilice la contraseña de ejemplo en un servidor público ni suba contraseñas personales al repositorio.

## 6. Navegación de la interfaz

BibliotecaSQL utiliza **teclas numéricas normales** para que funcione en laptops Windows, macOS y Linux, sin necesitar teclas de función.

| Tecla | Acción habitual |
|---|---|
| `1`–`9` | Elegir una opción disponible en el menú o tabla actual |
| `0` | Regresar |
| `↑` / `↓` | Moverse entre registros u opciones |
| `Enter` | Seleccionar un registro o abrir detalles |
| `/` | Activar la búsqueda en tablas que la admiten |
| `Backspace` | Borrar caracteres de la búsqueda |
| `Page Up` / `Page Down` | Cambiar de bloque cuando esté disponible |
| `Esc` | Cancelar o cerrar una ventana |
| `D` en el menú principal | Activar/desactivar el modo didáctico |

Las acciones disponibles **siempre aparecen al pie de cada pantalla**; consúltelas antes de pulsar una tecla. En catálogos y préstamos algunas pantallas muestran `4` para agregar, `5` para modificar y `6` para eliminar/devolver, según el módulo. Al borrar registros se solicita confirmación.

### Operaciones que puede practicar

1. **Libros:** consultar, buscar, agregar, modificar y eliminar cuando las relaciones lo permitan; seleccionar autores, categorías y editoriales existentes o registrar nuevos desde el formulario.
2. **Catálogos:** administrar autores, categorías y editoriales; observar qué ocurre al intentar eliminar un registro que tiene libros relacionados.
3. **Préstamos:** registrar préstamos de usuarios existentes o nuevos; definir plazo en días, registrar devoluciones y consultar activos y vencidos.
4. **Reportes:** estadísticas por categoría e historial de préstamos por usuario; `Enter` permite consultar detalles en los reportes correspondientes.
5. **Laboratorio SQL (opción 9):** explorar **36 consultas** organizadas en A7 (funciones y agrupamiento), A8 (`JOIN`) y A9 (subconsultas).

**Fechas:** la interfaz presenta las fechas en formato `DD-MM-AAAA`; PostgreSQL conserva el tipo `DATE`. No confunda *fecha del préstamo*, *fecha límite* y *devolución real*.

## 7. Modo didáctico: ver SQL y Java

Active el modo didáctico con la tecla **`D` desde el menú principal**, ejecute una consulta y abra el visor didáctico desde la tabla de resultados usando la opción que se indica en pantalla (habitualmente `3`). Las vistas del visor permiten estudiar:

1. **SQL ejecutado**, con resaltado de sintaxis.
2. **Código Java** relacionado con la ejecución, también coloreado.
3. **Explicación SQL**, que describe las operaciones presentes.
4. **Explicación Java**, orientada a la preparación, ejecución y procesamiento del resultado.

Algunas explicaciones se basan en patrones detectados automáticamente: **consulte también el código fuente y los resultados reales**, ya que el visor no sustituye una revisión completa de la lógica SQL. Para mostrar el archivo Java original, inicie la aplicación desde la raíz del proyecto.

**Secuencia sugerida:** abra un ejercicio A7, siga el recorrido desde la interfaz hacia el DAO; continúe con un `JOIN` de A8 y después con una subconsulta de A9. Observe cómo `ResultSet` se transforma en filas visibles.

## 8. Estructura del repositorio

```text
biblioteca-sql-java/
├── README.md                    # Esta guía
├── HISTORIAL.md                 # Cronología reconstruida
├── ISSUES_PARA_GITHUB.md        # Tareas históricas para Issues
├── PROJECTS_PLAN.md             # Organización del tablero
├── pom.xml                      # Dependencias y ejecución Maven
├── docker-compose.yml           # PostgreSQL de la aplicación
├── docker-compose.test.yml      # PostgreSQL aislado para pruebas
├── database/                    # Esquema, datos y normalización
├── src/main/java/               # Interfaz, servicios, DAO, modelos
├── src/test/java/               # Pruebas automatizadas
├── docs/                        # Guías técnicas y didácticas
└── scripts/                     # Auditoría, pruebas y migraciones
```

**Ruta de aprendizaje recomendada:** `src/main/java/edu/uv/biblioteca/Main.java` → clases de `ui` → `service` → `dao` → `config` y archivos de `database`. Consulte [docs/01-arquitectura.md](docs/01-arquitectura.md) y las guías de `docs/`.

## 9. Ejecutar las pruebas y la auditoría SQL

### Comprobar las clases y pruebas JUnit

```bash
mvn test
```

Algunas pruebas son de integración y requieren una base accesible y condiciones concretas. Para ejecutar la **batería aislada**, utilice **Git Bash o WSL (Windows)** o una terminal **macOS/Linux**:

```bash
bash scripts/probar-integracion.sh
```

Este script crea un **contenedor y volumen exclusivos de pruebas**, usa el puerto `55433` y los elimina al terminar. **No introduzca datos personales en ese entorno**, pues su volumen se borra al finalizar.

### Ejecutar la auditoría de los 36 ejercicios SQL

Con Docker activo, ejecute:

```bash
bash scripts/auditar-laboratorio.sh
```

Este script ejecuta las 36 consultas mediante Java en modo de solo lectura. **Una consulta que se ejecuta sin error no es necesariamente correcta desde el punto de vista lógico:** deben revisarse los resultados y los objetivos del ejercicio. Consulte [docs/22-auditoria-laboratorio-sql.md](docs/22-auditoria-laboratorio-sql.md).

## 10. Detener el proyecto y conservar los datos

Para finalizar Java utilice la opción **Salir** del menú. Después puede detener PostgreSQL:

```bash
docker compose down
```

Este comando **no elimina el volumen** de PostgreSQL. Si quiere reanudar el proyecto otro día:

```bash
docker compose up -d --wait
mvn compile exec:java
```

> **¡Atención!** `docker compose down -v` **elimina los volúmenes** asociados al proyecto y con ellos sus bases de datos locales. Úselo únicamente cuando desee borrar el entorno de laboratorio y ya haya respaldado cualquier dato importante.

Si cambia el nombre de la carpeta del proyecto, Docker Compose puede utilizar **otro nombre de proyecto y crear otro volumen**, aunque sus datos anteriores sigan existiendo. Verifique `docker volume ls` antes de concluir que se perdieron. Consulte el apartado de migración del documento [docs/18-normalizacion-v9.md](docs/18-normalizacion-v9.md) si proviene de versiones anteriores a v9.

## 11. Problemas frecuentes y soluciones

| Problema | Qué revisar |
|---|---|
| `mvn: command not found` o `mvn no se reconoce` | Instale Maven 3 y abra una terminal nueva; compruebe `mvn -version` |
| `java: command not found` | Instale JDK 17 y compruebe `java -version` y `javac -version` |
| `Cannot connect to the Docker daemon` | Inicie Docker Desktop o el servicio Docker Engine |
| El puerto `5432` ya está ocupado | Hay otro PostgreSQL o contenedor escuchando. Deténgalo o cambie el puerto publicado en Compose **y** `DB_URL` de Java |
| `Connection refused` desde Java | Compruebe `docker compose ps`, la salud del contenedor y `DB_URL` |
| Error de contraseña | Confirme `DB_USER`, `DB_PASSWORD` y las credenciales del contenedor. Cambiar solo `POSTGRES_PASSWORD` después del primer arranque **no cambia automáticamente** la contraseña almacenada en un volumen existente |
| No aparece el menú o se dibujan mal los bordes | Amplíe la ventana; use terminal monoespaciada UTF-8; pruebe la terminal nativa del sistema |
| Las flechas no responden | Pruebe Windows Terminal, Terminal de macOS o una terminal Linux; algunas consolas embebidas interpretan teclas de forma distinta |
| El volumen parece vacío después de actualizar | Compruebe el nombre del proyecto Compose y `docker volume ls`; **no ejecute `down -v`** |
| Un autor/categoría/editorial no puede eliminarse | Compruebe si existen libros relacionados; PostgreSQL protege la integridad referencial |
| Maven no descarga dependencias | Revise Internet, proxy, certificados y acceso a Maven Central |
| Un script `.sh` no se ejecuta en PowerShell | Ábralo con Git Bash o WSL y use `bash scripts/NOMBRE.sh` |

Para pedir ayuda, comparta **el comando ejecutado, el mensaje completo de error, el sistema operativo y la salida de `docker compose ps`**. No envíe contraseñas ni información personal.

## 12. Estudiar la evolución del proyecto

La reconstrucción contiene **21 versiones etiquetadas** desde `v0.1` hasta `v9.0.5`. Puede verlas en [Tags](https://github.com/jgdiaz19/biblioteca-sql-java/tags) y comparar versiones desde GitHub, por ejemplo:

- [v0.1 → v0.7](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.1...v0.7): arquitectura inicial e interfaz de texto.
- [v0.7 → v0.8.2](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.7...v0.8.2): visor SQL/Java didáctico.
- [v0.8.6 → v9.0](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v0.8.6...v9.0): normalización del modelo relacional.
- [v9.0 → v9.0.5](https://github.com/jgdiaz19/biblioteca-sql-java/compare/v9.0...v9.0.5): catálogos, préstamos, reportes y laboratorios.

Lea [HISTORIAL.md](HISTORIAL.md) antes de interpretar las fechas. El proceso de reconstrucción y las tareas están descritos en [ISSUES_PARA_GITHUB.md](ISSUES_PARA_GITHUB.md) y [PROJECTS_PLAN.md](PROJECTS_PLAN.md). Cuando el tablero de **GitHub Projects** esté publicado, se incorporará aquí su enlace directo.

## 13. Reglas para trabajar en clase

- Utilice **solo datos de prueba**, no información personal real de usuarios.
- No cambie registros en `biblioteca_universitaria`: se necesitan para reproducir las actividades académicas.
- Antes de modificar código, ejecute la versión de referencia y compruebe su funcionamiento.
- Documente los cambios con comentarios simples y explique qué modificó y por qué.
- Verifique resultados SQL, restricciones, manejo de errores y comportamiento visual.
- **No publique secretos** ni archivos de configuración con credenciales personales.

---

**Créditos:** *Designed and Developed by JEGD* · Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán · Universidad Veracruzana.

**Alcance:** software didáctico y de laboratorio; no está diseñado para operar una biblioteca real sin revisiones adicionales de seguridad, pruebas y privacidad.
