/**
 * BUBBLE SORT (ordenação por bolha)
 *
 * Ideia: passa pelo vetor várias vezes comparando vizinhos.
 * Se o da esquerda for maior que o da direita, troca os dois.
 * A cada passada o maior número "sobe" (como bolha) para o final.
 *
 * Custo: O(n²) no caso médio e no pior caso. O(n) se já estiver ordenado.
 */
public class BubbleSort {

    /** Ordena o vetor do índice "inicio" (incluso) até "fim" (NÃO incluso). */
    public static void ordenar(int[] vetor, int inicio, int fim) {
        for (int ultimo = fim - 1; ultimo > inicio; ultimo--) {

            boolean fezAlgumaTroca = false;

            for (int i = inicio; i < ultimo; i++) {
                if (vetor[i] > vetor[i + 1]) {
                    // troca vetor[i] com vetor[i + 1]
                    int guardado = vetor[i];
                    vetor[i] = vetor[i + 1];
                    vetor[i + 1] = guardado;
                    fezAlgumaTroca = true;
                }
            }

            // se passou o vetor inteiro sem trocar nada, já está ordenado
            if (!fezAlgumaTroca) {
                return;
            }
        }
    }
}
