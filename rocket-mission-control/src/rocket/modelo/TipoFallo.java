package rocket.modelo;


public enum TipoFallo {
    COMBUSTIBLE_INSUFICIENTE("Combustible insuficiente", Etapa.PREPARACION, 3),
    FALLO_MOTOR("Fallo del motor", Etapa.DESPEGUE, 5),
    PRESION_FUERA_DE_RANGO("Presión fuera de rango", Etapa.CUENTA_REGRESIVA, 3),
    FALLA_ELECTRICA("Falla eléctrica", Etapa.DESPEGUE, 4),
    PERDIDA_COMUNICACION("Pérdida de comunicación", Etapa.ASCENSO, 2);

    private final String descripcion;
    private final Etapa etapa;
    private final int severidad;

    TipoFallo(String descripcion, Etapa etapa, int severidad) {
        this.descripcion = descripcion;
        this.etapa = etapa;
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public int getSeveridad() {
        return severidad;
    }
}
