#!/bin/bash
# compile.sh - Script para compilar o projeto ServidorCalculo

echo "Compilando o projeto ServidorCalculo..."
mkdir -p bin
javac -d bin src/ServidorCalculo.java src/ClienteCalculo.java src/TesteMulticliente.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✔ Compilação concluída com sucesso!"
    echo ""
    echo "Para executar:"
    echo "  1) Servidor:            java -cp bin ServidorCalculo"
    echo "  2) Cliente interativo:  java -cp bin ClienteCalculo"
    echo "  3) Teste multicliente:  java -cp bin TesteMulticliente"
else
    echo "✘ Erro na compilação."
fi
