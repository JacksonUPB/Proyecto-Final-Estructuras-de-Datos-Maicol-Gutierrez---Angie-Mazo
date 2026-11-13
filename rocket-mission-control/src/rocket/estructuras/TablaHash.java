package rocket.estructuras;

import java.util.function.Consumer;


public class TablaHash<V> {

    private static class Nodo<V> {
        final String clave;
        V valor;
        Nodo<V> siguiente;

        Nodo(String clave, V valor, Nodo<V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private static final int CAPACIDAD_INICIAL = 16;
    private static final double FACTOR_CARGA = 0.75;

    private Nodo<V>[] tabla;
    private int tamano;

    public TablaHash() {
        this.tabla = crearArreglo(CAPACIDAD_INICIAL);
        this.tamano = 0;
    }

    @SuppressWarnings("unchecked")
    private Nodo<V>[] crearArreglo(int capacidad) {
        return (Nodo<V>[]) new Nodo[capacidad];
    }

    private static int indice(String clave, int capacidad) {
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    private void validar(String clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
    }

    public boolean poner(String clave, V valor) {
        validar(clave);
        int i = indice(clave, tabla.length);
        for (Nodo<V> n = tabla[i]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                n.valor = valor;
                return false;
            }
        }
        tabla[i] = new Nodo<>(clave, valor, tabla[i]);
        tamano++;
        if (tamano > tabla.length * FACTOR_CARGA) {
            redimensionar();
        }
        return true;
    }

    public V obtener(String clave) {
        validar(clave);
        for (Nodo<V> n = tabla[indice(clave, tabla.length)]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                return n.valor;
            }
        }
        return null;
    }

    public boolean contiene(String clave) {
        validar(clave);
        for (Nodo<V> n = tabla[indice(clave, tabla.length)]; n != null; n = n.siguiente) {
            if (n.clave.equals(clave)) {
                return true;
            }
        }
        return false;
    }

    /** Elimina la clave y devuelve su valor, o null si no existía. */
    public V eliminar(String clave) {
        validar(clave);
        int i = indice(clave, tabla.length);
        Nodo<V> anterior = null;
        for (Nodo<V> n = tabla[i]; n != null; anterior = n, n = n.siguiente) {
            if (n.clave.equals(clave)) {
                if (anterior == null) {
                    tabla[i] = n.siguiente;
                } else {
                    anterior.siguiente = n.siguiente;
                }
                tamano--;
                return n.valor;
            }
        }
        return null;
    }

    private void redimensionar() {
        Nodo<V>[] vieja = tabla;
        Nodo<V>[] nueva = crearArreglo(vieja.length * 2);
        for (Nodo<V> cabeza : vieja) {
            for (Nodo<V> n = cabeza; n != null; ) {
                Nodo<V> sig = n.siguiente;
                int i = indice(n.clave, nueva.length);
                n.siguiente = nueva[i];
                nueva[i] = n;
                n = sig;
            }
        }
        tabla = nueva;
    }

    public void paraCada(Consumer<V> accion) {
        for (Nodo<V> cabeza : tabla) {
            for (Nodo<V> n = cabeza; n != null; n = n.siguiente) {
                accion.accept(n.valor);
            }
        }
    }

    public int tamanio() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int capacidad() {
        return tabla.length;
    }

    public int longitudMaximaCadena() {
        int max = 0;
        for (Nodo<V> cabeza : tabla) {
            int largo = 0;
            for (Nodo<V> n = cabeza; n != null; n = n.siguiente) {
                largo++;
            }
            if (largo > max) {
                max = largo;
            }
        }
        return max;
    }
}
