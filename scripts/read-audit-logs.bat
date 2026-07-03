@echo off
echo Lendo Logs de Auditoria do Banco de Dados PostgreSQL...
docker exec -i challenge-postgres psql -U admin -d solicitations_db -c "SELECT * FROM challenge.audit_logs ORDER BY timestamp DESC;"
echo.
echo Finalizado!
pause
