# Guías técnicas de BibliotecaSQL

Esta carpeta contiene la documentación correspondiente a **BibliotecaSQL v9.0.5**. Las instrucciones de instalación, ejecución y solución de problemas generales se encuentran en el [README principal](../README.md).

Las guías se organizan por **tema de estudio**, no por número de versión. De esta forma, cada documento puede actualizarse sin crear archivos adicionales para registrar cambios menores.

| Guía | Contenido | Lectura recomendada |
|---|---|---|
| [01. Arquitectura del sistema](01-arquitectura.md) | Relación entre interfaz, servicio, DAO y JDBC | Antes de leer las clases |
| [02. Modelo de datos](02-modelo-de-datos.md) | Esquema, claves foráneas, normalización y vistas | Antes de modificar SQL |
| [03. Operaciones y reglas de negocio](03-operaciones-y-reglas.md) | Catálogos, préstamos, devoluciones y transacciones | Antes de probar cambios de datos |
| [04. Laboratorio SQL](04-laboratorio.md) | Las 36 consultas organizadas en A7, A8 y A9 | Durante las prácticas SQL |
| [05. Interfaz y modo didáctico](05-interfaz-y-modo-didactico.md) | Navegación, búsquedas y lectura de SQL/Java | Al usar la aplicación |
| [06. Pruebas y mantenimiento](06-pruebas-y-mantenimiento.md) | Comprobaciones, respaldo y diagnóstico | Al modificar el proyecto |
| [07. Catálogo de libros: guía de código Java](07-catalogo-libros-java.md) | Análisis de clases, métodos y consultas del catálogo | Para estudiar una implementación completa |

## Secuencia de estudio propuesta

Una lectura útil comienza por las guías **01 y 02**, que explican la estructura del software y su base de datos. Después puede abrirse el catálogo de libros y seguir el recorrido descrito en la **guía 07**. La **guía 03** permite ampliar ese análisis a las operaciones con reglas de negocio; las guías **04 y 05** relacionan las consultas SQL con el modo didáctico. Finalmente, la **guía 06** explica cómo comprobar el funcionamiento después de modificar el código.

Las versiones antiguas, disponibles desde [HISTORIAL.md](../HISTORIAL.md), conservan documentos y decisiones anteriores. No deben combinarse sus instrucciones con las de esta versión sin comprobar que los métodos, tablas y menús sigan siendo los mismos.
