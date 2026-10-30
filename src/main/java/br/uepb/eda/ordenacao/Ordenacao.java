package br.uepb.eda.ordenacao;

import java.util.Random;

/**
 * Algoritmos de ordenação exigidos no Projeto da Unidade 1.
 *
 * Todos seguem os pseudocódigos dos slides "EDA-05 Ordenação" (Prof. Janderson),
 * convertidos de índices 1..N para índices 0..N-1 (como o próprio slide orienta).
 * Todos ordenam o vetor recebido IN-PLACE do ponto de vista de quem chama
 * (ao final, o próprio array passado está ordenado).
 */
public final class Ordenacao {

    /** Maior valor possível nos vetores do projeto: intervalo [1, 100.000]. */
    public static final int VALOR_MAXIMO = 100_000;

    // Gerador usado apenas pelo QuickSort + shuffle
    private static final Random RANDOM = new Random();

    private Ordenacao() {
        // Classe utilitária: só tem métodos static, não deve ser instanciada
    }

    // =====================================================================
    // BUBBLE SORT
    // =====================================================================

    /** Bubble Sort - versão do slide 8: sempre faz as n-1 passadas. */
    public static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {          // slide: i <- 1 to n-1
            for (int j = 0; j < n - 1 - i; j++) {  // slide: j <- 1 to n-i
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                }
            }
        }
    }

    /**
     * Bubble Sort otimizado (observação do slide 10): se uma passada inteira
     * não fizer nenhuma troca, o vetor já está ordenado e o algoritmo para.
     * Melhor caso (vetor já ordenado): O(n).
     */
    public static void bubbleSortOtimizado(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean houveTroca = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    swap(a, j, j + 1);
                    houveTroca = true;
                }
            }
            if (!houveTroca) {
                return; // nenhuma troca nesta passada -> já ordenado
            }
        }
    }

    // =====================================================================
    // SELECTION SORT
    // =====================================================================

    /** Selection Sort - versão do slide 13 (troca direta: NÃO é estável). */
    public static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            swap(a, i, min); // o slide faz o swap sempre, mesmo se min == i
        }
    }

    /**
     * Selection Sort estável (slide 15): em vez de trocar diretamente,
     * desloca os elementos entre i e min uma posição para a direita
     * e insere o menor na posição i. Assim elementos iguais mantêm a ordem.
     *
     * Detalhe importante: usar "<" (e não "<=") para pegar a PRIMEIRA
     * ocorrência do menor valor, senão a estabilidade se perde.
     */
    public static void selectionSortEstavel(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            int menor = a[min];
            // "move os maiores para frente": desloca a[i..min-1] para a[i+1..min]
            for (int k = min; k > i; k--) {
                a[k] = a[k - 1];
            }
            a[i] = menor; // "insere o menor na posição correta"
        }
    }

    // =====================================================================
    // INSERTION SORT
    // =====================================================================

    /** Insertion Sort - versão do slide 18. */
    public static void insertionSort(int[] a) {
        int n = a.length;
        for (int j = 1; j < n; j++) {           // slide: j <- 2 to n
            int key = a[j];
            int i = j - 1;
            while (i >= 0 && a[i] > key) {      // slide: i > 0 (índice 1..N)
                a[i + 1] = a[i];
                i--;
            }
            a[i + 1] = key;
        }
    }

    // =====================================================================
    // MERGE SORT (versão clássica, top-down recursiva)
    // =====================================================================

    /**
     * Merge Sort clássico (slides 27-29): divide ao meio, ordena cada metade
     * recursivamente e intercala (merge). Usa um array auxiliar de tamanho n,
     * alocado uma única vez -> memória extra O(n).
     */
    public static void mergeSort(int[] a) {
        if (a.length < 2) {
            return;
        }
        int[] aux = new int[a.length];
        mergeSort(a, aux, 0, a.length - 1);
    }

    private static void mergeSort(int[] a, int[] aux, int left, int right) {
        if (left >= right) {
            return; // caso base: subvetor de tamanho 1 (slide: if n = 1 return)
        }
        int meio = left + (right - left) / 2; // evita overflow de (left+right)
        mergeSort(a, aux, left, meio);
        mergeSort(a, aux, meio + 1, right);
        merge(a, aux, left, meio, right);
    }

    /** Intercala a[left..meio] e a[meio+1..right], já ordenados (slide 29). */
    private static void merge(int[] a, int[] aux, int left, int meio, int right) {
        // copia o trecho para o auxiliar
        for (int k = left; k <= right; k++) {
            aux[k] = a[k];
        }
        int i = left;      // início da metade esquerda
        int j = meio + 1;  // início da metade direita
        int k = left;      // posição de escrita em a
        while (i <= meio && j <= right) {
            // "<=" garante estabilidade: em empate, vem primeiro o da esquerda
            if (aux[i] <= aux[j]) {
                a[k++] = aux[i++];
            } else {
                a[k++] = aux[j++];
            }
        }
        // copia o que sobrou (só um dos dois laços executa)
        while (i <= meio) {
            a[k++] = aux[i++];
        }
        while (j <= right) {
            a[k++] = aux[j++];
        }
    }

    // =====================================================================
    // QUICK SORT
    // =====================================================================

    /** Quick Sort - versão do slide 33, com o partition "à la Hoare" do slide 34. */
    public static void quickSort(int[] a) {
        quickSort(a, 0, a.length - 1);
    }

    /**
     * Quick Sort do slide + shuffle (observação do slide 37): embaralha o vetor
     * antes de ordenar, para evitar o pior caso em entradas já ordenadas/inversas.
     * O tempo do embaralhamento faz parte do algoritmo e é medido junto.
     */
    public static void quickSortComShuffle(int[] a) {
        shuffle(a);
        quickSort(a, 0, a.length - 1);
    }

    private static void quickSort(int[] a, int left, int right) {
        if (left < right) {
            int pivot = partition(a, left, right);
            quickSort(a, left, pivot - 1);
            quickSort(a, pivot + 1, right);
        }
    }

    /** Partition do slide 34: pivô fixo no início (a[left]). */
    private static int partition(int[] a, int left, int right) {
        int p = a[left];
        int i = left + 1;
        int j = right;
        while (i <= j) {
            if (a[i] <= p) {
                i++;
            } else if (a[j] > p) {
                j--;
            } else {
                swap(a, i, j);
            }
        }
        swap(a, left, j);
        return j; // posição final do pivô
    }

    /** Embaralhamento de Fisher-Yates: O(n), cada permutação é igualmente provável. */
    private static void shuffle(int[] a) {
        for (int i = a.length - 1; i > 0; i--) {
            int r = RANDOM.nextInt(i + 1); // sorteia em [0, i]
            swap(a, i, r);
        }
    }

    // =====================================================================
    // COUNTING SORT
    // =====================================================================

    /**
     * Counting Sort - versão do slide 42, com k = 100.000 (maior valor possível).
     * Usa C[0..k] (posição 0 nunca é usada, pois os valores começam em 1)
     * e B[0..n-1] para a saída. No fim, copia B de volta para a,
     * para que o array recebido fique ordenado (como nos outros algoritmos).
     */
    public static void countingSort(int[] a) {
        countingSort(a, VALOR_MAXIMO);
    }

    public static void countingSort(int[] a, int k) {
        int n = a.length;
        int[] c = new int[k + 1]; // Java já inicializa com 0 (slide: C[i] <- 0)
        int[] b = new int[n];

        // conta quantas vezes cada valor aparece
        for (int j = 0; j < n; j++) {
            c[a[j]]++;
        }
        // soma de prefixos: c[i] = quantidade de elementos <= i
        for (int i = 2; i <= k; i++) {
            c[i] += c[i - 1];
        }
        // percorre de trás para frente -> mantém a estabilidade
        for (int j = n - 1; j >= 0; j--) {
            b[c[a[j]] - 1] = a[j]; // "-1" porque B começa no índice 0
            c[a[j]]--;
        }
        System.arraycopy(b, 0, a, 0, n);
    }

    // =====================================================================
    // AUXILIAR
    // =====================================================================

    /** SWAP do slide 8. */
    private static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
