#!/bin/bash

echo "=== Testando Endpoints do Desafio Santander ==="

echo -e "\n1. Testando endpoint de autenticação..."
AUTH_RESPONSE=$(curl -s --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--data '{"usuario":"usuario","senha":"senha123"}')

echo "Resposta da autenticação: $AUTH_RESPONSE"

# Extrair token da resposta (assumindo formato {"token":"Bearer xyz"})
TOKEN=$(echo $AUTH_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Token extraído: $TOKEN"

echo -e "\n2. Testando cadastro de agência com token..."
if [ ! -z "$TOKEN" ]; then
    CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header "Authorization: $TOKEN" \
    --data '{"posX":10,"posY":-5}')

    echo "Resposta do cadastro: $CADASTRO_RESPONSE"
else
    echo "Erro: Token não foi obtido. Testando com token fixo..."
    CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c3VhcmlvIiwiaWF0IjoxNzU5MzM0OTc0LCJleHAiOjE3NTkzMzg1NzR9.2PKsUkMpwwa1udXUmu2rgcjChvD8ma-hD3DqxlIL3Cs' \
    --data '{"posX":10,"posY":-5}')

    echo "Resposta do cadastro: $CADASTRO_RESPONSE"
fi

echo -e "\n3. Testando consulta de agências (sem autenticação)..."
CONSULTA_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/consultar')
echo "Resposta da consulta: $CONSULTA_RESPONSE"

echo -e "\n=== Teste concluído ==="
