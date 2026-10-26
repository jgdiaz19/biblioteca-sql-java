# Interfaz de consola y modo didáctico

BibliotecaSQL utiliza **Lanterna** para construir una interfaz de texto con menús, marcos, tablas y ventanas emergentes. Su diseño recupera características de las aplicaciones de consola clásicas, pero evita depender de las teclas de función, que pueden requerir combinaciones adicionales en algunas laptops.

## 1. Navegación

| Tecla | Función habitual |
|---|---|
| `1` a `9` | Seleccionar una opción visible |
| `0` | Regresar o salir del menú |
| `↑`, `↓` | Cambiar de registro o línea |
| `Page Up`, `Page Down` | Desplazarse por bloques |
| `Home`, `End` | Ir al inicio o al final donde se admita |
| `ENTER` | Abrir el registro o confirmar una selección |
| `ESC` | Cancelar o cerrar una ventana |
| `1` o `/` en tablas compatibles | Activar búsqueda incremental |
| `D` en el menú principal | Activar o desactivar el modo didáctico |

Las acciones son **contextuales**. Por ejemplo, en el catálogo de libros la tecla `4` inicia el alta, mientras que en el menú principal abre estadísticas. Las indicaciones al pie de la ventana establecen qué operación está disponible en cada momento.

El apuntador `▶` o `>` indica la selección. Las tablas pueden desplazarse cuando el contenido excede el espacio visible.

## 2. Búsqueda incremental de libros

El catálogo permite introducir caracteres y actualizar las coincidencias conforme se escribe. La opción **2. Cambiar filtro** alterna entre dos métodos:

| Estrategia | Ejecución |
|---|---|
| Java / en memoria | La aplicación filtra una lista de objetos `Book` que ya cargó |
| PostgreSQL / `ILIKE` | Java ejecuta otra consulta parametrizada para recuperar coincidencias |

El cambio de estrategia restablece el término de búsqueda y la selección. En autores, categorías, editoriales y otros reportes, el filtrado se realiza sobre las filas que están cargadas en memoria.

Se recomienda comparar ambos métodos en el código para identificar el costo de consultar nuevamente el SGBD y la diferencia entre procesar datos en Java y procesarlos mediante SQL.

## 3. Contenido del modo didáctico

Pulse `D` en el menú principal. Después de ejecutar una consulta, las pantallas que ofrecen `3. Ver SQL` permiten abrir el visor.

| Tecla del visor | Contenido |
|---|---|
| `1` | Sentencia SQL ejecutada, con resaltado de sintaxis |
| `2` | Fragmento del código Java relacionado |
| `3` | Explicación SQL |
| `4` | Explicación Java |
| `←`, `→` | Cambiar entre las sentencias registradas |
| `↑`, `↓` | Recorrer las líneas |
| `Page Up`, `Page Down` | Avanzar por bloques |
| `0`, `ESC` | Cerrar el visor |

`SqlTrace` conserva hasta **40 sentencias** por recorrido. Identifica las consultas cuando JDBC invoca métodos como `executeQuery()` o `executeUpdate()`. Aunque la traza conserva parámetros técnicos, estos no aparecen como un panel independiente; los estudiantes pueden estudiar los métodos `setString()` y `setInt()` directamente en la vista Java.

La clase `JavaSourceView` consulta archivos bajo `src/main/java`. Por eso debe ejecutarse Maven **desde la raíz del repositorio**. La pantalla muestra un fragmento contextual, no necesariamente el método completo.

Las explicaciones y el coloreado se basan en reglas de reconocimiento relativamente sencillas; sirven para orientar la lectura, pero no sustituyen un análisis técnico de la consulta.

## 4. Requisitos de presentación

Para mejorar la legibilidad se recomienda una terminal de **80 columnas por 24 filas como mínimo**, fuente monoespaciada y UTF-8. Cuando la terminal integrada del IDE no interprete adecuadamente las teclas o el dibujo de marcos, utilice una terminal nativa.

La pantalla de inicio muestra un monograma tipográfico de la UV, el nombre de la Facultad de Negocios y Tecnologías, campus Ixtaczoquitlán, y los créditos *Designed and Developed by JEGD*. El monograma no pretende sustituir al escudo oficial.
