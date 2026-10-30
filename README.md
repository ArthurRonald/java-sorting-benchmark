# EDA/LEDA 2026.2: Projeto da Unidade 1
## Análise de Algoritmos de Busca e Ordenação em Java (`int[]`)

Este repositório implementa e mede (com `System.nanoTime()`) os algoritmos de busca e ordenação pedidos no enunciado. Os resultados são exportados para CSV e os gráficos são gerados a partir deles.

**Autores:** Arthur Ronald Soares Evaristo e Karlos Eduardo Saraiva da Silva
**Disciplina:** EDA/LEDA 2026.2, UEPB (Prof. Janderson Jason Barbosa Aguiar)

## Estrutura

```
java-sorting-benchmark/
├── pom.xml                         # projeto Maven (IntelliJ abre direto)
├── README.md
├── LICENSE                         # MIT
├── src/main/java/br/uepb/eda/
│   ├── Main.java                   # ponto de entrada: escolhe o modo do experimento
│   ├── ordenacao/Ordenacao.java    # Bubble (2), Selection (2), Insertion, Merge, Quick (2), Counting
│   ├── busca/Busca.java            # Linear iterativa/recursiva/duas pontas, Binária iterativa/recursiva
│   └── experimento/
│       ├── GeradorDeVetores.java   # 4 cenários: aleatório, ordenado, inverso, parcial (15%)
│       ├── Verificador.java        # confere se o resultado está correto (fora da medição)
│       ├── Estatisticas.java       # média, desvio padrão, mediana, mínimo, máximo
│       ├── EscritorCsv.java        # grava os CSVs
│       ├── ExperimentoOrdenacao.java
│       ├── ExperimentoBusca.java
│       └── TesteCorrecao.java      # testes de correção (modo "teste")
├── scripts/
│   ├── executar.sh                 # compila e roda um modo (macOS/Linux/Git Bash)
│   └── gerar_graficos.py           # gera os PNGs a partir dos CSVs
├── resultados/                     # CSVs e logs reais usados no relatório
├── graficos/                       # PNGs e razões de crescimento gerados dos CSVs
└── relatorio/                      # relatório final (.docx e .pdf)
```

## Requisitos

- JDK 21 ou superior (os resultados foram obtidos com JDK 26)
- Maven (opcional) — ou apenas `javac`
- Python 3 com `matplotlib` (somente para gerar os gráficos)

## Como executar

Modos: `teste`, `busca`, `ordenacao`, `ordenacao-grande`, `tudo`.

**macOS / Linux / Git Bash:**

```bash
./scripts/executar.sh teste              # 1) testes de correção (segundos)
./scripts/executar.sh busca              # 2) experimento de busca
./scripts/executar.sh ordenacao          # 3) experimento principal de ordenação
./scripts/executar.sh ordenacao-grande   # 4) ordenação com vetores grandes (só algoritmos rápidos)
```

**Windows (PowerShell) com Maven:**

```bash
mvn compile
java -cp target/classes br.uepb.eda.Main ordenacao
```

**Windows (PowerShell) sem Maven (só JDK):**

```powershell
mkdir out
javac -d out (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName
java -cp out br.uepb.eda.Main ordenacao
```

**Gráficos:**

```bash
pip install matplotlib
python scripts/gerar_graficos.py
```

> Os experimentos de ordenação levam dezenas de minutos. Deixe o computador na tomada e sem outros programas abertos.

## Metodologia (resumo)

| Item | Valor |
|---|---|
| Valores dos vetores | inteiros em [1, 100.000], com repetições |
| Execuções por combinação | 25 (as 5 primeiras são descartadas como warm-up) |
| Busca | n = 50.000, 150.000, 500.000; vetor ordenado; alvo = −1 (ausente) |
| Ordenação (principal) | todos os 10 algoritmos; n = 25.000, 50.000, 100.000; 4 cenários |
| Ordenação (grande) | Merge, Quick, Quick+shuffle, Counting, `Arrays.sort`; n = 50.000, 150.000, 500.000 |
| Entrada | uma entrada original por (tamanho, cenário); cada algoritmo recebe uma cópia |
| Verificação | após cada execução, fora da medição (ordenado e com os mesmos elementos) |
| Tempo limite | se uma execução passar de 20 s, a combinação é interrompida e registrada como tal |

## Combinações interrompidas pelo limite de tempo

No experimento real, estas combinações passaram de 20 s e ficaram registradas como interrompidas nos CSVs (coluna `status`):

- BubbleSort e BubbleSortOtimizado, vetor aleatório, n = 100.000
- QuickSort (sem shuffle), vetor inverso, n = 500.000

Detalhes e discussão na seção 4.4 do relatório (`relatorio/`).

## Ambiente dos experimentos

Windows 11, Intel Core i5-12450HX (8 núcleos, 12 threads), 16 GB DDR5, JDK 26.0.1 (HotSpot 64-bit). Ver `resultados/ambiente.txt`.