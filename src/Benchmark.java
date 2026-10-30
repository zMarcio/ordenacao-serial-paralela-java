import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * BENCHMARK: o "framework de teste".
 *
 * Roda cada algoritmo, em cada versão (serial e paralela com várias threads),
 * para cada tipo de dado e cada tamanho, 5 vezes, e grava o tempo de cada
 * execução em um arquivo CSV.
 *
 * Como rodar:
 *   java -cp out Benchmark completo   -> experimento principal (grava resultados/amostras.csv)
 *   java -cp out Benchmark nucleos    -> experimento de núcleos (grava resultados/amostras_nucleos.csv)
 *
 * No experimento de núcleos limitamos os núcleos da máquina por fora, com o comando
 * "taskset" do Linux (ex.: taskset -c 0-1 java ... = só os núcleos 0 e 1).
 */
public class Benchmark {

    static final int AMOSTRAS = 5;

    static final String[] ALGORITMOS = {"BubbleSort", "InsertionSort", "MergeSort", "QuickSort"};
    static final String[] TIPOS_DE_DADOS = {"aleatorio", "ordenado", "invertido"};
    static final int[] THREADS_PARALELAS = {2, 4, 8, 16};

    // Bubble e Insertion são O(n²): com vetores grandes demorariam horas,
    // por isso usam tamanhos menores que Merge e Quick (que são O(n log n)).
    static final int[] TAMANHOS_LENTOS = {10_000, 30_000, 60_000};
    static final int[] TAMANHOS_RAPIDOS = {1_000_000, 5_000_000, 10_000_000};

    public static void main(String[] args) throws Exception {
        String experimento = "completo";
        if (args.length > 0) {
            experimento = args[0];
        }

        // quantos núcleos o Java está enxergando (muda quando usamos taskset)
        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("Experimento: " + experimento + " | núcleos disponíveis: " + nucleos);

        String arquivo;
        if (experimento.equals("nucleos")) {
            arquivo = "resultados/amostras_nucleos.csv";
        } else {
            arquivo = "resultados/amostras.csv";
        }

        // se o arquivo ainda não existe, escreve o cabeçalho
        boolean arquivoNovo = !new java.io.File(arquivo).exists();
        PrintWriter csv = new PrintWriter(new FileWriter(arquivo, true)); // true = adiciona no final
        if (arquivoNovo) {
            csv.println("nucleos,algoritmo,versao,threads,tipo_dados,tamanho,amostra,tempo_ms");
        }

        for (String algoritmo : ALGORITMOS) {

            int[] tamanhos;
            if (algoritmo.equals("BubbleSort") || algoritmo.equals("InsertionSort")) {
                tamanhos = TAMANHOS_LENTOS;
            } else {
                tamanhos = TAMANHOS_RAPIDOS;
            }

            String[] tipos = TIPOS_DE_DADOS;
            if (experimento.equals("nucleos")) {
                // no experimento de núcleos usamos só o caso mais interessante:
                // dados aleatórios e o maior tamanho
                tipos = new String[] {"aleatorio"};
                tamanhos = new int[] {tamanhos[tamanhos.length - 1]};
            }

            for (String tipo : tipos) {
                for (int tamanho : tamanhos) {
                    int[] original = GeradorDeDados.gerar(tipo, tamanho);

                    // versão serial (threads = 1)
                    medir(csv, nucleos, algoritmo, "serial", 1, tipo, original);

                    // versões paralelas
                    for (int threads : THREADS_PARALELAS) {
                        medir(csv, nucleos, algoritmo, "paralelo", threads, tipo, original);
                    }
                }
            }
        }

        csv.close();
        System.out.println("Pronto! Resultados em " + arquivo);
    }

    /** Roda 1 aquecimento (descartado) + AMOSTRAS medições e grava cada uma no CSV. */
    static void medir(PrintWriter csv, int nucleos, String algoritmo, String versao, int threads,
                      String tipo, int[] original) throws Exception {

        // aquecimento: deixa o Java "esquentar" (compilar o código) antes de medir de verdade
        executarUmaVez(algoritmo, versao, threads, original);

        for (int amostra = 1; amostra <= AMOSTRAS; amostra++) {
            double tempoMs = executarUmaVez(algoritmo, versao, threads, original);

            csv.println(nucleos + "," + algoritmo + "," + versao + "," + threads + ","
                    + tipo + "," + original.length + "," + amostra + ","
                    + String.format(java.util.Locale.US, "%.3f", tempoMs));
            csv.flush();

            System.out.printf("%-14s %-9s threads=%-2d %-10s n=%-9d amostra=%d -> %10.2f ms%n",
                    algoritmo, versao, threads, tipo, original.length, amostra, tempoMs);
        }
    }

    /** Ordena UMA cópia do vetor e devolve quanto tempo levou, em milissegundos. */
    static double executarUmaVez(String algoritmo, String versao, int threads, int[] original) throws Exception {
        int[] copia = original.clone(); // nunca ordena o original, senão a 2ª vez já estaria ordenado

        long inicio = System.nanoTime();
        if (versao.equals("serial")) {
            Ordenador.ordenarSerial(algoritmo, copia);
        } else {
            Ordenador.ordenarParalelo(algoritmo, copia, threads);
        }
        long fim = System.nanoTime();

        if (!Ordenador.estaOrdenado(copia)) {
            throw new IllegalStateException("ERRO: " + algoritmo + " " + versao + " não ordenou!");
        }

        return (fim - inicio) / 1_000_000.0;
    }
}
