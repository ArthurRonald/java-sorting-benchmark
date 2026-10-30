package br.uepb.eda.experimento;

import br.uepb.eda.ordenacao.Ordenacao;

import java.util.Arrays;
import java.util.Random;

/**
 * Gera os vetores de entrada dos 4 cenários exigidos no enunciado.
 * Valores sempre no intervalo [1, 100.000], com repetições permitidas.
 *
 * Usa uma semente fixa: rodando de novo, as entradas são exatamente as mesmas
 * (o experimento fica reproduzível).
 */
public final class GeradorDeVetores {

    public static final String ALEATORIO = "aleatorio";
    public static final String ORDENADO = "ordenado";
    public static final String INVERSO = "inverso";
    public static final String PARCIAL = "parcialmente_ordenado";

    /** Os 4 cenários, na ordem em que aparecem nas tabelas. */
    public static final String[] CENARIOS = {ALEATORIO, ORDENADO, INVERSO, PARCIAL};

    /** Fração de posições embaralhadas no cenário parcialmente ordenado. */
    public static final double FRACAO_PARCIAL = 0.15;

    private static final long SEMENTE = 2026L;

    private GeradorDeVetores() {
    }

    /** Gera o vetor ORIGINAL de um cenário. Todos os algoritmos recebem cópias dele. */
    public static int[] gerar(String cenario, int n) {
        // semente diferente para cada (cenário, tamanho), mas sempre a mesma entre execuções
        Random rnd = new Random(SEMENTE + 31L * n + cenario.hashCode());
        return switch (cenario) {
            case ALEATORIO -> aleatorio(n, rnd);
            case ORDENADO -> ordenado(n, rnd);
            case INVERSO -> inverso(n, rnd);
            case PARCIAL -> parcialmenteOrdenado(n, rnd);
            default -> throw new IllegalArgumentException("Cenário desconhecido: " + cenario);
        };
    }

    /** Valores aleatórios uniformes em [1, 100.000]. */
    public static int[] aleatorio(int n, Random rnd) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) {
            a[i] = rnd.nextInt(Ordenacao.VALOR_MAXIMO) + 1; // nextInt(100000) dá [0, 99999]
        }
        return a;
    }

    /** Vetor aleatório já ordenado de forma crescente (com repetições). */
    public static int[] ordenado(int n, Random rnd) {
        int[] a = aleatorio(n, rnd);
        Arrays.sort(a); // preparação da entrada: NÃO entra na medição
        return a;
    }

    /** Vetor ordenado de forma decrescente. */
    public static int[] inverso(int n, Random rnd) {
        int[] a = ordenado(n, rnd);
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
        return a;
    }

    /**
     * Parcialmente ordenado, como o enunciado pede: parte de um vetor ordenado,
     * sorteia ~15% das posições e embaralha APENAS os elementos dessas posições.
     */
    public static int[] parcialmenteOrdenado(int n, Random rnd) {
        int[] a = ordenado(n, rnd);
        int quantidade = (int) Math.round(n * FRACAO_PARCIAL);

        // 1) sorteia 'quantidade' posições distintas (embaralha os índices e pega os primeiros)
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }
        for (int i = 0; i < quantidade; i++) {
            int r = i + rnd.nextInt(n - i); // Fisher-Yates parcial
            int t = indices[i];
            indices[i] = indices[r];
            indices[r] = t;
        }

        // 2) embaralha os valores só entre as posições sorteadas (Fisher-Yates)
        for (int i = quantidade - 1; i > 0; i--) {
            int r = rnd.nextInt(i + 1);
            int p1 = indices[i];
            int p2 = indices[r];
            int t = a[p1];
            a[p1] = a[p2];
            a[p2] = t;
        }
        return a;
    }
}
