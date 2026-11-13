package rocket.modelo;

import java.time.LocalDate;

public class Mision {

    private final String id;
    private final String nombre;
    private final String codigoCohete;
    private final LocalDate fecha;
    private final int prioridad;
    private EstadoMision estado = EstadoMision.PROGRAMADA;

    public Mision(String id, String nombre, String codigoCohete, LocalDate fecha, int prioridad) {
        this.id = id;
        this.nombre = nombre;
        this.codigoCohete = codigoCohete;
        this.fecha = fecha;
        this.prioridad = prioridad;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoCohete() {
        return codigoCohete;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public EstadoMision getEstado() {
        return estado;
    }

    public void setEstado(EstadoMision estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | cohete %s | fecha %s | prioridad %d | estado: %s",
                id, nombre, codigoCohete, fecha, prioridad, estado);
    }
}
