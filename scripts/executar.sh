#!/bin/bash
# Compila o projeto (sem precisar do Maven) e executa um modo do experimento.
#
# Uso (a partir da pasta projeto-eda-unidade1):
#   ./scripts/executar.sh teste
#   ./scripts/executar.sh busca
#   ./scripts/executar.sh ordenacao
#   ./scripts/executar.sh ordenacao-grande
#   ./scripts/executar.sh ordenacao 10000,20000,40000   (tamanhos personalizados)
#

set -e
cd "$(dirname "$0")/.."   # vai para a raiz do projeto, de onde quer que o script seja chamado

echo ">> Compilando..."
rm -rf out
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")

echo ">> Executando modo: $*"
# Saída também é salva em resultados/log_<modo>.txt
mkdir -p resultados
caffeinate -i java -cp out br.uepb.eda.Main "$@" 2>&1 | tee "resultados/log_$1.txt"
