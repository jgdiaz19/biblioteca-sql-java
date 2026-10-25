#!/bin/sh
set -eu
for db in biblioteca_universitaria biblioteca_operativa; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres -c "CREATE DATABASE $db;"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" -f /scripts/02-esquema.sql
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" -f /scripts/03-datos.sql
done
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname biblioteca_operativa -f /scripts/04-conciliar.sql
