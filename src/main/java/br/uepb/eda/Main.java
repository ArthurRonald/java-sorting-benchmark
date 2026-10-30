package br.uepb.eda;

import br.uepb.eda.experimento.ExperimentoBusca;
import br.uepb.eda.experimento.ExperimentoOrdenacao;
import br.uepb.eda.experimento.TesteCorrecao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Ponto de entrada do projeto. Uso:
 *
 *   java -cp target/classes br.uepb.eda.Main <modo> [tamanhos]
 *
 * Modos:
 *   teste            -> testes de correção + mini-experimento (segundos). Rode PRIMEIRO.
 *   busca            -> experimento de busca (n = 50.000, 150.000, 500.000).
 *   ordenacao        -> experimento PRINCIPAL: os 10 algoritmos de ordenação,
 *                       nos 4 cenários, n = 25.000, 50.000, 100.000.
 *   ordenacao-grande -> complementar: só os algoritmos rápidos,
 *                       n = 50.000, 150.000, 500.000 (tamanhos sugeridos pelo professor).
 *   tudo             -> busca + ordenacao + ordenacao-grande, nesta ordem.
 *
 * [tamanhos] (opcional) substitui os tamanhos padrão do modo, ex.: ordenacao 10000,20000,40000
 *
 * Os CSVs são gravados na pasta "resultados/" (a partir da pasta onde o programa é executado).
 */
public class Main {

    // Regras do enunciado
    static final int EXECUCOES = 25;
    static final int WARMUP = 5;

    // Tamanhos padrão
    static final int[] TAMANHOS_BUSCA = {50_000, 150_000, 500_000};
    static final int[] TAMANHOS_ORDENACAO = {25_000, 50_000, 100_000};
    static final int[] TAMANHOS_ORDENACAO_GRANDE = {50_000, 150_000, 500_000};

    /** Uma busca é repetida 1000 vezes em cada execução (ver ExperimentoBusca). */
    static final int BUSCAS_POR_LOTE = 1000;

    /** Se UMA execução de ordenação passar disso, a combinação é interrompida. */
    static final double LIMITE_SEGUNDOS = 20.0;

    /**
     * Pilha de 1 GB para a thread do experimento. Necessária porque a Busca Linear
     * Recursiva (profundidade n) e o QuickSort do slide em vetor ordenado/inverso
     * (profundidade ~n) estourariam a pilha padrão da JVM (StackOverflowError).
     */
    static final long TAMANHO_PILHA = 1L << 30;

    static final Path PASTA_RESULTADOS = Path.of("resultados");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Informe o modo: teste | busca | ordenacao | ordenacao-grande | tudo");
            System.out.println("Exemplo: java -cp target/classes br.uepb.eda.Main teste");
            System.exit(1);
        }
        String modo = args[0];
        int[] tamanhosInformados = args.length > 1 ? lerTamanhos(args[1]) : null;

        // Roda o experimento numa thread com pilha grande e espera ela terminar
        Throwable[] erro = new Throwable[1];
        Thread t = new Thread(null, () -> {
            try {
                executarModo(modo, tamanhosInformados);
            } catch (Throwable e) {
                erro[0] = e;
            }
        }, "experimento", TAMANHO_PILHA);
        t.start();
        t.join();

        if (erro[0] != null) {
            System.err.println("\nO experimento FALHOU: " + erro[0]);
            erro[0].printStackTrace();
            System.exit(2);
        }
    }

    private static void executarModo(String modo, int[] tamanhos) throws IOException {
        registrarAmbiente(modo);
        switch (modo) {
            case "teste" -> {
                TesteCorrecao.executar();
                // mini-experimento só para conferir que tudo funciona e os CSVs são gerados
                new ExperimentoBusca("teste_busca", new int[]{1_000, 10_000, 100_000},
                        6, 1, BUSCAS_POR_LOTE).executar(PASTA_RESULTADOS);
                new ExperimentoOrdenacao("teste_ordenacao", new int[]{1_000, 2_000, 4_000},
                        ExperimentoOrdenacao.TODOS, 6, 1, LIMITE_SEGUNDOS).executar(PASTA_RESULTADOS);
                System.out.println("\nTeste concluído. Se não apareceu nenhum ERRO, pode rodar os experimentos reais.");
            }
            case "busca" -> busca(tamanhos);
            case "ordenacao" -> ordenacao(tamanhos);
            case "ordenacao-grande" -> ordenacaoGrande(tamanhos);
            case "tudo" -> {
                busca(null);
                ordenacao(null);
                ordenacaoGrande(null);
            }
            default -> throw new IllegalArgumentException("Modo desconhecido: " + modo);
        }
    }

    private static void busca(int[] tamanhos) throws IOException {
        new ExperimentoBusca("busca", tamanhos != null ? tamanhos : TAMANHOS_BUSCA,
                EXECUCOES, WARMUP, BUSCAS_POR_LOTE).executar(PASTA_RESULTADOS);
    }

    private static void ordenacao(int[] tamanhos) throws IOException {
        new ExperimentoOrdenacao("ordenacao", tamanhos != null ? tamanhos : TAMANHOS_ORDENACAO,
                ExperimentoOrdenacao.TODOS, EXECUCOES, WARMUP, LIMITE_SEGUNDOS).executar(PASTA_RESULTADOS);
    }

    private static void ordenacaoGrande(int[] tamanhos) throws IOException {
        new ExperimentoOrdenacao("ordenacao_grande", tamanhos != null ? tamanhos : TAMANHOS_ORDENACAO_GRANDE,
                ExperimentoOrdenacao.RAPIDOS, EXECUCOES, WARMUP, LIMITE_SEGUNDOS).executar(PASTA_RESULTADOS);
    }

    /** Converte "10000,20000,40000" em {10000, 20000, 40000}. */
    private static int[] lerTamanhos(String texto) {
        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /** Anexa ao arquivo ambiente.txt os dados da máquina (úteis na seção Metodologia do relatório). */
    private static void registrarAmbiente(String modo) throws IOException {
        Files.createDirectories(PASTA_RESULTADOS);
        String info = String.format("""
                ---- %s | modo: %s ----
                Java: %s (%s)
                Sistema: %s %s (%s)
                Núcleos disponíveis: %d
                Memória máxima da JVM: %d MB
                Execuções por combinação: %d (descartadas como warm-up: %d)
                """,
                LocalDateTime.now().withNano(0), modo,
                System.getProperty("java.version"), System.getProperty("java.vm.name"),
                System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory() / (1024 * 1024),
                EXECUCOES, WARMUP);
        System.out.print(info);
        Files.writeString(PASTA_RESULTADOS.resolve("ambiente.txt"), info, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
    }
}
