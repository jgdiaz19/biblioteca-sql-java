# Publicación de la reconstrucción en GitHub

El bundle `BibliotecaSQL_historial.bundle` contiene commits y etiquetas, incluso sin una carpeta `.git` visible.

1. En GitHub, cree un repositorio público **vacío** llamado `biblioteca-sql-java`, bajo la cuenta apropiada. **No marque** README, licencia ni `.gitignore` al crearlo.
2. En su computadora, extraiga `BibliotecaSQL_historial.bundle` del paquete de entrega y ejecute:

```bash
git clone BibliotecaSQL_historial.bundle biblioteca-sql-java
cd biblioteca-sql-java
git remote remove origin
git remote add origin https://github.com/jgdiaz19/biblioteca-sql-java.git
git push -u origin main
git push origin --tags
```

3. Compruebe las etiquetas desde **Code → Tags**. En **Releases → Draft a new release**, elija `v9.0.5` y publique una Release con el título `BibliotecaSQL v9.0.5` y el resumen de novedades. GitHub ofrecerá automáticamente descargar el código de esa etiqueta en ZIP o TAR.GZ. Puede adjuntar asimismo el ZIP original `BibliotecaSQL_v9.0.5.zip` si desea una distribución explícita.
4. Abra **Projects → New project** desde el perfil/organización. Cree una vista tipo tabla o tablero, agregue las tareas del archivo `ISSUES_PARA_GITHUB.md` como Issues del repositorio y añádalas al proyecto. Vincule el Project al repositorio desde la configuración de Projects cuando la interfaz lo permita.
5. En la descripción del repositorio enlace `HISTORIAL.md` y el Project. En About añada temas: `java`, `postgresql`, `jdbc`, `educacion`, `sql`, `lanterna`.

**Integridad:** las etiquetas son instantáneas reconstruidas. No cambie la fecha de los commits para fingir que fueron publicados anteriormente. Conserve la aclaración del documento `HISTORIAL.md`.

**Seguridad:** revise las credenciales demostrativas de Docker antes de hacer público el repositorio; nunca publique contraseñas reales ni archivos `.env`.
