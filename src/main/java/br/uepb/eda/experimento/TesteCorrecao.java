package br.uepb.eda.experimento;

import java.util.Arrays;
import java.util.Random;

/**
 * Testes rápidos de correção (modo "teste"): roda todos os algoritmos em vetores
 * pequenos, inclusive casos-limite, e confere os resultados. Se algo estiver errado,
 * o programa para com uma mensagem de ERRO. Leva poucos segundos.
 */
public final class TesteCorrecao {

    private TesteCorrecao() {
    }

    public static void executar() {
        System.out.println("\n=== Testes de correção ===");
        testarOrdenacoes();
        testarBuscas();
        System.out.println("=== Todos os testes de correção passaram ===");
    }

    private static void testarOrdenacoes() {
        Random rnd = new Random(42);
        int[] tamanhos = {0, 1, 2, 3, 5, 10, 100, 1000, 3000};
        int casos = 0;

        for (String algoritmo : ExperimentoOrdenacao.TODOS) {
            for (int n : tamanhos) {
                for (String cenario : GeradorDeVetores.CENARIOS) {
                    testarUmaOrdenacao(algoritmo, GeradorDeVetores.gerar(cenario, n));
                    casos++;
                }
                // casos-limite: todos iguais, só dois valores, valores extremos 1 e 100.000
                testarUmaOrdenacao(algoritmo, preencher(n, 7));
                testarUmaOrdenacao(algoritmo, doisValores(n, rnd));
                testarUmaOrdenacao(algoritmo, extremos(n, rnd));
                casos += 3;
            }
            System.out.printf("  OK  %-21s%n", algoritmo);
        }
        System.out.println("  " + casos + " casos de ordenação verificados");
    }

    private static void testarUmaOrdenacao(String algoritmo, int[] entrada) {
        int[] esperado = entrada.clone();
        Arrays.sort(esperado);
        int[] copia = entrada.clone();
        ExperimentoOrdenacao.ordenar(algoritmo, copia);
        Verificador.verificarOrdenacao(algoritmo, copia, esperado);
    }

    private static void testarBuscas() {
        Random rnd = new Random(7);
        int[] tamanhos = {0, 1, 2, 3, 10, 1000};
        int casos = 0;

        for (String algoritmo : ExperimentoBusca.TODAS) {
            for (int n : tamanhos) {
                int[] a = GeradorDeVetores.ordenado(n, rnd);
                // elementos presentes (testa todos os índices)
                for (int i = 0; i < n; i++) {
                    int r = ExperimentoBusca.buscar(algoritmo, a, a[i]);
                    Verificador.verificarBusca(algoritmo, a, a[i], r);
                    casos++;
                }
                // elementos ausentes: -1 (o do enunciado), 0 e um valor acima do máximo
                for (int x : new int[]{-1, 0, 100_001}) {
                    int r = ExperimentoBusca.buscar(algoritmo, a, x);
                    Verificador.verificarBusca(algoritmo, a, x, r);
                    if (r != -1) {
                        throw new IllegalStateException("ERRO: " + algoritmo + " achou " + x);
                    }
                    casos++;
                }
            }
            System.out.printf("  OK  %-21s%n", algoritmo);
        }
        System.out.println("  " + casos + " casos de busca verificados");
    }

    private static int[] preencher(int n, int valor) {
        int[] a = new int[n];
        Arrays.fill(a, valor);
        return a;
    }

    private static int[] doisValores(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextBoolean() ? 3 : 9;
        }
        return a;
    }

    private static int[] extremos(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextBoolean() ? 1 : 100_000;
        }
        return a;
    }
}
