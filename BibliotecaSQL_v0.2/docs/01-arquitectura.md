# Arquitectura de BibliotecaSQL 0.2

`Main` conecta a PostgreSQL y construye las dependencias. `MenuController` controla la navegación; `ConsoleView` presenta los datos; `LibraryService` valida las entradas; los DAO encapsulan SQL y convierten `ResultSet` a objetos inmutables (`record`).

```text
Main → MenuController → LibraryService → BookDao/LoanDao → JDBC → PostgreSQL
           ↓                                   ↓
       ConsoleView                       Modelos record
```

**Aislamiento:** Las clases `ui` no utilizan JDBC; los DAO no leen teclado ni imprimen en pantalla. La misma conexión se comparte en v0.2, para simplificar el ejemplo de consola monousuario.

**Modo didáctico:** muestra exactamente la consulta parametrizada y el valor del parámetro; no concatena entrada de usuarios como SQL. No ejecuta SQL arbitrario.

**Datos:** la base `biblioteca_universitaria` se conserva para las actividades originales; `biblioteca_operativa` tiene disponibilidad conciliada. En v0.2 no se modifican registros desde la aplicación.

**Siguiente etapa:** servicios transaccionales de préstamo y devolución, respetando los préstamos abiertos existentes y con pruebas de integración.
