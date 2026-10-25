# Navegación en pantallas extensas

Las tablas destacan con `▶` el registro seleccionado. En el visor SQL o Java, el símbolo señala el principio del bloque visible. La barra lateral derecha resume la posición del desplazamiento dentro del contenido.

- ↑ y ↓: mover una línea o registro.
- Page Up y Page Down: cambiar de bloque.
- Home y End: ir al principio o al final.
- ← y → en la ventana didáctica: cambiar la sentencia registrada.
- 0 o ESC: regresar.

En `ClassicTui`, `selected` representa el índice de la fila activa, `offset` la primera fila visible, y `scroll` la primera línea visible en el código. Se dibuja de nuevo la pantalla después de cada tecla para mantener el indicador sincronizado.
