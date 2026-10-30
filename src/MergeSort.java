/**
 * MERGE SORT (ordenação por intercalação)
 *
 * Ideia: "dividir para conquistar".
 * 1) Corta o vetor ao meio.
 * 2) Ordena cada metade (chamando o próprio Merge Sort).
 * 3) Junta (intercala) as duas metades já ordenadas.
 *
 * Custo: O(n log n) sempre.
 */
public class MergeSort {

    /** Ordena o vetor do índice "inicio" (incluso) até "fim" (NÃO incluso). */
    public static void ordenar(int[] vetor, int inicio, int fim) {
        // vetor auxiliar usado na hora de juntar as metades
        int[] auxiliar = new int[vetor.length];
        ordenarRecursivo(vetor, auxiliar, inicio, fim);
    }

    private static void ordenarRecursivo(int[] vetor, int[] auxiliar, int inicio, int fim) {
        int tamanho = fim - inicio;
        if (tamanho <= 1) {
            return; // 0 ou 1 elemento já está ordenado
        }

        int meio = inicio + tamanho / 2;

        ordenarRecursivo(vetor, auxiliar, inicio, meio); // metade da esquerda
        ordenarRecursivo(vetor, auxiliar, meio, fim);    // metade da direita

        juntar(vetor, auxiliar, inicio, meio, fim);
    }

    /** Junta vetor[inicio..meio) com vetor[meio..fim), as duas já ordenadas. */
    private static void juntar(int[] vetor, int[] auxiliar, int inicio, int meio, int fim) {
        int esquerda = inicio;
        int direita = meio;
        int posicao = inicio;

        // pega sempre o menor entre o primeiro da esquerda e o primeiro da direita
        while (esquerda < meio && direita < fim) {
            if (vetor[esquerda] <= vetor[direita]) {
                auxiliar[posicao] = vetor[esquerda];
                esquerda++;
            } else {
                auxiliar[posicao] = vetor[direita];
                direita++;
            }
            posicao++;
        }

        // copia o que sobrou de algum dos lados
        while (esquerda < meio) {
            auxiliar[posicao] = vetor[esquerda];
            esquerda++;
            posicao++;
        }
        while (direita < fim) {
            auxiliar[posicao] = vetor[direita];
            direita++;
            posicao++;
        }

        // devolve o resultado para o vetor original
        for (int i = inicio; i < fim; i++) {
            vetor[i] = auxiliar[i];
        }
    }
}
