package rocket.servicio;

import rocket.modelo.EstadoMision;
import rocket.modelo.Incidente;
import rocket.modelo.Mision;

public class ResultadoSimulacion {

    private final Mision mision;
    private final EstadoMision resultado;
    private final String bitacora;
    private final Incidente incidente;

    public ResultadoSimulacion(Mision mision, EstadoMision resultado, String bitacora, Incidente incidente) {
        this.mision = mision;
        this.resultado = resultado;
        this.bitacora = bitacora;
        this.incidente = incidente;
    }

    public Mision getMision() {
        return mision;
    }

    public EstadoMision getResultado() {
        return resultado;
    }

    public String getBitacora() {
        return bitacora;
    }

    public Incidente getIncidente() {
        return incidente;
    }
}
