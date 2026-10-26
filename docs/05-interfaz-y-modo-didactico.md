# Interfaz clásica y modo didáctico

**Versión:** BibliotecaSQL v9.0.5. La capa de presentación utiliza **Lanterna**: una interfaz de texto con fondo azul, marcos de caracteres, ventanas emergentes, menús numéricos y búsqueda incremental.

## Menú principal

| Tecla | Opción |
|---|---|
| `1` | Catálogo de libros |
| `2` | Administración de autores, categorías y editoriales |
| `3` | Préstamos activos |
| `4` | Estadísticas por categoría |
| `5` | Registrar préstamo |
| `6` | Registrar devolución |
| `7` | Préstamos vencidos |
| `8` | Préstamos por usuario |
| `9` | Laboratorios SQL |
| `D` | Activar o desactivar el modo didáctico |
| `0` | Salir |

No se necesitan teclas de función ni combinaciones `Fn`, lo que permite utilizar el sistema en laptops con Windows, Linux o macOS.

## Navegación y tablas

- `1` o `/`: iniciar búsqueda en las tablas que la admiten.
- `↑` y `↓`: mover el registro o la línea seleccionada.
- `Page Up` y `Page Down`: avanzar o retroceder por bloques.
- `Home` y `End`: ir a los extremos donde estén implementados.
- `ENTER`: elegir un elemento o consultar el detalle.
- `0` o `ESC`: regresar o cancelar, según el contexto.
- `4`, `5`, `6`: agregar, modificar y eliminar en catálogos donde se presenten esas opciones.

Las funciones se indican al pie de cada pantalla. La selección se señala mediante `▶` o `>` y las pantallas extensas incorporan indicadores de desplazamiento.

## Dos estrategias de búsqueda de libros

En el catálogo de libros, **2. Cambiar filtro** alterna entre:

| Modo | Cómo funciona |
|---|---|
| Java / en memoria | Filtra objetos ya cargados, comparando cadenas |
| PostgreSQL / ILIKE | Ejecuta otra consulta JDBC parametrizada con `ILIKE ?` |

**Al cambiar de modo, el término de búsqueda se limpia** y se vuelve al catálogo completo. En los otros catálogos y reportes, la búsqueda incremental filtra los registros que ya están en memoria.

**Ejercicio:** busque el mismo término de ambas formas, observe el modo didáctico y explique en cuál de los dos casos se ejecuta SQL durante la búsqueda.

## Modo didáctico

Desde el menú principal, pulse `D` para activarlo. Ejecute una consulta y use `3. Ver SQL` en las pantallas que ofrezcan esta opción.

| Tecla del visor | Vista |
|---|---|
| `1` | Sentencia SQL realmente ejecutada, con colores |
| `2` | Fragmento de Java que prepara o ejecuta la sentencia, con colores |
| `3` | Explicación SQL |
| `4` | Explicación Java |
| `←` / `→` | Cambiar entre sentencias ejecutadas |
| `↑` / `↓` | Recorrer las líneas de la vista |
| `Page Up` / `Page Down` | Avanzar o retroceder por bloques |
| `0` / `ESC` | Cerrar el visor |

`SqlTrace` registra las sentencias cuando JDBC llama a `executeQuery`, `executeUpdate` o `execute` y conserva hasta **40 entradas**. Los parámetros se almacenan para la traza técnica, pero no aparecen como panel independiente en la interfaz.

`JavaSourceView` busca el archivo original dentro de `src/main/java` y muestra un fragmento cercano a la ejecución. **Inicie Maven desde la raíz del repositorio** para que encuentre el código fuente. El fragmento no equivale necesariamente al método completo; abra el archivo indicado en VS Code si necesita más contexto.

Las explicaciones se elaboran parcialmente a partir de patrones reconocidos por el programa. Son un apoyo, **no una verificación exhaustiva de la lógica SQL o Java**. El coloreado utiliza un analizador léxico ligero.

## Recomendaciones de terminal

Utilice una terminal de al menos **80 columnas × 24 filas**, fuente monoespaciada y UTF-8. Para dificultades con marcos, flechas o colores, pruebe Windows Terminal, Terminal de macOS o una terminal Linux en vez de la terminal integrada del IDE.

El splash screen conserva el monograma ASCII UV, el nombre de la Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán, la Universidad Veracruzana y los créditos **Designed and Developed by JEGD**. El monograma no sustituye al escudo institucional oficial.
