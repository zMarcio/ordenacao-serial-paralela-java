/**
 * ORDENADOR: o lugar onde ficam a versão SERIAL e a versão PARALELA.
 *
 * SERIAL   = uma thread só ordena o vetor inteiro.
 *
 * PARALELO = a mesma receita para os 4 algoritmos:
 *   1) corta o vetor em N pedaços (N = número de threads);
 *   2) cada thread ordena o SEU pedaço com o algoritmo serial;
 *   3) espera todas as threads terminarem (join);
 *   4) junta (intercala) os pedaços ordenados, de dois em dois, até sobrar um só.
 */
public class Ordenador {

    // ------------------------------------------------------------------
    // VERSÃO SERIAL
    // ------------------------------------------------------------------

    public static void ordenarSerial(String algoritmo, int[] vetor) {
        ordenarTrecho(algoritmo, vetor, 0, vetor.length);
    }

    /** Chama o algoritmo certo pelo nome. */
    public static void ordenarTrecho(String algoritmo, int[] vetor, int inicio, int fim) {
        if (algoritmo.equals("BubbleSort")) {
            BubbleSort.ordenar(vetor, inicio, fim);
        } else if (algoritmo.equals("InsertionSort")) {
            InsertionSort.ordenar(vetor, inicio, fim);
        } else if (algoritmo.equals("MergeSort")) {
            MergeSort.ordenar(vetor, inicio, fim);
        } else if (algoritmo.equals("QuickSort")) {
            QuickSort.ordenar(vetor, inicio, fim);
        } else {
            throw new IllegalArgumentException("Algoritmo desconhecido: " + algoritmo);
        }
    }

    // ------------------------------------------------------------------
    // VERSÃO PARALELA
    // ------------------------------------------------------------------

    /** O "trabalho" que cada thread faz: ordenar o seu pedaço. */
    static class TarefaDeOrdenacao implements Runnable {
        String algoritmo;
        int[] pedaco;

        TarefaDeOrdenacao(String algoritmo, int[] pedaco) {
            this.algoritmo = algoritmo;
            this.pedaco = pedaco;
        }

        public void run() {
            ordenarTrecho(algoritmo, pedaco, 0, pedaco.length);
        }
    }

    public static void ordenarParalelo(String algoritmo, int[] vetor, int quantidadeDeThreads) throws InterruptedException {
        int tamanho = vetor.length;

        // 1) corta o vetor em pedaços (o último pedaço pega o que sobrar)
        int[][] pedacos = new int[quantidadeDeThreads][];
        int tamanhoDoPedaco = tamanho / quantidadeDeThreads;
        for (int t = 0; t < quantidadeDeThreads; t++) {
            int inicio = t * tamanhoDoPedaco;
            int fim = (t == quantidadeDeThreads - 1) ? tamanho : inicio + tamanhoDoPedaco;
            pedacos[t] = java.util.Arrays.copyOfRange(vetor, inicio, fim);
        }

        // 2) cria e inicia uma thread para cada pedaço
        Thread[] threads = new Thread[quantidadeDeThreads];
        for (int t = 0; t < quantidadeDeThreads; t++) {
            threads[t] = new Thread(new TarefaDeOrdenacao(algoritmo, pedacos[t]));
            threads[t].start();
        }

        // 3) espera todas terminarem
        for (int t = 0; t < quantidadeDeThreads; t++) {
            threads[t].join();
        }

        // 4) junta os pedaços de dois em dois até sobrar um só
        int[][] restantes = pedacos;
        while (restantes.length > 1) {
            int quantosNovos = (restantes.length + 1) / 2;
            int[][] novos = new int[quantosNovos][];
            for (int k = 0; k < quantosNovos; k++) {
                int[] a = restantes[2 * k];
                if (2 * k + 1 < restantes.length) {
                    novos[k] = intercalar(a, restantes[2 * k + 1]);
                } else {
                    novos[k] = a; // sobrou um sozinho, passa direto para a próxima rodada
                }
            }
            restantes = novos;
        }

        // copia o resultado final de volta para o vetor original
        System.arraycopy(restantes[0], 0, vetor, 0, tamanho);
    }

    /** Junta dois vetores já ordenados em um novo vetor ordenado. */
    public static int[] intercalar(int[] a, int[] b) {
        int[] resultado = new int[a.length + b.length];
        int i = 0;
        int j = 0;
        int k = 0;

        while (i < a.length && j < b.length) {
            if (a[i] <= b[j]) {
                resultado[k] = a[i];
                i++;
            } else {
                resultado[k] = b[j];
                j++;
            }
            k++;
        }
        while (i < a.length) {
            resultado[k] = a[i];
            i++;
            k++;
        }
        while (j < b.length) {
            resultado[k] = b[j];
            j++;
            k++;
        }
        return resultado;
    }

    /** Confere se o vetor está em ordem crescente. */
    public static boolean estaOrdenado(int[] vetor) {
        for (int i = 1; i < vetor.length; i++) {
            if (vetor[i - 1] > vetor[i]) {
                return false;
            }
        }
        return true;
    }
}
