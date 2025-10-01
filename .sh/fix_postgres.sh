#!/bin/bash

echo "=== Resolução do Problema PostgreSQL ==="

echo "1. Parando containers existentes..."
docker-compose down -v

echo "2. Removendo volumes antigos para reset completo..."
docker volume prune -f

echo "3. Iniciando containers com configuração corrigida..."
docker-compose up -d postgres redis

echo "4. Aguardando PostgreSQL inicializar (30 segundos)..."
sleep 30

echo "5. Verificando status dos containers..."
docker ps

echo "6. Testando conexão PostgreSQL..."
docker exec -it postgres pg_isready -U postgres -d agencia_db

echo "7. Se tudo estiver OK, você pode iniciar a aplicação com:"
echo "   ./gradlew bootRun"

echo ""
echo "=== Comandos para testar após a aplicação iniciar ==="
echo "# 1. Autenticação:"
echo "curl -X POST http://localhost:8080/autenticacao -H 'Content-Type: application/json' -d '{\"usuario\":\"usuario\",\"senha\":\"senha123\"}'"
echo ""
echo "# 2. Cadastrar agência (substitua SEU_TOKEN pelo token retornado):"
echo "curl -X POST http://localhost:8080/desafio/cadastrar -H 'Content-Type: application/json' -H 'Authorization: Bearer SEU_TOKEN' -d '{\"posX\":10,\"posY\":-5}'"
echo ""
echo "# 3. Consultar agências:"
echo "curl -X GET http://localhost:8080/desafio/consultar"
