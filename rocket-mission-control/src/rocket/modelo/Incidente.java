package rocket.modelo;

public class Incidente {

    private final TipoFallo tipo;
    private final Etapa etapa;
    private final String idMision;

    public Incidente(TipoFallo tipo, Etapa etapa, String idMision) {
        this.tipo = tipo;
        this.etapa = etapa;
        this.idMision = idMision;
    }

    public TipoFallo getTipo() {
        return tipo;
    }

    public Etapa getEtapa() {
        return etapa;
    }

    public String getIdMision() {
        return idMision;
    }

    public int getSeveridad() {
        return tipo.getSeveridad();
    }

    @Override
    public String toString() {
        return String.format("Misión %s | %s | etapa: %s | severidad %d",
                idMision, tipo.getDescripcion(), etapa.getNombre(), getSeveridad());
    }
}
