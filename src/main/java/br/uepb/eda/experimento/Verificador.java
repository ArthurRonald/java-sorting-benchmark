package br.uepb.eda.experimento;

import java.util.Arrays;

/**
 * Verifica a correção dos resultados. É chamado DEPOIS de medir o tempo,
 * então o custo da verificação não entra na medição (exigência do enunciado).
 */
public final class Verificador {

    private Verificador() {
    }

    /** true se a[i] <= a[i+1] para todo i (ordem não decrescente). */
    public static boolean estaOrdenado(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verificação completa da ordenação:
     * 1) o vetor está ordenado; e
     * 2) é igual ao resultado de referência (mesmos elementos, mesmas repetições),
     *    o que garante que o algoritmo não perdeu nem inventou valores.
     * Lança erro se algo estiver errado, para o experimento parar na hora.
     */
    public static void verificarOrdenacao(String algoritmo, int[] resultado, int[] esperado) {
        if (!estaOrdenado(resultado)) {
            throw new IllegalStateException("ERRO: " + algoritmo + " produziu um vetor NÃO ordenado!");
        }
        if (!Arrays.equals(resultado, esperado)) {
            throw new IllegalStateException("ERRO: " + algoritmo + " alterou os elementos do vetor!");
        }
    }

    /** Verifica o resultado de uma busca. */
    public static void verificarBusca(String algoritmo, int[] a, int x, int indiceRetornado) {
        boolean correto;
        if (indiceRetornado == -1) {
            // retornou "não encontrado": x realmente não pode estar no vetor
            correto = Arrays.binarySearch(a, x) < 0;
        } else {
            correto = indiceRetornado >= 0 && indiceRetornado < a.length && a[indiceRetornado] == x;
        }
        if (!correto) {
            throw new IllegalStateException("ERRO: " + algoritmo + " retornou " + indiceRetornado
                    + " ao procurar " + x);
        }
    }
}
