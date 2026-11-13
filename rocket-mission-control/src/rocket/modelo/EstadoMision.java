package rocket.modelo;

public enum EstadoMision {
    PROGRAMADA("Programada"),
    EN_COLA("En cola de lanzamiento"),
    EXITOSA("Exitosa"),
    ABORTADA("Abortada"),
    FALLIDA("Fallida");

    private final String texto;

    EstadoMision(String texto) {
        this.texto = texto;
    }

    @Override
    public String toString() {
        return texto;
    }
}
