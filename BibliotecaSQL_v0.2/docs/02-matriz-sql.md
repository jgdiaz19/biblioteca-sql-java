# Matriz de correspondencia preliminar

| Contenido | Operación visible | Implementación |
|---|---|---|
| WHERE, ILIKE, parámetros JDBC | Buscar por título | `BookDao.SEARCH_SQL` |
| JOIN y filtro de nulos | Préstamos activos | `LoanDao.ACTIVE_SQL` |
| COUNT, GROUP BY, FILTER | Estadísticas | `BookDao.STATS_SQL` |
| SELECT, ORDER BY | Catálogo | `BookDao.LIST_SQL` |

**Nota:** la correspondencia exacta con números de ejercicios A7, A8 y A9 queda pendiente de cotejar sus cuadernos definitivos; no se debe presentar esta tabla como trazabilidad ya verificada.
