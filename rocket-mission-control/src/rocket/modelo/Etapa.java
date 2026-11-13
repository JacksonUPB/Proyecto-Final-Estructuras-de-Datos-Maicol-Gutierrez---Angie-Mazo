package rocket.modelo;

public enum Etapa {
    PREPARACION("Preparación", "Combustible y sistemas verificados"),
    CUENTA_REGRESIVA("Cuenta regresiva", "Presión dentro del rango permitido"),
    DESPEGUE("Despegue", "Motor y sistema eléctrico operativos"),
    ASCENSO("Ascenso", "Comunicación estable"),
    RESULTADO("Resultado", "Etapas completadas");

    private final String nombre;
    private final String mensajeOk;

    Etapa(String nombre, String mensajeOk) {
        this.nombre = nombre;
        this.mensajeOk = mensajeOk;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMensajeOk() {
        return mensajeOk;
    }

    /** Un fallo en estas etapas ocurre después de iniciar el despegue (lanzamiento fallido). */
    public boolean despegueIniciado() {
        return this == DESPEGUE || this == ASCENSO;
    }
}
