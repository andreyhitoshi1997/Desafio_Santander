#!/bin/bash

echo "=== Testando Endpoints do Desafio Santander ==="

# Primeiro, vamos testar se a aplicação está rodando
echo -e "\n0. Verificando se a aplicação está rodando..."
curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/autenticacao || echo "Aplicação não está respondendo"

echo -e "\n1. Testando endpoint de autenticação..."
AUTH_RESPONSE=$(curl -s --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--data '{"usuario":"usuario","senha":"senha123"}')

echo "Resposta da autenticação: $AUTH_RESPONSE"

# Extrair token da resposta
TOKEN=$(echo $AUTH_RESPONSE | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
echo "Token extraído: $TOKEN"

echo -e "\n2. Testando cadastro de agência com token extraído..."
if [ ! -z "$TOKEN" ]; then
    CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header "Authorization: $TOKEN" \
    --data '{"posX":10,"posY":-5}')

    echo "Resposta do cadastro: $CADASTRO_RESPONSE"
else
    echo "Token não obtido. Testando com o token do seu exemplo..."
    CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvIiwiaWF0IjoxNzU5MzM0OTc0LCJleHAiOjE3NTkzMzg1NzR9.2PKsUkMpwwa1udXUmu2rgcjChvD8ma-hD3DqxlIL3Cs' \
    --data '{"posX":10,"posY":-5}')

    echo "Resposta do cadastro: $CADASTRO_RESPONSE"
fi

echo -e "\n3. Testando consulta de agências..."
CONSULTA_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/consultar')
echo "Resposta da consulta: $CONSULTA_RESPONSE"

echo -e "\n=== Seus curls originais ==="
echo -e "\n4. Testando seu curl de autenticação original..."
curl --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=355FD98C92F7CF925B6466E56BF6E3A1' \
--data '{"usuario":"usuario","senha":"senha123"}'

echo -e "\n\n5. Testando seu curl de cadastro original..."
curl --location 'http://localhost:8080/desafio/cadastrar' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvIiwiaWF0IjoxNzU5MzM0OTc0LCJleHAiOjE3NTkzMzg1NzR9.2PKsUkMpwwa1udXUmu2rgcjChvD8ma-hD3DqxlIL3Cs' \
--header 'Cookie: JSESSIONID=355FD98C92F7CF925B6466E56BF6E3A1' \
--data '{"posX":10,"posY":-5}'

echo -e "\n\n=== Teste concluído ==="
