# Lectura del código por capas

1. Empiece en `Main.java`: allí creamos la conexión e inicializamos la interfaz y los servicios.
2. Revise `ConsoleView.java`: controla la lectura del teclado y la impresión de tablas.
3. Abra `MenuController.java`: cada opción solicita datos y llama al servicio correspondiente.
4. Lea `LibraryService.java`: delega las consultas y valida que las búsquedas sean adecuadas.
5. Continúe en `BookDao`, `LoanDao`, `ReportDao` y `LabDao`: allí se encuentran las sentencias SQL.
6. Estudie `LoanTransactionService.java`: observe `setAutoCommit(false)`, las validaciones, `commit` y `rollback`.
7. Analice `model/`: cada `record` agrupa los datos que utiliza una pantalla.

## Convenciones de comentarios

Los comentarios explican operaciones relevantes sin repetir literalmente cada instrucción. Se emplean frases cortas como «Inicializamos...», «Validamos...» y «Consultamos...».

## Práctica sugerida

Añada una consulta al laboratorio que cuente libros por autor. Identifique los cambios necesarios en DAO, servicio y presentación. Después verifique que el catálogo y los préstamos continúen funcionando.
