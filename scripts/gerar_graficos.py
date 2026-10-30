"""
Gera os gráficos do relatório a partir dos CSVs REAIS da pasta resultados/.
Os PNGs são salvos em graficos/.

Uso (a partir da pasta projeto-eda-unidade1, com o ambiente virtual ativado):
    python scripts/gerar_graficos.py

Requer apenas o matplotlib (pip install matplotlib).
"""

import csv
import math
from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # gera arquivos sem abrir janela
import matplotlib.pyplot as plt

RAIZ = Path(__file__).resolve().parent.parent
RESULTADOS = RAIZ / "resultados"
GRAFICOS = RAIZ / "graficos"

CENARIOS = ["aleatorio", "ordenado", "inverso", "parcialmente_ordenado"]
NOMES_CENARIOS = {
    "aleatorio": "Aleatório",
    "ordenado": "Já ordenado",
    "inverso": "Ordem inversa",
    "parcialmente_ordenado": "Parcialmente ordenado (15%)",
}


def ler_csv(nome):
    caminho = RESULTADOS / nome
    if not caminho.exists():
        print(f"  (arquivo {nome} não encontrado - pulando)")
        return None
    with open(caminho, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def salvar(fig, nome):
    GRAFICOS.mkdir(exist_ok=True)
    caminho = GRAFICOS / nome
    fig.tight_layout()
    fig.savefig(caminho, dpi=150)
    plt.close(fig)
    print(f"  gerado: graficos/{nome}")


def colunas_agrupadas(ax, grupos, series, valores, unidade, log=False):
    """Gráfico de colunas: eixo x = grupos (tamanhos), uma barra por série (algoritmo)."""
    largura = 0.8 / len(series)
    for i, serie in enumerate(series):
        xs = [g + (i - (len(series) - 1) / 2) * largura for g in range(len(grupos))]
        ys = [valores.get((serie, grupo), 0) for grupo in grupos]
        ax.bar(xs, ys, width=largura, label=serie)
    ax.set_xticks(range(len(grupos)))
    ax.set_xticklabels([f"{g:,}".replace(",", ".") for g in grupos])
    ax.set_xlabel("Tamanho do vetor de entrada (n)")
    ax.set_ylabel(f"Tempo médio ({unidade})" + (" - escala log" if log else ""))
    if log:
        ax.set_yscale("log")
    ax.grid(axis="y", alpha=0.3)
    ax.legend(fontsize=8)


# ---------------------------------------------------------------- BUSCA
def graficos_busca():
    print("Busca:")
    linhas = ler_csv("busca_resumo.csv")
    if not linhas:
        return
    valores = {(l["algoritmo"], int(l["tamanho"])): float(l["media_ns"]) for l in linhas}
    tamanhos = sorted({int(l["tamanho"]) for l in linhas})
    algoritmos = list(dict.fromkeys(l["algoritmo"] for l in linhas))
    lineares = [a for a in algoritmos if "Linear" in a]
    binarias = [a for a in algoritmos if "Binaria" in a]

    # 1) todas juntas, escala log (diferença linear x binária é de milhares de vezes)
    fig, ax = plt.subplots(figsize=(9, 5))
    colunas_agrupadas(ax, tamanhos, algoritmos, valores, "ns por busca", log=True)
    ax.set_title("Buscas lineares x binárias (elemento -1, ausente)")
    salvar(fig, "busca_todas_log.png")

    # 2) só as lineares, escala normal (mostra o crescimento proporcional a n)
    fig, ax = plt.subplots(figsize=(8, 5))
    colunas_agrupadas(ax, tamanhos, lineares, valores, "ns por busca")
    ax.set_title("Buscas lineares - O(n)")
    salvar(fig, "busca_lineares.png")

    # 3) só as binárias, escala normal (mostra o crescimento ~log n)
    fig, ax = plt.subplots(figsize=(8, 5))
    colunas_agrupadas(ax, tamanhos, binarias, valores, "ns por busca")
    ax.set_title("Buscas binárias - O(log n)")
    salvar(fig, "busca_binarias.png")


# ----------------------------------------------------------- ORDENAÇÃO
def graficos_ordenacao(prefixo, titulo):
    print(f"Ordenação ({prefixo}):")
    linhas = ler_csv(f"{prefixo}_resumo.csv")
    if not linhas:
        return
    ok = [l for l in linhas if l["status"] == "OK"]
    valores = {(l["algoritmo"], l["cenario"], int(l["tamanho"])): float(l["media_ms"]) for l in ok}
    tamanhos = sorted({int(l["tamanho"]) for l in linhas})
    algoritmos = list(dict.fromkeys(l["algoritmo"] for l in linhas))

    for cenario in CENARIOS:
        # Linhas: tempo x n, um traço por algoritmo (escala log no eixo y)
        fig, ax = plt.subplots(figsize=(9, 5.5))
        for alg in algoritmos:
            pontos = [(n, valores[(alg, cenario, n)]) for n in tamanhos
                      if valores.get((alg, cenario, n), 0) > 0]  # escala log não aceita 0
            if pontos:
                ax.plot([p[0] for p in pontos], [p[1] for p in pontos], marker="o", label=alg)
        ax.set_yscale("log")
        ax.set_xticks(tamanhos)
        ax.set_xticklabels([f"{n:,}".replace(",", ".") for n in tamanhos])
        ax.set_xlabel("Tamanho do vetor (n)")
        ax.set_ylabel("Tempo médio (ms) - escala log")
        ax.set_title(f"{titulo} - {NOMES_CENARIOS[cenario]}")
        ax.grid(alpha=0.3, which="both")
        ax.legend(fontsize=8, ncol=2)
        salvar(fig, f"{prefixo}_{cenario}_linhas.png")

        # Colunas: comparação no MAIOR tamanho
        n = tamanhos[-1]
        presentes = [a for a in algoritmos if (a, cenario, n) in valores]
        if presentes:
            fig, ax = plt.subplots(figsize=(9, 5))
            ys = [valores[(a, cenario, n)] for a in presentes]
            ax.bar(presentes, ys, color="#4c72b0")
            ax.set_yscale("log")
            ax.set_ylabel("Tempo médio (ms) - escala log")
            ax.set_title(f"{titulo} - {NOMES_CENARIOS[cenario]} - n = {n:,}".replace(",", "."))
            ax.tick_params(axis="x", rotation=45)
            for lbl in ax.get_xticklabels():
                lbl.set_ha("right")
            ax.grid(axis="y", alpha=0.3, which="both")
            salvar(fig, f"{prefixo}_{cenario}_colunas_n{n}.png")

    # Um gráfico por algoritmo comparando os 4 cenários no maior tamanho
    n = tamanhos[-1]
    fig, ax = plt.subplots(figsize=(11, 5.5))
    largura = 0.8 / len(CENARIOS)
    for i, cenario in enumerate(CENARIOS):
        xs = [k + (i - 1.5) * largura for k in range(len(algoritmos))]
        ys = [valores.get((a, cenario, n), 0) for a in algoritmos]
        ax.bar(xs, ys, width=largura, label=NOMES_CENARIOS[cenario])
    ax.set_xticks(range(len(algoritmos)))
    ax.set_xticklabels(algoritmos, rotation=45, ha="right")
    ax.set_yscale("log")
    ax.set_ylabel("Tempo médio (ms) - escala log")
    ax.set_title(f"{titulo} - efeito do cenário em cada algoritmo (n = {n:,})".replace(",", "."))
    ax.grid(axis="y", alpha=0.3, which="both")
    ax.legend(fontsize=8)
    salvar(fig, f"{prefixo}_cenarios_por_algoritmo_n{n}.png")

    escrever_razoes(prefixo, algoritmos, tamanhos, valores)


def escrever_razoes(prefixo, algoritmos, tamanhos, valores):
    """
    Razão de crescimento: tempo(n maior) / tempo(n menor) entre tamanhos consecutivos.
    Compare com a teoria: se n dobra, O(n) -> ~2x, O(n log n) -> ~2,1x, O(n²) -> ~4x.
    Se n triplica (50k -> 150k): O(n) -> 3x, O(n log n) -> ~3,3x, O(n²) -> 9x.
    """
    caminho = GRAFICOS / f"{prefixo}_razoes_crescimento.csv"
    GRAFICOS.mkdir(exist_ok=True)
    with open(caminho, "w", encoding="utf-8", newline="") as f:
        w = csv.writer(f)
        w.writerow(["algoritmo", "cenario", "de_n", "para_n", "fator_n", "razao_tempo_observada",
                    "esperado_se_O(n)", "esperado_se_O(n_log_n)", "esperado_se_O(n2)"])
        for alg in algoritmos:
            for cenario in CENARIOS:
                for n1, n2 in zip(tamanhos, tamanhos[1:]):
                    t1, t2 = valores.get((alg, cenario, n1)), valores.get((alg, cenario, n2))
                    if not t1 or not t2:
                        continue
                    f_n = n2 / n1
                    nlogn = (n2 * math.log2(n2)) / (n1 * math.log2(n1))
                    w.writerow([alg, cenario, n1, n2, f"{f_n:.2f}", f"{t2 / t1:.2f}",
                                f"{f_n:.2f}", f"{nlogn:.2f}", f"{f_n ** 2:.2f}"])
    print(f"  gerado: graficos/{caminho.name}")


if __name__ == "__main__":
    graficos_busca()
    graficos_ordenacao("ordenacao", "Ordenação")
    graficos_ordenacao("ordenacao_grande", "Ordenação (vetores grandes)")
    print("Pronto. Abra a pasta graficos/ para ver as imagens.")
