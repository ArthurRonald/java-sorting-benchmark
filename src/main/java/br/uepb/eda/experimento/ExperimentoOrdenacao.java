package br.uepb.eda.experimento;

import br.uepb.eda.ordenacao.Ordenacao;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Executa o experimento de ORDENAÇÃO seguindo as regras do enunciado:
 * - para cada (tamanho, cenário) é gerado UM vetor original;
 * - todos os algoritmos recebem CÓPIAS dessa mesma entrada original;
 * - cada combinação roda 25 vezes, e as 5 primeiras (warm-up) são descartadas;
 * - o tempo é medido com System.nanoTime() só em volta da chamada do algoritmo;
 * - após cada execução o resultado é verificado (fora da medição).
 *
 * Gera três arquivos CSV na pasta de resultados:
 *   <nome>_brutos.csv  -> todas as execuções, uma por linha (inclusive warm-ups, marcados)
 *   <nome>_resumo.csv  -> média, desvio padrão, mediana, mínimo e máximo (sem warm-ups)
 *   <nome>_tabela.csv  -> tabela no formato da questão 16 da lista (médias em ms)
 */
public class ExperimentoOrdenacao {

    /** Todos os algoritmos de ordenação do projeto (nomes usados nos CSVs). */
    public static final String[] TODOS = {
            "BubbleSort", "BubbleSortOtimizado",
            "SelectionSort", "SelectionSortEstavel",
            "InsertionSort",
            "MergeSort",
            "QuickSort", "QuickSortShuffle",
            "CountingSort",
            "ArraysSort"
    };

    /** Somente os algoritmos rápidos (O(n log n) ou lineares), para vetores grandes. */
    public static final String[] RAPIDOS = {
            "MergeSort", "QuickSort", "QuickSortShuffle", "CountingSort", "ArraysSort"
    };

    private static final double NANOS_POR_MS = 1_000_000.0;

    private final String nome;
    private final int[] tamanhos;
    private final String[] algoritmos;
    private final int execucoes;
    private final int warmup;
    private final double limiteSegundos;

    /**
     * @param nome           prefixo dos arquivos CSV (ex.: "ordenacao")
     * @param tamanhos       tamanhos dos vetores (do menor para o maior)
     * @param algoritmos     algoritmos a comparar
     * @param execucoes      total de execuções por combinação (enunciado: no mínimo 25)
     * @param warmup         execuções iniciais descartadas (enunciado: 5)
     * @param limiteSegundos se UMA execução passar desse tempo, a combinação é interrompida
     *                       (e o algoritmo não é testado nos tamanhos maiores daquele cenário)
     */
    public ExperimentoOrdenacao(String nome, int[] tamanhos, String[] algoritmos,
                                int execucoes, int warmup, double limiteSegundos) {
        this.nome = nome;
        this.tamanhos = tamanhos;
        this.algoritmos = algoritmos;
        this.execucoes = execucoes;
        this.warmup = warmup;
        this.limiteSegundos = limiteSegundos;
    }

    /** Chama o algoritmo pelo nome. */
    public static void ordenar(String algoritmo, int[] a) {
        switch (algoritmo) {
            case "BubbleSort" -> Ordenacao.bubbleSort(a);
            case "BubbleSortOtimizado" -> Ordenacao.bubbleSortOtimizado(a);
            case "SelectionSort" -> Ordenacao.selectionSort(a);
            case "SelectionSortEstavel" -> Ordenacao.selectionSortEstavel(a);
            case "InsertionSort" -> Ordenacao.insertionSort(a);
            case "MergeSort" -> Ordenacao.mergeSort(a);
            case "QuickSort" -> Ordenacao.quickSort(a);
            case "QuickSortShuffle" -> Ordenacao.quickSortComShuffle(a);
            case "CountingSort" -> Ordenacao.countingSort(a);
            case "ArraysSort" -> Arrays.sort(a); // referência da biblioteca padrão (Dual-Pivot QuickSort)
            default -> throw new IllegalArgumentException("Algoritmo desconhecido: " + algoritmo);
        }
    }

    public void executar(Path pastaResultados) throws IOException {
        String[] cenarios = GeradorDeVetores.CENARIOS;
        int totalCombinacoes = tamanhos.length * cenarios.length * algoritmos.length;
        int combinacaoAtual = 0;
        long inicioGeral = System.nanoTime();

        // guarda a média (ou o status) de cada combinação, para montar a tabela no final
        Map<String, String> celulasTabela = new LinkedHashMap<>();
        // combinações (algoritmo|cenário) que estouraram o tempo limite
        Set<String> interrompidos = new HashSet<>();

        System.out.printf("%n=== Experimento '%s': tamanhos %s, %d execuções (%d de warm-up) ===%n",
                nome, Arrays.toString(tamanhos), execucoes, warmup);

        try (EscritorCsv brutos = new EscritorCsv(pastaResultados.resolve(nome + "_brutos.csv"),
                "algoritmo", "cenario", "tamanho", "execucao", "warmup", "tempo_ns", "tempo_ms");
             EscritorCsv resumo = new EscritorCsv(pastaResultados.resolve(nome + "_resumo.csv"),
                     "algoritmo", "cenario", "tamanho", "execucoes_validas",
                     "media_ms", "desvio_padrao_ms", "mediana_ms", "minimo_ms", "maximo_ms", "status")) {

            for (int n : tamanhos) {
                for (String cenario : cenarios) {
                    // UMA entrada original por (tamanho, cenário)
                    int[] original = GeradorDeVetores.gerar(cenario, n);
                    // resultado de referência, usado só na verificação
                    int[] esperado = original.clone();
                    Arrays.sort(esperado);

                    for (String algoritmo : algoritmos) {
                        combinacaoAtual++;
                        String chave = algoritmo + "|" + cenario;
                        String prefixo = String.format("[%3d/%d] %-21s %-22s n=%-7d",
                                combinacaoAtual, totalCombinacoes, algoritmo, cenario, n);

                        if (interrompidos.contains(chave)) {
                            System.out.println(prefixo + " PULADO (estourou o limite num tamanho menor)");
                            resumo.linha(algoritmo, cenario, n, 0, null, null, null, null, null,
                                    "PULADO_TEMPO_LIMITE");
                            celulasTabela.put(chave + "|" + n, "tempo_limite");
                            continue;
                        }

                        System.gc(); // limpa a memória ANTES de medir (fora da medição)
                        List<Double> temposValidos = new ArrayList<>();
                        boolean estourou = false;

                        for (int exec = 1; exec <= execucoes; exec++) {
                            int[] copia = original.clone(); // cópia da MESMA entrada original

                            long inicio = System.nanoTime();
                            ordenar(algoritmo, copia);
                            long fim = System.nanoTime();
                            long tempoNs = fim - inicio;

                            // verificação fora da medição
                            Verificador.verificarOrdenacao(algoritmo, copia, esperado);

                            boolean ehWarmup = exec <= warmup;
                            brutos.linha(algoritmo, cenario, n, exec, ehWarmup ? "sim" : "nao",
                                    tempoNs, tempoNs / NANOS_POR_MS);
                            if (!ehWarmup) {
                                temposValidos.add(tempoNs / NANOS_POR_MS);
                            }

                            if (tempoNs / 1e9 > limiteSegundos) {
                                estourou = true;
                                break;
                            }
                        }

                        if (estourou) {
                            interrompidos.add(chave);
                            System.out.printf("%s INTERROMPIDO: uma execução passou de %.0f s%n",
                                    prefixo, limiteSegundos);
                            resumo.linha(algoritmo, cenario, n, temposValidos.size(),
                                    null, null, null, null, null, "INTERROMPIDO_TEMPO_LIMITE");
                            celulasTabela.put(chave + "|" + n, "tempo_limite");
                            continue;
                        }

                        Estatisticas e = new Estatisticas(paraArray(temposValidos));
                        resumo.linha(algoritmo, cenario, n, e.getQuantidade(),
                                e.getMedia(), e.getDesvioPadrao(), e.getMediana(),
                                e.getMinimo(), e.getMaximo(), "OK");
                        celulasTabela.put(chave + "|" + n, String.format(java.util.Locale.US, "%.4f", e.getMedia()));

                        double decorridoMin = (System.nanoTime() - inicioGeral) / 60e9;
                        System.out.printf("%s média = %12.3f ms  (dp %.3f)  [%.1f min decorridos]%n",
                                prefixo, e.getMedia(), e.getDesvioPadrao(), decorridoMin);
                    }
                }
            }
        }

        escreverTabela(pastaResultados, celulasTabela);
        System.out.printf("=== '%s' concluído em %.1f minutos ===%n",
                nome, (System.nanoTime() - inicioGeral) / 60e9);
    }

    /** Tabela no formato da questão 16: uma linha por algoritmo, colunas cenário × tamanho. */
    private void escreverTabela(Path pasta, Map<String, String> celulas) throws IOException {
        List<String> cabecalho = new ArrayList<>();
        cabecalho.add("algoritmo");
        for (String cenario : GeradorDeVetores.CENARIOS) {
            for (int n : tamanhos) {
                cabecalho.add(cenario + "_n" + n + "_ms");
            }
        }
        try (EscritorCsv tabela = new EscritorCsv(pasta.resolve(nome + "_tabela.csv"),
                cabecalho.toArray(new String[0]))) {
            for (String algoritmo : algoritmos) {
                List<Object> linha = new ArrayList<>();
                linha.add(algoritmo);
                for (String cenario : GeradorDeVetores.CENARIOS) {
                    for (int n : tamanhos) {
                        linha.add(celulas.getOrDefault(algoritmo + "|" + cenario + "|" + n, ""));
                    }
                }
                tabela.linha(linha.toArray());
            }
        }
    }

    private static double[] paraArray(List<Double> lista) {
        double[] v = new double[lista.size()];
        for (int i = 0; i < v.length; i++) {
            v[i] = lista.get(i);
        }
        return v;
    }
}
