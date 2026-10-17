# Java Sorting & Search Benchmark

Implementação em Java de algoritmos de ordenação e busca sobre `int[]`, com um benchmark que compara o tempo medido com a complexidade esperada.

## Algoritmos

**Ordenação:** Bubble Sort (clássica e otimizada), Selection Sort (clássica e estável), Insertion Sort, Merge Sort, Quick Sort (pivô fixo e com shuffle), Counting Sort. O `Arrays.sort(int[])` é usado apenas como referência.

**Busca (em vetor ordenado):** linear iterativa, linear recursiva, binária iterativa, binária recursiva e linear iterativa de duas pontas.

## Metodologia

- Valores inteiros em `[1, 100.000]`, com repetições.
- Tamanhos dos vetores: 50.000, 150.000 e 500.000 elementos (podem ser ajustados conforme o tempo de execução).
- Cenários: aleatório, ordenado, ordem inversa e parcialmente ordenado (cerca de 15% das posições embaralhadas).
- 25 execuções por combinação, descartando as 5 primeiras como warm-up.
- Tempo medido com `System.nanoTime()`.
- Todos os algoritmos recebem cópias da mesma entrada.
- A corretude é verificada após cada execução, fora da medição de tempo.

## Como executar

Requer Java 21 ou superior. Testado com Java 26.0.1.

```bash
javac -d out src/*.java
java -cp out Main
```

## Resultados

Os dados brutos estão na pasta `data/`. O relatório completo está em [PREENCHER: link do PDF].

## Autores

- Arthur Ronald Soares Evaristo
- Karlos Eduardo Saraiva da Silva

Projeto desenvolvido na disciplina de Estrutura de Dados (UEPB, 2026.2).
