#!/bin/bash

echo "🚀 Iniciando serviços para desenvolvimento..."

# Subir apenas PostgreSQL e Elasticsearch
docker-compose up -d postgres elasticsearch

echo "⏳ Aguardando serviços ficarem prontos..."
sleep 10

echo "✅ PostgreSQL: localhost:5433"
echo "✅ Elasticsearch: http://localhost:9201"
echo ""
echo "📝 Para rodar a aplicação: mvn spring-boot:run"
echo "📚 Swagger: http://localhost:8080/swagger-ui.html"