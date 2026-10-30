import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * ESTATÍSTICA: lê o CSV com todas as amostras e gera um CSV de resumo.
 *
 * Para cada combinação (núcleos, algoritmo, versão, threads, tipo de dado, tamanho) calcula:
 *   media_ms       -> média dos tempos
 *   desvio_ms      -> desvio padrão (quanto os tempos variam)
 *   min_ms/max_ms  -> menor e maior tempo
 *   cv_pct         -> coeficiente de variação = desvio / média (em %)
 *   speedup        -> tempo médio SERIAL / tempo médio desta versão (quantas vezes ficou mais rápido)
 *   eficiencia     -> speedup / threads (1,0 = aproveitou 100% das threads)
 *
 * Como rodar:
 *   java -cp out Estatistica resultados/amostras.csv resultados/resumo.csv
 */
public class Estatistica {

    public static void main(String[] args) throws Exception {
        String entrada = args[0];
        String saida = args[1];

        // chave = "nucleos,algoritmo,versao,threads,tipo,tamanho"  ->  lista de tempos
        LinkedHashMap<String, ArrayList<Double>> tempos = new LinkedHashMap<>();

        BufferedReader leitor = new BufferedReader(new FileReader(entrada));
        leitor.readLine(); // pula o cabeçalho
        String linha;
        while ((linha = leitor.readLine()) != null) {
            String[] c = linha.split(",");
            String chave = c[0] + "," + c[1] + "," + c[2] + "," + c[3] + "," + c[4] + "," + c[5];
            double tempo = Double.parseDouble(c[7]);

            if (!tempos.containsKey(chave)) {
                tempos.put(chave, new ArrayList<Double>());
            }
            tempos.get(chave).add(tempo);
        }
        leitor.close();

        PrintWriter csv = new PrintWriter(saida);
        csv.println("nucleos,algoritmo,versao,threads,tipo_dados,tamanho,amostras,media_ms,desvio_ms,min_ms,max_ms,cv_pct,speedup,eficiencia");

        for (String chave : tempos.keySet()) {
            ArrayList<Double> lista = tempos.get(chave);
            String[] c = chave.split(",");
            int threads = Integer.parseInt(c[3]);

            double media = media(lista);
            double desvio = desvioPadrao(lista, media);

            // acha a média da versão serial da mesma combinação para calcular o speedup
            String chaveSerial = c[0] + "," + c[1] + ",serial,1," + c[4] + "," + c[5];
            double mediaSerial = media(tempos.get(chaveSerial));
            double speedup = mediaSerial / media;
            double eficiencia = speedup / threads;

            csv.println(chave + "," + lista.size() + ","
                    + f(media) + "," + f(desvio) + "," + f(minimo(lista)) + "," + f(maximo(lista)) + ","
                    + f(100 * desvio / media) + "," + f(speedup) + "," + f(eficiencia));
        }
        csv.close();
        System.out.println("Resumo gravado em " + saida);
    }

    static double media(ArrayList<Double> lista) {
        double soma = 0;
        for (double x : lista) {
            soma += x;
        }
        return soma / lista.size();
    }

    /** Desvio padrão amostral (divide por n - 1). */
    static double desvioPadrao(ArrayList<Double> lista, double media) {
        double soma = 0;
        for (double x : lista) {
            soma += (x - media) * (x - media);
        }
        return Math.sqrt(soma / (lista.size() - 1));
    }

    static double minimo(ArrayList<Double> lista) {
        double menor = lista.get(0);
        for (double x : lista) {
            if (x < menor) menor = x;
        }
        return menor;
    }

    static double maximo(ArrayList<Double> lista) {
        double maior = lista.get(0);
        for (double x : lista) {
            if (x > maior) maior = x;
        }
        return maior;
    }

    /** Formata número com 3 casas e ponto decimal (padrão de CSV). */
    static String f(double x) {
        return String.format(Locale.US, "%.3f", x);
    }
}
