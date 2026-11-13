package rocket.servicio;

import rocket.modelo.Cohete;
import rocket.modelo.Etapa;
import rocket.modelo.EstadoMision;
import rocket.modelo.Incidente;
import rocket.modelo.Mision;
import rocket.modelo.TipoFallo;


public class Simulador {

    private Simulador() {
    }

    /**
     * @param falloManual fallo forzado por el usuario (se produce al llegar a su etapa), o null
     */
    public static ResultadoSimulacion ejecutar(Mision mision, Cohete cohete, TipoFallo falloManual,
                                               ConfigSimulacion config) {
        StringBuilder bitacora = new StringBuilder();
        bitacora.append("Simulando misión ").append(mision.getId()).append(" - ")
                .append(mision.getNombre()).append(" (cohete ").append(cohete.getCodigo()).append(")\n");

        Etapa[] etapas = {Etapa.PREPARACION, Etapa.CUENTA_REGRESIVA, Etapa.DESPEGUE, Etapa.ASCENSO};
        for (Etapa etapa : etapas) {
            TipoFallo fallo = (falloManual != null && falloManual.getEtapa() == etapa)
                    ? falloManual
                    : detectarFallo(etapa, cohete, config);
            if (fallo != null) {
                bitacora.append("  [").append(etapa.getNombre()).append("] FALLO: ")
                        .append(fallo.getDescripcion()).append('\n');
                EstadoMision estado = etapa.despegueIniciado() ? EstadoMision.FALLIDA : EstadoMision.ABORTADA;
                return new ResultadoSimulacion(mision, estado, bitacora.toString(),
                        new Incidente(fallo, etapa, mision.getId()));
            }
            bitacora.append("  [").append(etapa.getNombre()).append("] OK: ")
                    .append(etapa.getMensajeOk()).append('\n');
        }
        bitacora.append("  [").append(Etapa.RESULTADO.getNombre()).append("] ")
                .append(Etapa.RESULTADO.getMensajeOk()).append('\n');
        return new ResultadoSimulacion(mision, EstadoMision.EXITOSA, bitacora.toString(), null);
    }

    private static TipoFallo detectarFallo(Etapa etapa, Cohete cohete, ConfigSimulacion config) {
        switch (etapa) {
            case PREPARACION:
                double minimo = cohete.getCapacidadCombustible() * config.getMinimoCombustiblePorcentaje() / 100.0;
                if (cohete.getNivelCombustible() < minimo) {
                    return TipoFallo.COMBUSTIBLE_INSUFICIENTE;
                }
                return null;
            case CUENTA_REGRESIVA:
                if (cohete.getPresion() < config.getPresionMinima() || cohete.getPresion() > config.getPresionMaxima()) {
                    return TipoFallo.PRESION_FUERA_DE_RANGO;
                }
                return null;
            case DESPEGUE:
                if (!cohete.isMotorOperativo()) {
                    return TipoFallo.FALLO_MOTOR;
                }
                if (!cohete.isSistemaElectricoDisponible()) {
                    return TipoFallo.FALLA_ELECTRICA;
                }
                return null;
            case ASCENSO:
                return cohete.isComunicacionActiva() ? null : TipoFallo.PERDIDA_COMUNICACION;
            default:
                return null;
        }
    }
}
