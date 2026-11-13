package rocket.estructuras;


public class ColaPrioridad<T> {

    private static class Entrada<T> {
        final T dato;
        final int prioridad;
        final long orden;

        Entrada(T dato, int prioridad, long orden) {
            this.dato = dato;
            this.prioridad = prioridad;
            this.orden = orden;
        }
    }

    private Entrada<T>[] heap;
    private int tamano;
    private long contador;

    public ColaPrioridad() {
        this.heap = crearArreglo(16);
    }

    @SuppressWarnings("unchecked")
    private static <E> Entrada<E>[] crearArreglo(int n) {
        return (Entrada<E>[]) new Entrada[n];
    }

    /** true si a debe salir antes que b. */
    private boolean antes(Entrada<T> a, Entrada<T> b) {
        if (a.prioridad != b.prioridad) {
            return a.prioridad > b.prioridad;
        }
        return a.orden < b.orden;
    }

    public void encolar(T dato, int prioridad) {
        if (dato == null) {
            throw new IllegalArgumentException("No se puede encolar null");
        }
        if (tamano == heap.length) {
            Entrada<T>[] nuevo = crearArreglo(heap.length * 2);
            for (int i = 0; i < tamano; i++) {
                nuevo[i] = heap[i];
            }
            heap = nuevo;
        }
        heap[tamano] = new Entrada<>(dato, prioridad, contador++);
        subir(tamano);
        tamano++;
    }

    public T desencolar() {
        if (tamano == 0) {
            throw new IllegalStateException("La cola de prioridad está vacía");
        }
        T dato = heap[0].dato;
        tamano--;
        heap[0] = heap[tamano];
        heap[tamano] = null;
        if (tamano > 0) {
            bajar(0);
        }
        return dato;
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (!antes(heap[i], heap[padre])) {
                break;
            }
            intercambiar(i, padre);
            i = padre;
        }
    }

    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1;
            int der = izq + 1;
            int mejor = i;
            if (izq < tamano && antes(heap[izq], heap[mejor])) {
                mejor = izq;
            }
            if (der < tamano && antes(heap[der], heap[mejor])) {
                mejor = der;
            }
            if (mejor == i) {
                return;
            }
            intercambiar(i, mejor);
            i = mejor;
        }
    }

    private void intercambiar(int a, int b) {
        Entrada<T> tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    /** Copia independiente: desencolar la copia no afecta a la original. */
    public ColaPrioridad<T> copia() {
        ColaPrioridad<T> c = new ColaPrioridad<>();
        c.heap = crearArreglo(heap.length);
        for (int i = 0; i < tamano; i++) {
            c.heap[i] = heap[i];
        }
        c.tamano = tamano;
        c.contador = contador;
        return c;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamanio() {
        return tamano;
    }
}
