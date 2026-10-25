#!/usr/bin/env bash
set -euo pipefail
# Obtenemos la carpeta raíz del repositorio.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
# Creamos un proyecto y un volumen aislados para las pruebas.
export COMPOSE_PROJECT_NAME=bibliotecasql_pruebas
cleanup() {
  # Eliminamos únicamente los contenedores y volúmenes de pruebas.
  docker compose -f docker-compose.test.yml down --volumes --remove-orphans
}
trap cleanup EXIT
# Reiniciamos el entorno de pruebas para partir siempre del mismo estado.
docker compose -f docker-compose.test.yml down --volumes --remove-orphans
docker compose -f docker-compose.test.yml up -d --wait
# Indicamos expresamente la dirección permitida para las pruebas.
export DB_TEST_URL="jdbc:postgresql://localhost:55433/biblioteca_operativa"
export DB_TEST_USER="admin"
export DB_TEST_PASSWORD="biblioteca_local"
mvn test
