# Ordenação Serial x Paralela em Java

Trabalho de Computação Concorrente e Paralela (UNIFOR – Ciência da Computação).

**Repositório:** https://github.com/zMarcio/ordenacao-serial-paralela-java

**Artigo:** [`Artigo-Ordenacao-Serial-Paralela.docx`](Artigo-Ordenacao-Serial-Paralela.docx)

## O que tem aqui

| Arquivo | O que faz |
|---|---|
| `src/BubbleSort.java` | Bubble Sort (serial) |
| `src/InsertionSort.java` | Insertion Sort (serial) |
| `src/MergeSort.java` | Merge Sort (serial) |
| `src/QuickSort.java` | Quick Sort (serial) |
| `src/Ordenador.java` | Versão **serial** e versão **paralela** (igual para os 4 algoritmos) |
| `src/GeradorDeDados.java` | Cria vetores `aleatorio`, `ordenado` e `invertido` |
| `src/Benchmark.java` | O "framework de teste": roda tudo 5x e grava CSV |
| `src/Estatistica.java` | Média, desvio padrão, CV, speedup e eficiência → `resumo.csv` |
| `src/Grafico.java` | Gráfico **dinâmico** em Swing (e exporta PNG) |
| `resultados/` | CSVs com todas as amostras e os resumos |
| `graficos/` | Figuras PNG geradas |
| `REGRAS.md` | Regras seguidas no código |

## Como a versão paralela funciona (a mesma ideia para os 4)

1. Corta o vetor em **N pedaços** (N = número de threads);
2. Cada `Thread` ordena **o seu pedaço** com o algoritmo serial;
3. `join()` espera todas terminarem;
4. Junta (intercala) os pedaços ordenados de dois em dois até sobrar um só.

## Como rodar

Requer apenas Java (JDK 17+; testado no JDK 25) e Linux (para o `taskset`).

```bash
./rodar_tudo.sh                 # compila, roda tudo, gera CSV e PNG (~15 min)
java -cp out Grafico            # abre o gráfico dinâmico
```

Ou passo a passo:

```bash
javac -encoding UTF-8 -d out src/*.java
java -cp out Benchmark completo                       # resultados/amostras.csv
taskset -c 0-1 java -cp out Benchmark nucleos         # limita a 2 núcleos
java -cp out Estatistica resultados/amostras.csv resultados/resumo.csv
java -cp out Grafico --png graficos
```

## Experimentos

- **Algoritmos:** Bubble, Insertion, Merge, Quick.
- **Versões:** serial e paralela com 2, 4, 8 e 16 threads.
- **Dados:** aleatório, ordenado e invertido.
- **Tamanhos:** 10 mil, 30 mil e 60 mil (Bubble/Insertion, O(n²)); 1, 5 e 10 milhões (Merge/Quick, O(n log n)).
- **Amostras:** 1 aquecimento descartado + 5 medições por combinação.
- **Núcleos:** 1, 2, 4 e 8 (via `taskset`), dados aleatórios, maior tamanho.

Máquina de teste: Intel Core i7-10510U (4 núcleos físicos / 8 lógicos), 16 GB RAM, Fedora Linux, OpenJDK 25.
