import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * GRÁFICO DINÂMICO EM SWING (ponto extra).
 *
 * Lê o CSV de amostras e desenha um gráfico de linhas:
 *   eixo X = versão (Serial, 2, 4, 8, 16 threads)
 *   eixo Y = tempo médio, speedup ou eficiência
 *   uma linha para cada tamanho de vetor (ou para cada nº de núcleos, no experimento de núcleos)
 *
 * É "dinâmico" porque:
 *   - os menus trocam algoritmo / tipo de dado / métrica na hora;
 *   - a cada 2 segundos o CSV é relido, então dá para ver o gráfico crescer
 *     ENQUANTO o Benchmark está rodando.
 *
 * Como rodar:
 *   java -cp out Grafico                 -> abre a janela
 *   java -cp out Grafico --png graficos  -> só salva as figuras PNG usadas no relatório
 */
public class Grafico extends JPanel {

    static final String[] ARQUIVOS = {"resultados/amostras.csv", "resultados/amostras_nucleos.csv"};
    static final String[] ALGORITMOS = {"BubbleSort", "InsertionSort", "MergeSort", "QuickSort"};
    static final String[] TIPOS = {"aleatorio", "ordenado", "invertido"};
    static final String[] METRICAS = {"tempo medio (ms)", "speedup", "eficiencia"};
    static final int[] EIXO_X_THREADS = {1, 2, 4, 8, 16}; // 1 = serial

    // cores fáceis de distinguir (inclusive para daltônicos)
    static final Color[] CORES = {
        new Color(0, 114, 178), new Color(230, 159, 0), new Color(0, 158, 115),
        new Color(204, 121, 167), new Color(213, 94, 0)
    };

    // o que está selecionado agora
    String arquivo = ARQUIVOS[0];
    String algoritmo = ALGORITMOS[0];
    String tipo = TIPOS[0];
    String metrica = METRICAS[1];

    // chave "serie,threads" -> lista de tempos lidos do CSV
    LinkedHashMap<String, ArrayList<Double>> tempos = new LinkedHashMap<>();
    TreeSet<Integer> series = new TreeSet<>(); // tamanhos (ou núcleos) encontrados

    // ------------------------------------------------------------------
    // LEITURA DO CSV
    // ------------------------------------------------------------------

    /** Relê o CSV e guarda só as linhas do algoritmo e tipo escolhidos. */
    void recarregar() {
        tempos.clear();
        series.clear();
        if (!new File(arquivo).exists()) {
            return;
        }
        boolean experimentoDeNucleos = arquivo.contains("nucleos");
        try {
            BufferedReader leitor = new BufferedReader(new FileReader(arquivo));
            leitor.readLine(); // cabeçalho
            String linha;
            while ((linha = leitor.readLine()) != null) {
                // colunas: nucleos,algoritmo,versao,threads,tipo_dados,tamanho,amostra,tempo_ms
                String[] c = linha.split(",");
                if (c.length < 8 || !c[1].equals(algoritmo) || !c[4].equals(tipo)) {
                    continue;
                }
                // no experimento de núcleos cada linha do gráfico é um nº de núcleos;
                // no principal, cada linha é um tamanho de vetor
                int serie = experimentoDeNucleos ? Integer.parseInt(c[0]) : Integer.parseInt(c[5]);
                series.add(serie);

                String chave = serie + "," + c[3];
                if (!tempos.containsKey(chave)) {
                    tempos.put(chave, new ArrayList<Double>());
                }
                tempos.get(chave).add(Double.parseDouble(c[7]));
            }
            leitor.close();
        } catch (Exception e) {
            System.out.println("Não consegui ler " + arquivo + ": " + e.getMessage());
        }
    }

    /** Valor do ponto (serie, threads) na métrica escolhida. NaN = ainda não tem dado. */
    double valor(int serie, int threads) {
        ArrayList<Double> lista = tempos.get(serie + "," + threads);
        ArrayList<Double> listaSerial = tempos.get(serie + ",1");
        if (lista == null || lista.isEmpty()) {
            return Double.NaN;
        }
        double media = Estatistica.media(lista);
        if (metrica.startsWith("tempo")) {
            return media;
        }
        if (listaSerial == null || listaSerial.isEmpty()) {
            return Double.NaN;
        }
        double speedup = Estatistica.media(listaSerial) / media;
        if (metrica.equals("speedup")) {
            return speedup;
        }
        return speedup / threads; // eficiência
    }

    // ------------------------------------------------------------------
    // DESENHO
    // ------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        desenhar((Graphics2D) g, getWidth(), getHeight());
    }

    void desenhar(Graphics2D g, int largura, int altura) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, largura, altura);

        // margens da área do gráfico
        int esquerda = 80, direita = 170, topo = 50, baixo = 60;
        int larguraUtil = largura - esquerda - direita;
        int alturaUtil = altura - topo - baixo;

        // título
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        String nomeExperimento = arquivo.contains("nucleos") ? " | variando núcleos" : "";
        g.drawString(algoritmo + " | dados " + tipo + " | " + metrica + nomeExperimento, esquerda, 30);

        // maior valor para saber a escala do eixo Y
        double maiorValor = 0;
        for (int serie : series) {
            for (int t : EIXO_X_THREADS) {
                double v = valor(serie, t);
                if (!Double.isNaN(v) && v > maiorValor) maiorValor = v;
            }
        }
        if (metrica.equals("speedup") && maiorValor < 8) maiorValor = 8;      // para caber a linha ideal
        if (metrica.equals("eficiencia") && maiorValor < 1) maiorValor = 1;
        if (maiorValor == 0) maiorValor = 1;
        maiorValor = maiorValor * 1.1;

        // grade e números do eixo Y
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int i = 0; i <= 5; i++) {
            double v = maiorValor * i / 5;
            int y = topo + alturaUtil - (int) (alturaUtil * i / 5.0);
            g.setColor(new Color(225, 225, 225));
            g.drawLine(esquerda, y, esquerda + larguraUtil, y);
            g.setColor(Color.DARK_GRAY);
            g.drawString(String.format("%.2f", v), 10, y + 4);
        }

        // números do eixo X
        for (int i = 0; i < EIXO_X_THREADS.length; i++) {
            int x = posicaoX(i, esquerda, larguraUtil);
            String texto = (EIXO_X_THREADS[i] == 1) ? "Serial" : EIXO_X_THREADS[i] + " threads";
            g.drawString(texto, x - 25, topo + alturaUtil + 20);
        }
        g.drawString("versão", esquerda + larguraUtil / 2 - 20, altura - 15);

        // eixos
        g.setColor(Color.BLACK);
        g.drawLine(esquerda, topo, esquerda, topo + alturaUtil);
        g.drawLine(esquerda, topo + alturaUtil, esquerda + larguraUtil, topo + alturaUtil);

        // linha ideal do speedup (speedup = nº de threads)
        if (metrica.equals("speedup")) {
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[] {6, 6}, 0));
            for (int i = 0; i < EIXO_X_THREADS.length - 1; i++) {
                double v1 = Math.min(EIXO_X_THREADS[i], maiorValor);
                double v2 = Math.min(EIXO_X_THREADS[i + 1], maiorValor);
                g.drawLine(posicaoX(i, esquerda, larguraUtil), posicaoY(v1, maiorValor, topo, alturaUtil),
                           posicaoX(i + 1, esquerda, larguraUtil), posicaoY(v2, maiorValor, topo, alturaUtil));
            }
        }

        // uma linha colorida para cada série
        int numeroDaSerie = 0;
        for (int serie : series) {
            Color cor = CORES[numeroDaSerie % CORES.length];
            g.setColor(cor);
            g.setStroke(new BasicStroke(2.5f));

            int xAnterior = -1, yAnterior = -1;
            for (int i = 0; i < EIXO_X_THREADS.length; i++) {
                double v = valor(serie, EIXO_X_THREADS[i]);
                if (Double.isNaN(v)) continue;
                int x = posicaoX(i, esquerda, larguraUtil);
                int y = posicaoY(v, maiorValor, topo, alturaUtil);
                g.fillOval(x - 4, y - 4, 8, 8);
                if (xAnterior >= 0) g.drawLine(xAnterior, yAnterior, x, y);
                xAnterior = x;
                yAnterior = y;
            }

            // legenda
            int yLegenda = topo + 10 + numeroDaSerie * 22;
            g.fillRect(largura - direita + 15, yLegenda - 9, 14, 10);
            g.setColor(Color.BLACK);
            String rotulo = arquivo.contains("nucleos") ? serie + " núcleo(s)" : "n = " + String.format("%,d", serie);
            g.drawString(rotulo, largura - direita + 35, yLegenda);
            numeroDaSerie++;
        }
        if (metrica.equals("speedup")) {
            int yLegenda = topo + 10 + numeroDaSerie * 22;
            g.setColor(Color.GRAY);
            g.drawLine(largura - direita + 15, yLegenda - 4, largura - direita + 29, yLegenda - 4);
            g.setColor(Color.BLACK);
            g.drawString("ideal", largura - direita + 35, yLegenda);
        }
    }

    int posicaoX(int indice, int esquerda, int larguraUtil) {
        return esquerda + 30 + (larguraUtil - 60) * indice / (EIXO_X_THREADS.length - 1);
    }

    int posicaoY(double valor, double maiorValor, int topo, int alturaUtil) {
        return topo + alturaUtil - (int) (alturaUtil * valor / maiorValor);
    }

    /** Desenha o gráfico atual numa imagem e salva como PNG. */
    void salvarPng(String caminho) throws Exception {
        BufferedImage imagem = new BufferedImage(900, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagem.createGraphics();
        desenhar(g, 900, 500);
        g.dispose();
        ImageIO.write(imagem, "png", new File(caminho));
        System.out.println("Salvo: " + caminho);
    }

    // ------------------------------------------------------------------
    // PROGRAMA PRINCIPAL
    // ------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--png")) {
            salvarFigurasDoRelatorio(args.length > 1 ? args[1] : "graficos");
            return;
        }
        abrirJanela();
    }

    /** Gera todas as figuras em PNG (sem abrir janela). */
    static void salvarFigurasDoRelatorio(String pasta) throws Exception {
        new File(pasta).mkdirs();
        Grafico grafico = new Grafico();
        for (String alg : ALGORITMOS) {
            grafico.algoritmo = alg;

            // experimento principal: todas as métricas e tipos de dados
            grafico.arquivo = ARQUIVOS[0];
            for (String tipo : TIPOS) {
                grafico.tipo = tipo;
                grafico.recarregar();
                grafico.metrica = "tempo medio (ms)";
                grafico.salvarPng(pasta + "/" + alg + "_" + tipo + "_tempo.png");
                grafico.metrica = "speedup";
                grafico.salvarPng(pasta + "/" + alg + "_" + tipo + "_speedup.png");
                grafico.metrica = "eficiencia";
                grafico.salvarPng(pasta + "/" + alg + "_" + tipo + "_eficiencia.png");
            }

            // experimento de núcleos: só dados aleatórios
            grafico.arquivo = ARQUIVOS[1];
            grafico.tipo = "aleatorio";
            grafico.recarregar();
            grafico.metrica = "tempo medio (ms)";
            grafico.salvarPng(pasta + "/" + alg + "_nucleos_tempo.png");
            grafico.metrica = "speedup";
            grafico.salvarPng(pasta + "/" + alg + "_nucleos_speedup.png");
        }
    }

    static void abrirJanela() {
        Grafico grafico = new Grafico();
        grafico.recarregar();

        JComboBox<String> menuArquivo = new JComboBox<>(ARQUIVOS);
        JComboBox<String> menuAlgoritmo = new JComboBox<>(ALGORITMOS);
        JComboBox<String> menuTipo = new JComboBox<>(TIPOS);
        JComboBox<String> menuMetrica = new JComboBox<>(METRICAS);
        menuMetrica.setSelectedItem(grafico.metrica);
        JButton botaoSalvar = new JButton("Salvar PNG");

        // quando qualquer menu muda: atualiza a seleção, relê o CSV e redesenha
        java.awt.event.ActionListener aoMudar = new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                grafico.arquivo = (String) menuArquivo.getSelectedItem();
                grafico.algoritmo = (String) menuAlgoritmo.getSelectedItem();
                grafico.tipo = (String) menuTipo.getSelectedItem();
                grafico.metrica = (String) menuMetrica.getSelectedItem();
                grafico.recarregar();
                grafico.repaint();
            }
        };
        menuArquivo.addActionListener(aoMudar);
        menuAlgoritmo.addActionListener(aoMudar);
        menuTipo.addActionListener(aoMudar);
        menuMetrica.addActionListener(aoMudar);

        botaoSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                try {
                    new File("graficos").mkdirs();
                    grafico.salvarPng("graficos/tela_" + System.currentTimeMillis() + ".png");
                } catch (Exception erro) {
                    System.out.println("Erro ao salvar: " + erro.getMessage());
                }
            }
        });

        // a cada 2 segundos relê o CSV (gráfico "ao vivo" enquanto o Benchmark roda)
        Timer relogio = new Timer(2000, aoMudar);
        relogio.start();

        JPanel barraDeCima = new JPanel();
        barraDeCima.add(new JLabel("Arquivo:"));
        barraDeCima.add(menuArquivo);
        barraDeCima.add(new JLabel("Algoritmo:"));
        barraDeCima.add(menuAlgoritmo);
        barraDeCima.add(new JLabel("Dados:"));
        barraDeCima.add(menuTipo);
        barraDeCima.add(new JLabel("Métrica:"));
        barraDeCima.add(menuMetrica);
        barraDeCima.add(botaoSalvar);

        JFrame janela = new JFrame("Análise de desempenho - ordenação serial x paralela");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setLayout(new BorderLayout());
        janela.add(barraDeCima, BorderLayout.NORTH);
        janela.add(grafico, BorderLayout.CENTER);
        janela.setSize(1100, 650);
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }
}
