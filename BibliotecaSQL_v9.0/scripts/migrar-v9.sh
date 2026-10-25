#!/usr/bin/env bash
set -euo pipefail
# Creamos un respaldo antes de cambiar el esquema operativo.
mkdir -p respaldos
file="respaldos/biblioteca_operativa_$(date +%Y%m%d_%H%M%S).sql"
docker compose exec -T postgres pg_dump -U admin -d biblioteca_operativa > "$file"
echo "Respaldo guardado en $file"
# Aplicamos la migración dentro de PostgreSQL. Puede ejecutarse de nuevo sin alterar los datos.
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U admin -d biblioteca_operativa -f /scripts/05-normalizar-catalogos.sql
# Validamos las relaciones al concluir.
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U admin -d biblioteca_operativa -c "SELECT count(*) AS libros, count(id_categoria) AS categorias_asignadas FROM libros;"
echo "Migración completada."
