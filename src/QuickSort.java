/**
 * QUICK SORT (ordenação rápida)
 *
 * Ideia: escolhe um número "pivô" (aqui: o do meio).
 * Joga todo mundo menor que o pivô para a esquerda e todo mundo maior para a direita.
 * Depois faz a mesma coisa com o lado esquerdo e com o lado direito.
 *
 * Custo: O(n log n) no caso médio. O(n²) no pior caso (raro com pivô do meio).
 */
public class QuickSort {

    /** Ordena o vetor do índice "inicio" (incluso) até "fim" (NÃO incluso). */
    public static void ordenar(int[] vetor, int inicio, int fim) {
        ordenarRecursivo(vetor, inicio, fim - 1); // aqui usamos "fim" incluso, por isso o -1
    }

    private static void ordenarRecursivo(int[] vetor, int esquerda, int direita) {
        if (esquerda >= direita) {
            return; // 0 ou 1 elemento já está ordenado
        }

        int pivo = vetor[esquerda + (direita - esquerda) / 2];
        int i = esquerda;
        int j = direita;

        // separa: menores que o pivô à esquerda, maiores à direita
        while (i <= j) {
            while (vetor[i] < pivo) {
                i++;
            }
            while (vetor[j] > pivo) {
                j--;
            }
            if (i <= j) {
                int guardado = vetor[i];
                vetor[i] = vetor[j];
                vetor[j] = guardado;
                i++;
                j--;
            }
        }

        // ordena os dois lados
        ordenarRecursivo(vetor, esquerda, j);
        ordenarRecursivo(vetor, i, direita);
    }
}
