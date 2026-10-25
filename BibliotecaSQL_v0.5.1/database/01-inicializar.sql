-- Creamos las dos bases de datos utilizadas por BibliotecaSQL.
CREATE DATABASE biblioteca_universitaria;
CREATE DATABASE biblioteca_operativa;

-- Cargamos el esquema y los datos originales para las actividades.
\connect biblioteca_universitaria
\set ON_ERROR_STOP on
\i /scripts/02-esquema.sql
\i /scripts/03-datos.sql

-- Cargamos una copia de trabajo para registrar préstamos y devoluciones.
\connect biblioteca_operativa
\i /scripts/02-esquema.sql
\i /scripts/03-datos.sql
\i /scripts/04-conciliar.sql
