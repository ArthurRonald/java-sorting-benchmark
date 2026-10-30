package br.uepb.eda.busca;

/**
 * Algoritmos de busca exigidos no Projeto da Unidade 1.
 * Todos consideram o vetor previamente ordenado (em ordem não decrescente)
 * e retornam o ÍNDICE do elemento procurado, ou -1 se ele não existir.
 */
public final class Busca {

    private Busca() {
        // Classe utilitária: só tem métodos static
    }

    /** Busca Linear Iterativa: percorre do início ao fim. O(n). */
    public static int linearIterativa(int[] a, int x) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == x) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Busca Linear Recursiva: verifica a posição i e chama a si mesma para i+1.
     * Recorrência: T(n) = T(n-1) + Θ(1) -> O(n).
     * Atenção: a profundidade da recursão é n (por isso o experimento roda
     * numa thread com pilha grande, senão ocorre StackOverflowError).
     */
    public static int linearRecursiva(int[] a, int x) {
        return linearRecursiva(a, x, 0);
    }

    private static int linearRecursiva(int[] a, int x, int i) {
        if (i >= a.length) {
            return -1;          // caso base: chegou ao fim sem encontrar
        }
        if (a[i] == x) {
            return i;           // caso base: encontrou
        }
        return linearRecursiva(a, x, i + 1);
    }

    /**
     * Busca Linear Iterativa Duas Pontas: compara o elemento da esquerda e o
     * da direita a cada iteração, andando das pontas para o centro.
     * Faz ~n/2 iterações, mas continua O(n).
     */
    public static int linearDuasPontas(int[] a, int x) {
        int esquerda = 0;
        int direita = a.length - 1;
        while (esquerda <= direita) {
            if (a[esquerda] == x) {
                return esquerda;
            }
            if (a[direita] == x) {
                return direita;
            }
            esquerda++;
            direita--;
        }
        return -1;
    }

    /** Busca Binária Iterativa: descarta metade do vetor a cada passo. O(log n). */
    public static int binariaIterativa(int[] a, int x) {
        int inicio = 0;
        int fim = a.length - 1;
        while (inicio <= fim) {
            int meio = inicio + (fim - inicio) / 2; // evita overflow de (inicio+fim)
            if (a[meio] == x) {
                return meio;
            } else if (a[meio] < x) {
                inicio = meio + 1;  // x só pode estar à direita
            } else {
                fim = meio - 1;     // x só pode estar à esquerda
            }
        }
        return -1;
    }

    /** Busca Binária Recursiva. Recorrência: T(n) = T(n/2) + Θ(1) -> O(log n). */
    public static int binariaRecursiva(int[] a, int x) {
        return binariaRecursiva(a, x, 0, a.length - 1);
    }

    private static int binariaRecursiva(int[] a, int x, int inicio, int fim) {
        if (inicio > fim) {
            return -1; // caso base: intervalo vazio
        }
        int meio = inicio + (fim - inicio) / 2;
        if (a[meio] == x) {
            return meio;
        } else if (a[meio] < x) {
            return binariaRecursiva(a, x, meio + 1, fim);
        } else {
            return binariaRecursiva(a, x, inicio, meio - 1);
        }
    }
}
