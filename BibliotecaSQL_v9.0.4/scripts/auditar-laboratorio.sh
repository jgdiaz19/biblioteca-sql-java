#!/usr/bin/env bash
set -euo pipefail
# Ejecutamos los 36 ejemplos sin modificar los datos.
docker compose up -d --wait
mvn -q compile exec:java -Dexec.mainClass=edu.uv.biblioteca.dao.LaboratoryAudit
