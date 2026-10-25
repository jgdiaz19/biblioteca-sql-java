# Publicar el historial reconstruido en GitHub

Repositorio verificado: **https://github.com/jgdiaz19/biblioteca-sql-java** (cuenta `jgdiaz19`). La preparación local incluye **23 commits en `main` y 21 etiquetas**; la última etiqueta de software es `v9.0.5`. El commit adicional final amplía el README para estudiantes y **no altera** la instantánea del software en `v9.0.5`.

## Procedimiento (Windows, macOS o Linux)

1. Descargue el archivo `BibliotecaSQL_GitHub_Historial_y_Release_ACTUALIZADO.zip` y descomprímalo.
2. Localice dentro del paquete `BibliotecaSQL_historial.bundle`.
3. Abra **Git Bash en Windows**, **Terminal en macOS** o una terminal en **Linux** y sitúese en la carpeta donde está el `.bundle`.
4. Ejecute lo siguiente, una línea a la vez:

```bash
git clone BibliotecaSQL_historial.bundle biblioteca-sql-java
cd biblioteca-sql-java
git remote remove origin
git remote add origin https://github.com/jgdiaz19/biblioteca-sql-java.git
git log --oneline -5
git tag --list
git push -u origin main
git push origin --tags
```

Si GitHub solicita autenticación, utilice el inicio de sesión por navegador de Git Credential Manager o un token personal autorizado; GitHub **no acepta contraseñas de cuenta** para subir contenido por HTTPS. Alternativamente utilice SSH si ya lo tiene configurado.

**Importante:** no cree otro README ni inicialice el repositorio remoto antes de subirlo. Si el repositorio remoto ya tiene contenido, deténgase y compare las ramas; **no utilice `--force` ni `--force-with-lease` por defecto**. El proceso supone que el repositorio creado está vacío.

## Verificar el resultado

```bash
git ls-remote --heads origin main
git ls-remote --tags origin | grep 'refs/tags/v9.0.5'
git status
```

Abra en el navegador:
- [Repositorio](https://github.com/jgdiaz19/biblioteca-sql-java)
- [README](https://github.com/jgdiaz19/biblioteca-sql-java/blob/main/README.md)
- [Historial](https://github.com/jgdiaz19/biblioteca-sql-java/blob/main/HISTORIAL.md)
- [Etiquetas](https://github.com/jgdiaz19/biblioteca-sql-java/tags)

## Preparar la descarga v9.0.5

En GitHub, abra **Releases → Draft a new release**, seleccione la etiqueta **`v9.0.5`** ya publicada, coloque como título **BibliotecaSQL v9.0.5** y describa sus funciones principales. GitHub genera automáticamente enlaces para descargar los archivos fuente de esa etiqueta en ZIP y TAR.GZ. También puede adjuntar el ZIP `BibliotecaSQL_v9.0.5.zip` proporcionado en este paquete.

No cree una etiqueta `v9.0.5` nueva apuntando a `main`: **la etiqueta original reconstruida apunta al commit exacto del software 9.0.5**, mientras que `main` incorpora documentación posterior.

## Tareas y tablero

1. Abra **Issues** del repositorio; use `ISSUES_PARA_GITHUB.md` como base para crear las tareas históricas. Señale en su descripción que se trata de tareas **retrospectivas**.
2. Abra el apartado **Projects** del perfil `jgdiaz19`, cree un tablero (por ejemplo `BibliotecaSQL — Evolución del proyecto`) y agréguelo al repositorio cuando esté disponible la opción de vinculación.
3. Configure los estados **Pendiente**, **En desarrollo**, **En revisión** y **Completado** siguiendo `PROJECTS_PLAN.md`.
4. Al publicar el tablero, inserte su URL en el README para que los estudiantes naveguen hasta él.

La cronología del archivo `HISTORIAL.md` señala fechas de hitos, **no fechas originales de commits en GitHub**. La reconstrucción no debe presentarse como publicación simultánea con cada jornada de desarrollo.
