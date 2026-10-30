package br.uepb.eda.experimento;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Escreve arquivos CSV simples (separador vírgula, decimal com ponto).
 * Cada linha é gravada e enviada ao disco na hora (flush), então, se o programa
 * for interrompido, o que já foi medido não se perde.
 */
public class EscritorCsv implements AutoCloseable {

    private final PrintWriter writer;

    public EscritorCsv(Path arquivo, String... cabecalho) throws IOException {
        Files.createDirectories(arquivo.getParent());
        this.writer = new PrintWriter(Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8));
        linha((Object[]) cabecalho);
    }

    /** Escreve uma linha. Números decimais saem com ponto (Locale.US) e 4 casas. */
    public void linha(Object... valores) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            Object v = valores[i];
            if (v instanceof Double d) {
                sb.append(String.format(Locale.US, "%.4f", d));
            } else {
                sb.append(v == null ? "" : v.toString());
            }
        }
        writer.println(sb);
        writer.flush();
    }

    @Override
    public void close() {
        writer.close();
    }
}
