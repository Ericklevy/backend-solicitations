@echo off
cd /d "%~dp0.."
echo Executando testes automatizados via Docker (Maven)...
docker run -i --rm -e TESTCONTAINERS_RYUK_DISABLED=true -e TESTCONTAINERS_HOST_OVERRIDE=host.docker.internal -v "%cd%":/usr/src/app -v //var/run/docker.sock:/var/run/docker.sock -w /usr/src/app maven:3.9.5-eclipse-temurin-21 mvn test
echo Finalizado!
pause
