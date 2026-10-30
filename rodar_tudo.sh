#!/bin/bash
# Roda TUDO do zero: compila, faz os testes, calcula estatística e gera os gráficos.
# Uso: ./rodar_tudo.sh
set -e

echo "== 1) Compilando =="
javac -encoding UTF-8 -d out src/*.java

echo "== 2) Experimento principal (todas as threads, todos os núcleos) =="
rm -f resultados/amostras.csv resultados/amostras_nucleos.csv
java -Xmx3g -cp out Benchmark completo

echo "== 3) Experimento de núcleos (taskset limita quantos núcleos o Java pode usar) =="
for nucleos in 0 0-1 0-3 0-7; do
    taskset -c $nucleos java -Xmx3g -cp out Benchmark nucleos
done

echo "== 4) Estatística =="
java -cp out Estatistica resultados/amostras.csv resultados/resumo.csv
java -cp out Estatistica resultados/amostras_nucleos.csv resultados/resumo_nucleos.csv

echo "== 5) Gráficos PNG =="
java -Djava.awt.headless=true -cp out Grafico --png graficos

echo "Pronto! Para abrir o gráfico dinâmico: java -cp out Grafico"
