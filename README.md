# BibliotecaSQL

**Sistema didáctico de gestión bibliotecaria en consola**  
**Versión de referencia: v9.0.5** · Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán · Universidad Veracruzana

BibliotecaSQL es una aplicación Java que permite administrar libros, autores, categorías, editoriales y préstamos mediante una interfaz de texto. Utiliza PostgreSQL para el almacenamiento, JDBC para el acceso a los datos y Lanterna para presentar menús, tablas y ventanas en terminales de Windows, Linux y macOS.

El proyecto acompaña la experiencia educativa de **Bases de Datos**. Reúne ejercicios de funciones SQL (A7), consultas con `JOIN` (A8) y subconsultas (A9), además de ejemplos de validación, normalización y transacciones. El código está disponible para su lectura y modificación con fines académicos.

> **Versión que debe utilizarse en clase:** trabaje con la rama `main` o con la etiqueta `v9.0.5`. Las etiquetas anteriores se conservan para analizar el desarrollo histórico, pero pueden contener errores o funciones incompletas. El historial se reconstruyó retrospectivamente a partir de entregas conservadas; las fechas de sus hitos no deben confundirse con fechas originales de publicación en GitHub. Consulte [HISTORIAL.md](HISTORIAL.md).

## 1. Requisitos

La aplicación se ejecuta en una computadora con **Windows 10/11, macOS o Linux**. Se requiere conexión a Internet para descargar herramientas e instalar dependencias por primera vez.

| Herramienta | Versión o condición | Función |
|---|---|---|
| Java Development Kit (JDK) | **17** | Compilar y ejecutar Java |
| Apache Maven | **3.x** | Gestionar dependencias, compilar y ejecutar pruebas |
| Docker y Docker Compose | Instalación operativa | Ejecutar PostgreSQL en un contenedor |
| Git | Recomendado | Descargar el código y estudiar su historial |
| VS Code | Opcional | Examinar y modificar archivos Java, SQL y Markdown |

Fuentes de instalación: [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17), [Maven](https://maven.apache.org/download.cgi), [Docker](https://docs.docker.com/get-docker/), [Git](https://git-scm.com/downloads) y [VS Code](https://code.visualstudio.com/). En VS Code puede instalar **Extension Pack for Java**.

Compruebe que los programas estén disponibles desde la terminal:

```bash
java -version
javac -version
mvn -version
docker --version
docker compose version
git --version
```

### Consideraciones por sistema operativo

**Windows.** Utilice PowerShell, Windows Terminal o la terminal de VS Code. Inicie Docker Desktop antes de ejecutar Compose. Para los scripts con extensión `.sh` utilice Git Bash o WSL; los comandos `docker compose` y `mvn` pueden ejecutarse directamente desde PowerShell.

**macOS.** Inicie Docker Desktop. Si utiliza Homebrew, puede instalar Maven mediante `brew install maven`. Verifique que `java -version` corresponda a un JDK compatible, preferentemente JDK 17.

**Linux.** Instale OpenJDK 17, Maven, Git, Docker Engine y Compose Plugin según su distribución. Compruebe que el servicio Docker esté iniciado y que su cuenta disponga de los permisos necesarios para utilizarlo.

Si aparece `command not found` o «no se reconoce como un comando», instale la herramienta correspondiente, abra otra terminal y repita la comprobación.

## 2. Obtener el proyecto

Se recomienda clonar el repositorio:

```bash
git clone https://github.com/jgdiaz19/biblioteca-sql-java.git
cd biblioteca-sql-java
```

Como alternativa, utilice **Code → Download ZIP** en GitHub y extraiga su contenido. En ambos casos, **abra la terminal dentro de la carpeta que contiene `pom.xml` y `docker-compose.yml`**.

Si desea estudiar una versión histórica, consulte las [etiquetas del repositorio](https://github.com/jgdiaz19/biblioteca-sql-java/tags). Para ejecutar las prácticas habituales se recomienda mantener `main`.

## 3. Preparar PostgreSQL

Asegúrese de que Docker esté iniciado y ejecute desde la carpeta principal:

```bash
docker compose up -d --wait
docker compose ps
```

`up` crea o inicia los servicios definidos en `docker-compose.yml`; `-d` los deja en segundo plano y `--wait` espera su estado de ejecución o salud según la configuración. El servicio `postgres` dispone de una comprobación `healthcheck` con `pg_isready`.

En un volumen nuevo se crean dos bases:

| Base de datos | Uso |
|---|---|
| `biblioteca_universitaria` | Conserva el esquema y los datos originales de las prácticas. No se utiliza para escritura desde la aplicación. |
| `biblioteca_operativa` | Permite gestionar catálogos, préstamos, devoluciones y reportes; incluye categorías y editoriales normalizadas. |

Compruebe la carga inicial:

```bash
docker compose exec postgres psql -U admin -d biblioteca_operativa -c "SELECT COUNT(*) FROM libros;"
docker compose exec postgres psql -U admin -d biblioteca_universitaria -c "SELECT COUNT(*) FROM libros;"
```

El conjunto de ejemplo contiene inicialmente **24 libros**. La cantidad en `biblioteca_operativa` puede cambiar después de las operaciones del usuario.

Para consultar los mensajes del contenedor:

```bash
docker compose logs --tail=80 postgres
```

**Importante:** los scripts de inicialización automática se ejecutan al crear un volumen nuevo. Un reinicio posterior no vuelve a insertar todos los datos.

## 4. Iniciar BibliotecaSQL

Con PostgreSQL activo, ejecute:

```bash
mvn compile exec:java
```

Maven obtendrá las dependencias necesarias en la primera ejecución. Cuando aparezca la pantalla de presentación, presione una tecla para continuar.

La interfaz se visualiza mejor en una terminal con **al menos 80 columnas y 24 filas**, fuente monoespaciada y codificación UTF-8. Si las flechas, los marcos o los colores no funcionan como se espera, utilice Windows Terminal, Terminal de macOS o una terminal Linux.

### Configuración de la conexión

De manera predeterminada, para una instalación local de laboratorio, se utilizan los siguientes valores:

| Parámetro | Valor |
|---|---|
| Servidor | `localhost` |
| Puerto | `5432` |
| Base de datos | `biblioteca_operativa` |
| Usuario | `admin` |
| Contraseña de ejemplo | `biblioteca_local` |

La aplicación puede leer las variables de entorno **`DB_URL`**, **`DB_USER`** y **`DB_PASSWORD`**. Estos valores son exclusivamente para prácticas locales: no utilice la contraseña de ejemplo en servidores públicos ni publique credenciales personales.

## 5. Funciones de la aplicación

El menú principal reúne las siguientes operaciones:

| Opción | Función |
|---|---|
| 1 | Consultar, buscar y administrar libros |
| 2 | Administrar autores, categorías y editoriales |
| 3 | Consultar y gestionar préstamos activos |
| 4 | Consultar estadísticas por categoría |
| 5 | Registrar un préstamo |
| 6 | Registrar una devolución |
| 7 | Consultar préstamos vencidos |
| 8 | Consultar el historial de préstamos por usuario |
| 9 | Acceder al laboratorio SQL (A7, A8 y A9) |
| D | Activar o desactivar el modo didáctico |
| 0 | Salir |

La navegación utiliza números, flechas, `ENTER` y `ESC`, sin depender de teclas de función. Las acciones disponibles aparecen al pie de cada pantalla. En los catálogos, `4`, `5` y `6` corresponden generalmente a **agregar**, **modificar** y **eliminar**, respectivamente.

Las ventanas de registro permiten elegir autores, categorías y editoriales existentes o incorporar nuevos valores. Los identificadores se administran internamente: el usuario selecciona los registros por sus nombres. Las fechas se muestran como **DD-MM-AAAA**.

### Búsqueda de libros

La opción **2. Cambiar filtro** del catálogo alterna entre dos estrategias:

- **Java, en memoria:** compara el texto de la búsqueda con una lista de libros previamente recuperada.
- **PostgreSQL, mediante `ILIKE`:** vuelve a consultar la base con `PreparedStatement`.

Al cambiar de estrategia se limpia el término anterior. Esta comparación permite observar en qué capa se ejecuta el filtrado.

### Modo didáctico

Active el modo con **D** desde el menú principal. En las pantallas que ofrecen **3. Ver SQL**, el visor permite consultar:

1. La sentencia SQL ejecutada.
2. El fragmento del código Java que la prepara.
3. Una explicación de sus elementos SQL.
4. Una explicación de las operaciones Java relacionadas.

Los fragmentos se muestran con resaltado de sintaxis. Para localizarlos correctamente, **ejecute Maven desde la raíz del repositorio**, donde se encuentra `src/main/java`. El visor es un apoyo para el estudio; sus explicaciones no sustituyen la lectura del código ni la comprobación de resultados.

## 6. Laboratorio del segundo parcial

La opción **9** contiene **36 consultas**, organizadas de la siguiente forma:

| Actividad | Contenidos | Ejercicios |
|---|---|---|
| A7 | Funciones escalares, agregación y agrupamiento | 12 |
| A8 | Consultas con `JOIN` y relaciones entre tablas | 12 |
| A9 | Subconsultas, consultas correlacionadas y operadores de conjunto | 12 |

Los ejemplos corresponden a los **temas** de las actividades; no se afirma que reproduzcan literalmente todos los ejercicios numerados de los cuadernos originales.

La referencia de estudio está en [docs/04-laboratorio.md](docs/04-laboratorio.md). Para ejecutar una comprobación técnica de las 36 consultas, utilice:

```bash
bash scripts/auditar-laboratorio.sh
```

En Windows, este script debe ejecutarse desde **Git Bash o WSL**. Una consulta que se ejecuta sin errores todavía puede requerir revisión de su resultado lógico.

## 7. Pruebas y conservación de datos

Para ejecutar las pruebas automatizadas:

```bash
mvn test
```

Algunas pruebas de integración se omiten cuando no se ha configurado `DB_TEST_URL`. Revise el resumen de Maven antes de concluir que PostgreSQL se ha probado satisfactoriamente.

Para utilizar una instancia aislada de pruebas:

```bash
bash scripts/probar-integracion.sh
```

Este script utiliza el puerto **55433** y un volumen temporal propio, que elimina al finalizar. No debe apuntarse a la base de trabajo que desee conservar.

Para detener PostgreSQL **sin borrar sus datos**:

```bash
docker compose down
```

Para volver a trabajar, ejecute `docker compose up -d --wait` y después `mvn compile exec:java`.

> **Precaución:** `docker compose down -v` elimina los volúmenes vinculados al proyecto y puede ocasionar la pérdida de los datos locales. No lo utilice sin respaldo. Cambiar el nombre de la carpeta del proyecto también puede cambiar el nombre del volumen de Compose.

## 8. Problemas frecuentes

| Situación | Comprobación recomendada |
|---|---|
| `mvn` no se encuentra | Instale Maven; verifique `mvn -version` y abra otra terminal. |
| `java` o `javac` no se encuentran | Instale JDK 17 y compruebe las variables de entorno. |
| Docker no responde | Inicie Docker Desktop o el servicio Docker Engine. |
| El puerto `5432` está ocupado | Compruebe si existe otro PostgreSQL; ajuste el puerto y `DB_URL` cuando proceda. |
| `Connection refused` | Revise `docker compose ps`, los registros del servicio y la URL JDBC. |
| Fallo de contraseña | Compare las credenciales; cambiar `POSTGRES_PASSWORD` en un volumen ya inicializado no actualiza automáticamente la cuenta. |
| La terminal presenta caracteres incorrectos | Utilice UTF-8, una fuente monoespaciada y una terminal compatible. |
| No aparece el código Java en el visor | Ejecute el proyecto desde la carpeta principal. |
| El contenido parece haber desaparecido tras cambiar de carpeta | Compruebe `docker volume ls`; no elimine volúmenes antes de identificar el anterior. |

Para solicitar asistencia, indique el sistema operativo, el comando utilizado y el mensaje completo del error. No comparta contraseñas.

## 9. Organización y documentación

```text
biblioteca-sql-java/
├── README.md                 Guía de instalación y uso
├── HISTORIAL.md              Evolución retrospectiva del sistema
├── CONTRIBUIR.md             Procedimiento para proponer cambios
├── pom.xml                   Configuración de Maven
├── docker-compose.yml        PostgreSQL para la aplicación
├── docker-compose.test.yml   PostgreSQL para pruebas
├── database/                 Esquema, datos y normalización
├── docs/                     Guías académicas y técnicas
├── scripts/                  Auditoría, migración y pruebas
└── src/
    ├── main/java/            Código de la aplicación
    └── test/java/            Pruebas automatizadas
```

**Documentación vigente:** [índice de guías](docs/README.md), [arquitectura](docs/01-arquitectura.md), [modelo de datos](docs/02-modelo-de-datos.md), [reglas de negocio](docs/03-operaciones-y-reglas.md), [laboratorio SQL](docs/04-laboratorio.md), [interfaz y modo didáctico](docs/05-interfaz-y-modo-didactico.md), [pruebas](docs/06-pruebas-y-mantenimiento.md) y [referencia técnica del catálogo de libros](docs/07-catalogo-libros-java.md).

Para estudiar la evolución del sistema, consulte [HISTORIAL.md](HISTORIAL.md) y las [etiquetas de GitHub](https://github.com/jgdiaz19/biblioteca-sql-java/tags). Las tareas históricas están recopiladas en [ISSUES_PARA_GITHUB.md](ISSUES_PARA_GITHUB.md); la propuesta de seguimiento figura en [PROJECTS_PLAN.md](PROJECTS_PLAN.md).

## 10. Uso académico y créditos

Utilice únicamente datos ficticios. No modifique la base `biblioteca_universitaria` durante las actividades del curso ni publique credenciales.

**Designed and Developed by JEGD**  
Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán  
Universidad Veracruzana

Este software fue desarrollado con fines de enseñanza y experimentación; requiere medidas adicionales de seguridad, privacidad, autorización y pruebas antes de considerarse apto para gestionar una biblioteca real.
