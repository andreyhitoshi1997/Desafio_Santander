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

# Extrair token da resposta (corrigido para o campo 'bearer')
BEARER_TOKEN=$(echo $AUTH_RESPONSE | sed -n 's/.*"bearer":"\([^"]*\)".*/\1/p')
echo "Token extraído: $BEARER_TOKEN"

echo -e "\n2. Testando cadastro de agência com token extraído..."
if [ ! -z "$BEARER_TOKEN" ]; then
    CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header "Authorization: $BEARER_TOKEN" \
    --data '{"posX":10,"posY":-5}')

    echo "Resposta do cadastro: $CADASTRO_RESPONSE"
else
    echo "Token não obtido. Testando com um novo token..."
    # Tentar obter um novo token
    NEW_AUTH=$(curl -s --location 'http://localhost:8080/autenticacao' \
    --header 'Content-Type: application/json' \
    --data '{"usuario":"usuario","senha":"senha123"}')

    NEW_TOKEN=$(echo $NEW_AUTH | sed -n 's/.*"bearer":"\([^"]*\)".*/\1/p')

    if [ ! -z "$NEW_TOKEN" ]; then
        CADASTRO_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/cadastrar' \
        --header 'Content-Type: application/json' \
        --header "Authorization: $NEW_TOKEN" \
        --data '{"posX":10,"posY":-5}')
        echo "Resposta do cadastro com novo token: $CADASTRO_RESPONSE"
    else
        echo "Não foi possível obter token válido"
    fi
fi

echo -e "\n3. Testando consulta de agências..."
# Nota: Este endpoint não existe ainda, então retornará 404
CONSULTA_RESPONSE=$(curl -s --location 'http://localhost:8080/desafio/consultar')
echo "Resposta da consulta: $CONSULTA_RESPONSE"

echo -e "\n=== Seus curls originais ==="
echo -e "\n4. Testando seu curl de autenticação original..."
curl --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=355FD98C92F7CF925B6466E56BF6E3A1' \
--data '{"usuario":"usuario","senha":"senha123"}'

echo -e "\n\n5. Testando seu curl de cadastro original (com token fresco)..."
# Obter token novo para o teste
FRESH_AUTH=$(curl -s --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--data '{"usuario":"usuario","senha":"senha123"}')

FRESH_TOKEN=$(echo $FRESH_AUTH | sed -n 's/.*"bearer":"\([^"]*\)".*/\1/p')

if [ ! -z "$FRESH_TOKEN" ]; then
    curl --location 'http://localhost:8080/desafio/cadastrar' \
    --header 'Content-Type: application/json' \
    --header "Authorization: $FRESH_TOKEN" \
    --header 'Cookie: JSESSIONID=355FD98C92F7CF925B6466E56BF6E3A1' \
    --data '{"posX":10,"posY":-5}'
else
    echo "Não foi possível obter token para o teste de cadastro"
fi

echo -e "\n\n=== Teste concluído ==="
