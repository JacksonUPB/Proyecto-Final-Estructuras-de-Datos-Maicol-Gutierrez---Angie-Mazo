package rocket.estructuras;

import java.util.function.Consumer;

public class ColaFIFO<T> {

    private static class Nodo<T> {
        final T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    public void encolar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("No se puede encolar null");
        }
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    public T desencolar() {
        if (frente == null) {
            throw new IllegalStateException("La cola está vacía");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return dato;
    }

    public T verFrente() {
        if (frente == null) {
            throw new IllegalStateException("La cola está vacía");
        }
        return frente.dato;
    }

    public void paraCada(Consumer<T> accion) {
        for (Nodo<T> n = frente; n != null; n = n.siguiente) {
            accion.accept(n.dato);
        }
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return tamano;
    }
}
