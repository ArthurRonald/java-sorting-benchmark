package br.uepb.eda.experimento;

import java.util.Arrays;

/**
 * Estatísticas descritivas de um conjunto de tempos (em nanossegundos).
 * Recebe SOMENTE os tempos válidos (os warm-ups já foram descartados).
 */
public class Estatisticas {

    private final int quantidade;
    private final double media;
    private final double desvioPadrao;
    private final double mediana;
    private final double minimo;
    private final double maximo;

    public Estatisticas(double[] tempos) {
        if (tempos.length == 0) {
            throw new IllegalArgumentException("Nenhum tempo para calcular estatísticas");
        }
        this.quantidade = tempos.length;

        double soma = 0;
        for (double t : tempos) {
            soma += t;
        }
        this.media = soma / quantidade;

        // desvio padrão amostral (divide por n-1)
        double somaQuadrados = 0;
        for (double t : tempos) {
            somaQuadrados += (t - media) * (t - media);
        }
        this.desvioPadrao = quantidade > 1 ? Math.sqrt(somaQuadrados / (quantidade - 1)) : 0;

        double[] ordenados = tempos.clone();
        Arrays.sort(ordenados);
        this.minimo = ordenados[0];
        this.maximo = ordenados[quantidade - 1];
        if (quantidade % 2 == 1) {
            this.mediana = ordenados[quantidade / 2];
        } else {
            this.mediana = (ordenados[quantidade / 2 - 1] + ordenados[quantidade / 2]) / 2.0;
        }
    }

    public int getQuantidade() { return quantidade; }
    public double getMedia() { return media; }
    public double getDesvioPadrao() { return desvioPadrao; }
    public double getMediana() { return mediana; }
    public double getMinimo() { return minimo; }
    public double getMaximo() { return maximo; }
}
