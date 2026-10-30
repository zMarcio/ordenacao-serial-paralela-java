import java.util.Random;

/**
 * GERADOR DE DADOS: cria os vetores de entrada dos testes.
 *
 * Três "naturezas" de dados:
 *   aleatorio  -> números sorteados (caso médio)
 *   ordenado   -> 0, 1, 2, 3, ...  (melhor caso para Bubble e Insertion)
 *   invertido  -> n, n-1, ..., 1, 0 (pior caso para Bubble e Insertion)
 */
public class GeradorDeDados {

    // semente fixa: todo mundo que rodar o programa recebe os MESMOS números
    private static final long SEMENTE = 42;

    public static int[] gerar(String tipo, int tamanho) {
        int[] vetor = new int[tamanho];

        if (tipo.equals("aleatorio")) {
            Random sorteio = new Random(SEMENTE);
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = sorteio.nextInt(1_000_000); // valores entre 0 e 999.999
            }
        } else if (tipo.equals("ordenado")) {
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = i;
            }
        } else if (tipo.equals("invertido")) {
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = tamanho - i;
            }
        } else {
            throw new IllegalArgumentException("Tipo de dado desconhecido: " + tipo);
        }

        return vetor;
    }
}
