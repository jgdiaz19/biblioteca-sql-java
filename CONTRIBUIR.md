# Guía para contribuir a BibliotecaSQL

Este documento describe el procedimiento recomendado para proponer correcciones o mejoras en BibliotecaSQL. Las contribuciones se realizan mediante una **rama de trabajo y un Pull Request**, de manera que puedan revisarse antes de incorporarlas a `main`.

## 1. Preparación del entorno

Siga las instrucciones de [README.md](README.md) para instalar Java 17, Maven, Docker y Git. Antes de modificar el código, compruebe que la aplicación inicia y que comprende la función que desea cambiar.

Si dispone de permisos de escritura en el repositorio, puede clonarlo directamente. De lo contrario, cree primero un **fork** desde GitHub y clone su propia copia; podrá enviar un Pull Request desde esa rama sin modificar directamente el repositorio original.

```bash
# Sustituya SU_USUARIO por la cuenta que contiene su fork.
git clone https://github.com/SU_USUARIO/biblioteca-sql-java.git
cd biblioteca-sql-java
git switch -c mejora/descripcion-breve
```

Si cuenta con permisos de colaboración, puede utilizar la URL del repositorio original en lugar de la del fork. Cuando ya disponga de una copia local, actualice su rama principal antes de iniciar el trabajo.

## 2. Alcance de los cambios

- **Código Java:** respete las responsabilidades de `ui`, `service`, `dao` y `model`.
- **SQL:** compruebe nombres de tablas, restricciones y efectos sobre ambas bases de datos.
- **Interfaz:** mantenga la navegación con teclas numéricas y una presentación legible en terminales de Windows, macOS y Linux.
- **Documentación:** actualice la guía temática correspondiente en `docs/`; evite crear un archivo nuevo por cada ajuste menor.
- **Comentarios:** explique decisiones y validaciones relevantes con frases breves y precisas. Evite repetir literalmente lo que ya expresa el código.

El conjunto académico `biblioteca_universitaria` debe permanecer intacto durante las pruebas del sistema operativo. No incluya credenciales personales, datos reales de estudiantes ni información sensible.

## 3. Comprobaciones previas

En la carpeta principal ejecute, según el alcance de la modificación:

```bash
mvn test
bash scripts/probar-integracion.sh
bash scripts/auditar-laboratorio.sh
git diff --check
```

El script de integración usa un PostgreSQL temporal y elimina sus datos al terminar. El script de auditoría requiere conexión a PostgreSQL. Un resultado satisfactorio de compilación no equivale a una validación completa de reglas de negocio: documente qué pruebas se realizaron y cuáles quedaron pendientes.

## 4. Registrar y proponer los cambios

```bash
git status
git add .
git commit -m "docs: aclarar el funcionamiento del catálogo"
git push -u origin mejora/descripcion-breve
```

En GitHub abra **Pull requests → New pull request**. Si trabaja desde un fork, seleccione su rama como origen y `jgdiaz19/biblioteca-sql-java:main` como destino. Si es colaborador, compare su rama de trabajo con `main`. Indique en la descripción:

1. Qué problema o necesidad motivó el cambio.
2. Qué archivos o funcionalidades fueron modificados.
3. Cómo puede comprobarse el resultado.
4. Qué pruebas se ejecutaron y si existe alguna limitación conocida.

No utilice `git push --force` para actualizar la rama principal. La fusión del Pull Request debe realizarse una vez revisado el contenido.

## 5. Historial de versiones

Las 21 etiquetas existentes representan entregas conservadas y se mantienen para estudiar la evolución de BibliotecaSQL. Una corrección de documentación en `main` **no modifica la etiqueta `v9.0.5`**. No cambie ni reutilice etiquetas históricas para registrar mejoras nuevas.

La cronología y su carácter retrospectivo se explican en [HISTORIAL.md](HISTORIAL.md).

## 6. Orientación para estudiantes

La revisión de código debe concentrarse tanto en el resultado como en la explicación técnica: qué consulta se ejecuta, qué datos recibe, qué validaciones se aplican y cómo se preserva la integridad de PostgreSQL.

La [guía del catálogo de libros](docs/07-catalogo-libros-java.md) ofrece un ejemplo de lectura del código Java antes de proponer modificaciones.
