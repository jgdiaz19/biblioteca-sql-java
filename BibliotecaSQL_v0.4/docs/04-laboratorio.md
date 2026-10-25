# Laboratorio SQL: biblioteca universitaria

El laboratorio ejecuta consultas fijas de solo lectura. Puede utilizarse tanto con la base académica como con la operativa.

| Opción | Contenido | Correspondencia |
|---|---|---|
| 1 | UPPER y LENGTH | A7, funciones escalares |
| 2 | COUNT, AVG, GROUP BY y HAVING | A7, agregación y agrupamiento |
| 3 | generate_series | A7, funciones de conjunto |
| 4 | JOIN libros-autores | Tema JOIN; verificar ejercicio concreto de A8 |
| 5 | NOT EXISTS | Tema subconsultas; verificar ejercicio concreto de A8/A9 |
| 6 | Subconsulta con AVG | Tema subconsultas; verificar ejercicio concreto de A8/A9 |
| 7 | LEFT JOIN y ausencia de coincidencias | Tema JOIN; verificar ejercicio concreto de A8 |

La asignación de los ejercicios específicos A8 y A9 se realizará al cotejar los cuadernos definitivos. La etiqueta temática no significa que la consulta reproduzca literalmente un ejercicio.

## Actividades propuestas

1. Ejecute la consulta 1 y explique la diferencia entre UPPER y LENGTH.
2. En la consulta 2, cambie temporalmente el valor de HAVING y compare resultados.
3. Dibuje la relación entre autores y libros usada por la consulta 4.
4. Explique por qué NOT EXISTS de la consulta 5 considera todos los préstamos históricos.
5. Ejecute la consulta 7 e indique por qué aparecen usuarios sin préstamos.

No se permite ingresar instrucciones SQL arbitrarias desde la consola. Para experimentar, los estudiantes deben editar las consultas del archivo `LabDao.java`, recompilar y verificar los resultados.
