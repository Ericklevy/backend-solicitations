#!/bin/bash
cd "$(dirname "$0")/.."
echo "Executando testes automatizados via Docker (Maven)..."
docker run -i --rm -e TESTCONTAINERS_RYUK_DISABLED=true -v "$PWD":/usr/src/app -v /var/run/docker.sock:/var/run/docker.sock -w /usr/src/app maven:3.9.5-eclipse-temurin-21 mvn test
echo "Finalizado!"
