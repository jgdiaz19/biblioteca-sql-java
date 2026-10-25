-- Transcripción del dataset público de referencia. Se ejecuta solo en la base didáctica.
INSERT INTO autores (nombre,nacionalidad) VALUES
('Abraham Silberschatz','India'),('Ramez Elmasri','Egipto'),('C. J. Date','Reino Unido'),('Robert C. Martin','Estados Unidos'),('Ian Sommerville','Reino Unido'),('Martin Fowler','Reino Unido'),('Luciano Ramalho','Brasil'),('Eric Matthes','Estados Unidos'),('Andrew S. Tanenbaum','Estados Unidos'),('Thomas H. Cormen','Estados Unidos'),('Ana Torres','Mexico'),('Luis Mendoza',NULL);
INSERT INTO libros (titulo,isbn,anio_publicacion,categoria,editorial,numero_paginas,precio,disponible,id_autor) VALUES
('Fundamentos de Bases de Datos','978000000001',2021,'Bases de datos','Alpha',620,650.00,TRUE,2),
('Sistemas de Bases de Datos','978000000002',2020,'Bases de datos','Beta',710,780.00,FALSE,1),
('SQL para analisis de datos','978000000003',2024,'Bases de datos','Gamma',360,540.00,TRUE,11),
('PostgreSQL practico','978000000004',2025,'Bases de datos','Gamma',410,580.00,TRUE,12),
('Introduccion a SQL','978000000005',2018,'Bases de datos','Delta',290,300.00,TRUE,3),
('Clean Code','978000000006',2008,'Ingenieria de Software','Prentice',464,690.00,TRUE,4),
('Ingenieria de Software Moderna','978000000007',2023,'Ingenieria de Software','Omega',520,790.00,TRUE,5),
('Refactoring','978000000008',2019,'Ingenieria de Software','Addison',448,850.00,FALSE,6),
('Patrones para software mantenible','978000000009',2022,'Ingenieria de Software',NULL,390,610.00,TRUE,11),
('Programacion con Python','978000000010',2024,'Programacion','No Starch',544,720.00,TRUE,8),
('Python fluido','978000000011',2022,'Programacion','OReilly',1014,980.00,FALSE,7),
('Algoritmos esenciales','978000000012',2021,'Programacion','MIT Press',510,620.00,TRUE,10),
('Estructuras de datos con ejemplos','978000000013',2020,'Programacion','Sigma',430,560.00,TRUE,10),
('Redes de computadoras','978000000014',2021,'Redes','Pearson',960,920.00,TRUE,9),
('Redes modernas y servicios','978000000015',2025,'Redes','Sigma',470,760.00,FALSE,12),
('Seguridad en redes','978000000016',2023,'Seguridad','Omega',390,680.00,TRUE,11),
('Introduccion a la ciberseguridad','978000000017',2024,'Seguridad','Omega',340,490.00,TRUE,12),
('Datos, informacion y conocimiento','978000000018',2017,'Sistemas de informacion','Delta',250,280.00,TRUE,11),
('Analitica de datos aplicada','978000000019',2026,'Ciencia de datos','Gamma',430,880.00,TRUE,12),
('Mineria de Datos','978000000020',2022,'Ciencia de datos','Alpha',580,810.00,FALSE,11),
('Diseño de interfaces','978000000021',2021,'Interaccion Humano-Computadora','Beta',310,450.00,TRUE,12),
('Experiencia de usuario','978000000022',2023,'Interaccion Humano-Computadora',NULL,330,510.00,TRUE,11),
('Arquitectura de software','978000000023',2025,'Ingenieria de Software','Alpha',600,900.00,TRUE,5),
('Bases de Datos Distribuidas','978000000024',2026,'Bases de datos','Alpha',480,840.00,TRUE,2);
INSERT INTO usuarios (nombre,tipo_usuario,carrera,activo) VALUES
('Andrea Lopez','Estudiante','Ingenieria de Software',TRUE),('Carlos Mendez','Estudiante','Ingenieria de Software',TRUE),('Fernanda Ruiz','Estudiante','Tecnologias de la Informacion',TRUE),('Miguel Santos','Docente',NULL,TRUE),('Laura Perez','Estudiante','Administracion',TRUE),('Jose Ramirez','Administrativo',NULL,TRUE),('Sofia Hernandez','Estudiante','Ingenieria de Software',FALSE),('Daniel Cruz','Docente',NULL,TRUE),('Mariana Vega','Estudiante','Contaduria',TRUE),('Ricardo Flores','Estudiante','Ingenieria de Software',TRUE);
INSERT INTO prestamos (id_libro,id_usuario,fecha_prestamo,fecha_devolucion,fecha_devolucion_real) VALUES
(2,1,'2026-08-01','2026-08-15','2026-08-14'),(6,2,'2026-08-10','2026-08-24','2026-08-25'),(8,3,'2026-08-15','2026-08-29',NULL),(10,1,'2026-08-20','2026-09-03','2026-09-02'),(11,5,'2026-08-22','2026-09-05',NULL),(14,2,'2026-08-25','2026-09-08','2026-09-08'),(15,4,'2026-08-28','2026-09-11',NULL),(3,6,'2026-09-01','2026-09-15',NULL),(4,10,'2026-09-02','2026-09-16',NULL),(7,9,'2026-09-03','2026-09-17','2026-09-10'),(9,8,'2026-09-04','2026-09-18',NULL),(12,3,'2026-09-05','2026-09-19',NULL),(16,1,'2026-09-06','2026-09-20',NULL),(17,2,'2026-09-07','2026-09-21',NULL),(19,4,'2026-09-08','2026-09-22',NULL),(20,5,'2026-09-09','2026-09-23',NULL),(23,10,'2026-09-10','2026-09-24',NULL),(24,6,'2026-09-11','2026-09-25',NULL);
