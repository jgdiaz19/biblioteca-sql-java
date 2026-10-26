# Plan de organización en GitHub Projects

**Estado:** pendiente de creación y vinculación del tablero. Consulte el [Issue #19](https://github.com/jgdiaz19/biblioteca-sql-java/issues/19).

El proyecto debe permitir consultar dos conjuntos de trabajo: **hitos implementados históricamente** y **actividades de validación aún pendientes**. La finalidad es apoyar el estudio de la evolución del software sin confundir el registro retrospectivo con un seguimiento realizado en tiempo real durante su desarrollo.

## 1. Configuración recomendada

**Nombre:** `BibliotecaSQL — Desarrollo y mantenimiento`.

Propongo comenzar con una vista **Board** y añadir una vista **Table** para revisar las versiones y las evidencias. Los estados son:

| Estado | Interpretación |
|---|---|
| Pendiente | Actividad identificada, todavía no iniciada |
| En desarrollo | Cambio de código o documentación en elaboración |
| En revisión | Implementación realizada, pendiente de verificación |
| Completado | Trabajo atendido y documentado según su alcance |

Los campos adicionales pueden ser: **Tipo** (Funcionalidad, Corrección, Documentación, Pruebas), **Módulo** (Infraestructura, Catálogos, Préstamos, Interfaz, SQL), **Versión** y **Evidencia** (URL del commit, comparación o prueba).

## 2. Creación y vinculación

La creación del tablero debe hacerse desde la cuenta de GitHub que administra el repositorio:

1. Abra su perfil en GitHub y entre a **Projects → New project**.
2. Elija **Board**, asígnele el nombre indicado y cree el proyecto.
3. Desde el tablero, utilice **Add item from repository** y seleccione `jgdiaz19/biblioteca-sql-java`.
4. Agregue los Issues históricos [#4–#16](https://github.com/jgdiaz19/biblioteca-sql-java/issues?q=is%3Aissue+is%3Aclosed), incluidos los cerrados; asígnelos a **Completado**.
5. Incorpore las tareas abiertas [#17–#19](https://github.com/jgdiaz19/biblioteca-sql-java/issues?q=is%3Aissue+is%3Aopen) y colóquelas en el estado que corresponda.
6. En el repositorio, abra la pestaña **Projects**, seleccione **Link a project** y elija el nuevo tablero. También puede designar el repositorio como predeterminado desde la configuración del proyecto.
7. Configure la visibilidad apropiada para que los estudiantes puedan consultarlo; después añada su URL al README.

Consulte la [documentación de GitHub para crear proyectos](https://docs.github.com/en/issues/planning-and-tracking-with-projects/creating-projects/creating-a-project) y para [vincular un proyecto a un repositorio](https://docs.github.com/en/issues/planning-and-tracking-with-projects/managing-your-project/adding-your-project-to-a-repository).

## 3. Uso académico del tablero

Para analizar una tarea, el estudiante debe identificar el problema, revisar el Issue y comparar las etiquetas indicadas. Conviene distinguir los cambios de implementación, las correcciones derivadas de pruebas y las actualizaciones de documentación.

En particular, los 13 Issues cerrados se crearon **de manera retrospectiva**. Sus fechas en GitHub reflejan el registro posterior y no demuestran cuándo se inició o terminó originalmente cada actividad. Las validaciones abiertas permiten hacer explícito el trabajo aún no verificado.

Mientras el tablero no exista, la [relación de Issues documentados](ISSUES_PARA_GITHUB.md) seguirá siendo la referencia disponible.
