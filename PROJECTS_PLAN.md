# Propuesta de seguimiento en GitHub Projects

El tablero de BibliotecaSQL debe ayudar a reconstruir **qué decisiones se tomaron y cómo se verificaron**, no solamente mostrar una sucesión de versiones. Este documento establece una organización para trasladar las tareas de [ISSUES_PARA_GITHUB.md](ISSUES_PARA_GITHUB.md) a GitHub Projects.

## Organización recomendada

Utilice una vista de tablero para el estado de trabajo y una vista de tabla para consultar versiones y evidencias.

| Campo | Uso |
|---|---|
| Estado | Pendiente, En desarrollo, En revisión o Completado |
| Tipo | Funcionalidad, Corrección, Documentación o Pruebas |
| Módulo | Base de datos, Catálogos, Préstamos, Interfaz o Laboratorio SQL |
| Versión | Etiqueta donde se incorporó el cambio |
| Fecha del hito | Fecha aproximada respaldada por el historial |
| Evidencia | Commit, comparación de etiquetas, prueba o revisión |

**Importante:** una tarea implementada en una versión histórica no equivale necesariamente a una tarea verificada mediante pruebas integrales. Cuando no exista evidencia de ejecución real, consérvela en estado de revisión o abra una tarea específica de validación.

## Procedimiento para organizar el proyecto

1. Cree un tablero denominado **BibliotecaSQL — Desarrollo y mantenimiento**.
2. Registre los 13 hitos de [ISSUES_PARA_GITHUB.md](ISSUES_PARA_GITHUB.md) como tareas retrospectivas, o vincule los Issues correspondientes cuando existan.
3. Asigne a cada tarea el tipo, el módulo y las versiones relacionadas.
4. Incluya un enlace al commit o a la comparación de etiquetas para que sea posible examinar el cambio.
5. Mantenga una sección de trabajo futuro separada de los hitos ya implementados.
6. Publique el enlace del tablero en el README principal una vez que esté disponible.

## Uso didáctico

Los estudiantes pueden seleccionar una tarea, recuperar la versión anterior y explicar qué limitación presentaba. Después podrán comparar los archivos Java o SQL modificados y verificar si el cambio resolvió el problema identificado.

Este procedimiento permite estudiar la trazabilidad de requisitos y la revisión de software sin presentar el historial reconstruido como si las tareas se hubieran registrado durante cada jornada original de desarrollo.
