# Matriz de trazabilidad SQL y Java

Esta matriz distingue entre contenidos efectivamente identificados en el cuaderno A7 y ejemplos de ampliación cuyo identificador exacto requiere contrastarse con A8 y A9. No atribuimos ejercicios específicos sin verificar su enunciado.

| Contenido | Consulta o comportamiento | Archivo Java | Menú | Correspondencia |
|---|---|---|---|---|
| Funciones de texto | UPPER, LENGTH | `dao/LabDao.java` | 9.1 | A7, funciones escalares |
| Agregación | COUNT, AVG, GROUP BY, HAVING | `dao/LabDao.java` | 9.2 | A7, agregación y agrupamiento |
| Funciones de conjunto | generate_series | `dao/LabDao.java` | 9.3 | A7, funciones generadoras de filas |
| JOIN | Libros relacionados con autores | `dao/LabDao.java` | 9.4 | Contenido de JOIN; verificar ejercicio A8 |
| Subconsulta correlacionada | NOT EXISTS sobre préstamos | `dao/LabDao.java` | 9.5 | Contenido de subconsultas; verificar A8/A9 |
| Subconsulta escalar | Precio superior al promedio | `dao/LabDao.java` | 9.6 | Contenido de subconsultas; verificar A8/A9 |
| OUTER JOIN | Usuarios sin préstamos abiertos | `dao/LabDao.java` | 9.7 | Contenido de JOIN; verificar ejercicio A8 |
| Informes | Préstamos vencidos y resumen por usuario | `dao/ReportDao.java` | 7, 8 | Aplicación transversal |
| Reglas de negocio | Préstamo y devolución transaccionales | `service/LoanTransactionService.java` | 5, 6 | Extensión práctica, no equivalencia directa con ejercicios |

## Criterio de correspondencia

Una consulta se considerará equivalente a un ejercicio solo cuando coincidan las tablas, los filtros, la agrupación y los resultados esperados. Las consultas adicionales se presentan como aplicaciones, no como reproducciones literales de los cuadernos.
