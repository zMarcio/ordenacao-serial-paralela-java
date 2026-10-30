/**
 * INSERTION SORT (ordenação por inserção)
 *
 * Ideia: igual organizar cartas na mão.
 * Pega um número por vez e "empurra" ele para a esquerda
 * até achar o lugar certo dele entre os que já estão ordenados.
 *
 * Custo: O(n²) no caso médio e no pior caso. O(n) se já estiver ordenado.
 */
public class InsertionSort {

    /** Ordena o vetor do índice "inicio" (incluso) até "fim" (NÃO incluso). */
    public static void ordenar(int[] vetor, int inicio, int fim) {
        for (int i = inicio + 1; i < fim; i++) {

            int carta = vetor[i];   // número que vamos encaixar
            int j = i - 1;

            // anda para a esquerda empurrando os maiores uma casa para a direita
            while (j >= inicio && vetor[j] > carta) {
                vetor[j + 1] = vetor[j];
                j--;
            }

            // achou o lugar: coloca a carta ali
            vetor[j + 1] = carta;
        }
    }
}
