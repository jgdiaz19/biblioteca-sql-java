# Interfaz de consola clásica

La versión 0.7 utiliza Lanterna para leer cada tecla y actualizar la pantalla sin esperar ENTER. Se ejecuta en terminales compatibles de Windows, Linux y macOS.

## Navegación

- En el menú principal, las teclas `1` a `9` abren las opciones; `0` sale. Las flechas y ENTER también funcionan.
- En el catálogo, `1` o `/` activa la búsqueda. Se actualiza la tabla tras cada carácter, incluyendo retroceso.
- En el catálogo, `2` alterna entre filtros en memoria y consultas `ILIKE` a PostgreSQL. Se preserva el término introducido.
- Flechas y Page Up / Page Down recorren las filas; ENTER abre el detalle del libro.
- ESC cancela la edición del campo o regresa a la pantalla anterior; `0` regresa cuando no se está editando.
- En los demás reportes, `1` o `/` abre un filtro incremental en memoria que busca sobre las columnas visibles.

## Qué hace cada capa

- `ui/ClassicTui.java`: captura teclas, presenta menús y tablas, administra el estado del filtro.
- `service/LibraryService.java`: proporciona las operaciones del sistema sin depender de la interfaz.
- `dao/BookDao.java`: ejecuta consultas JDBC y vincula los parámetros.
- `service/LoanTransactionService.java`: ejecuta los préstamos y devoluciones con transacciones.

## Búsqueda incremental

El modo **Java** mantiene la lista completa en memoria y compara texto en minúsculas. El modo **PostgreSQL** vuelve a consultar con `PreparedStatement` y `CONCAT_WS(...) ILIKE ?`.

El conjunto oficial de la biblioteca tiene 24 libros; para este volumen resulta aceptable ejecutar la consulta al teclear. Con bases de datos grandes, conviene incorporar temporización, paginación SQL y ejecución asíncrona para que la pantalla no se detenga con consultas lentas.

**Alcance:** la búsqueda incremental está implementada en el catálogo de libros. Las tablas de préstamos, estadísticas y laboratorios también incluyen búsqueda incremental en memoria. La consulta dinámica PostgreSQL está disponible específicamente en el catálogo de libros.

## Requisitos

Se recomienda una ventana de terminal de al menos 80 columnas por 24 filas, UTF-8 y un tipo de letra monoespaciado. El modo visual es una recreación didáctica de entornos DOS, no una reproducción exacta de Borland. El monograma UV es tipográfico y no sustituye al escudo institucional.

El diseño no exige teclas Fn. Algunas terminales incorporadas requieren abrir una terminal externa para la captura de teclado de pantalla completa.
