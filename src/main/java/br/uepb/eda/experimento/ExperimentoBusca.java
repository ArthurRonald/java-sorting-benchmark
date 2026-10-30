package br.uepb.eda.experimento;

import br.uepb.eda.busca.Busca;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executa o experimento de BUSCA:
 * - vetor previamente ordenado (cenário "ordenado" do gerador), um por tamanho;
 * - elemento procurado: -1 (ausente -> pior caso de todas as buscas);
 * - 25 execuções por combinação, 5 primeiras descartadas (warm-up).
 *
 * Detalhe de medição: uma busca binária leva poucas dezenas de nanossegundos,
 * menos que a resolução do relógio do Mac (~42 ns). Por isso, cada "execução"
 * mede um LOTE de buscas seguidas e divide o tempo pelo tamanho do lote.
 * O resultado é o tempo médio de UMA busca, em nanossegundos.
 *
 * Arquivos gerados: <nome>_brutos.csv, <nome>_resumo.csv e <nome>_tabela.csv
 * (esta última no formato da questão 13 da lista).
 */
public class ExperimentoBusca {

    public static final String[] TODAS = {
            "BuscaLinearIterativa", "BuscaLinearRecursiva", "BuscaLinearDuasPontas",
            "BuscaBinariaIterativa", "BuscaBinariaRecursiva"
    };

    /** Elemento procurado, exigido pelo enunciado: não pertence a [1, 100.000]. */
    public static final int ALVO = -1;

    private final String nome;
    private final int[] tamanhos;
    private final int execucoes;
    private final int warmup;
    private final int buscasPorLote;

    // Acumula os resultados para o JIT da JVM não "apagar" as buscas por achar que são inúteis
    private long sumidouro = 0;

    public ExperimentoBusca(String nome, int[] tamanhos, int execucoes, int warmup, int buscasPorLote) {
        this.nome = nome;
        this.tamanhos = tamanhos;
        this.execucoes = execucoes;
        this.warmup = warmup;
        this.buscasPorLote = buscasPorLote;
    }

    /** Chama a busca pelo nome. */
    public static int buscar(String algoritmo, int[] a, int x) {
        return switch (algoritmo) {
            case "BuscaLinearIterativa" -> Busca.linearIterativa(a, x);
            case "BuscaLinearRecursiva" -> Busca.linearRecursiva(a, x);
            case "BuscaLinearDuasPontas" -> Busca.linearDuasPontas(a, x);
            case "BuscaBinariaIterativa" -> Busca.binariaIterativa(a, x);
            case "BuscaBinariaRecursiva" -> Busca.binariaRecursiva(a, x);
            default -> throw new IllegalArgumentException("Busca desconhecida: " + algoritmo);
        };
    }

    public void executar(Path pastaResultados) throws IOException {
        long inicioGeral = System.nanoTime();
        Map<String, Double> medias = new LinkedHashMap<>();

        System.out.printf("%n=== Experimento '%s': tamanhos %s, %d execuções (%d de warm-up), "
                        + "%d buscas por execução, alvo = %d ===%n",
                nome, Arrays.toString(tamanhos), execucoes, warmup, buscasPorLote, ALVO);

        try (EscritorCsv brutos = new EscritorCsv(pastaResultados.resolve(nome + "_brutos.csv"),
                "algoritmo", "tamanho", "execucao", "warmup", "buscas_no_lote", "tempo_lote_ns", "tempo_por_busca_ns");
             EscritorCsv resumo = new EscritorCsv(pastaResultados.resolve(nome + "_resumo.csv"),
                     "algoritmo", "tamanho", "execucoes_validas", "media_ns", "desvio_padrao_ns",
                     "mediana_ns", "minimo_ns", "maximo_ns")) {

            for (int n : tamanhos) {
                int[] vetor = GeradorDeVetores.gerar(GeradorDeVetores.ORDENADO, n); // previamente ordenado

                for (String algoritmo : TODAS) {
                    double[] temposValidos = new double[execucoes - warmup];

                    for (int exec = 1; exec <= execucoes; exec++) {
                        long somaResultados = 0;

                        long inicio = System.nanoTime();
                        for (int r = 0; r < buscasPorLote; r++) {
                            somaResultados += buscar(algoritmo, vetor, ALVO);
                        }
                        long fim = System.nanoTime();

                        // verificação fora da medição: todas as buscas devem ter retornado -1
                        if (somaResultados != -1L * buscasPorLote) {
                            throw new IllegalStateException("ERRO: " + algoritmo + " encontrou o -1, que não existe!");
                        }
                        sumidouro += somaResultados;

                        long tempoLote = fim - inicio;
                        double porBusca = (double) tempoLote / buscasPorLote;
                        boolean ehWarmup = exec <= warmup;
                        brutos.linha(algoritmo, n, exec, ehWarmup ? "sim" : "nao",
                                buscasPorLote, tempoLote, porBusca);
                        if (!ehWarmup) {
                            temposValidos[exec - warmup - 1] = porBusca;
                        }
                    }

                    Estatisticas e = new Estatisticas(temposValidos);
                    resumo.linha(algoritmo, n, e.getQuantidade(), e.getMedia(), e.getDesvioPadrao(),
                            e.getMediana(), e.getMinimo(), e.getMaximo());
                    medias.put(algoritmo + "|" + n, e.getMedia());
                    System.out.printf("%-23s n=%-7d média = %12.3f ns por busca (dp %.3f)%n",
                            algoritmo, n, e.getMedia(), e.getDesvioPadrao());
                }
            }
        }

        escreverTabela(pastaResultados, medias);
        System.out.printf("=== '%s' concluído em %.1f s (controle: %d) ===%n",
                nome, (System.nanoTime() - inicioGeral) / 1e9, sumidouro);
    }

    /** Tabela no formato da questão 13: uma linha por tamanho, uma coluna por algoritmo. */
    private void escreverTabela(Path pasta, Map<String, Double> medias) throws IOException {
        String[] cabecalho = new String[TODAS.length + 1];
        cabecalho[0] = "tamanho";
        for (int i = 0; i < TODAS.length; i++) {
            cabecalho[i + 1] = TODAS[i] + "_ns";
        }
        try (EscritorCsv tabela = new EscritorCsv(pasta.resolve(nome + "_tabela.csv"), cabecalho)) {
            for (int n : tamanhos) {
                Object[] linha = new Object[TODAS.length + 1];
                linha[0] = n;
                for (int i = 0; i < TODAS.length; i++) {
                    linha[i + 1] = medias.get(TODAS[i] + "|" + n);
                }
                tabela.linha(linha);
            }
        }
    }
}
