#!/bin/bash

echo "=== DIAGNÓSTICO DA APLICAÇÃO ==="
echo ""

# Teste 1: Verificar se a aplicação está respondendo
echo "1. Testando conectividade básica..."
curl -v -s http://localhost:8080/ 2>&1 | head -20

echo ""
echo "2. Testando endpoint de autenticação com verbose..."
curl -v -X POST http://localhost:8080/autenticacao \
-H "Content-Type: application/json" \
-d '{"usuario":"usuario","senha":"senha123"}' 2>&1 | head -30

echo ""
echo "3. Testando diferentes endpoints..."
echo "GET /"
curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/

echo "POST /autenticacao"
curl -s -o /dev/null -w "Status: %{http_code}\n" -X POST http://localhost:8080/autenticacao \
-H "Content-Type: application/json" \
-d '{"usuario":"usuario","senha":"senha123"}'

echo "POST /desafio/cadastrar (sem auth)"
curl -s -o /dev/null -w "Status: %{http_code}\n" -X POST http://localhost:8080/desafio/cadastrar \
-H "Content-Type: application/json" \
-d '{"posX":10,"posY":-5}'

echo ""
echo "4. Verificando logs do Docker..."
docker logs desafio_santander_app --tail 20
