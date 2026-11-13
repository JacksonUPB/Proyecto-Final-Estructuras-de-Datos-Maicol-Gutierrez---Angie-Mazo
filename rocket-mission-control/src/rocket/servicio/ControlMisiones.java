package rocket.servicio;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import rocket.estructuras.ColaFIFO;
import rocket.estructuras.ColaPrioridad;
import rocket.estructuras.TablaHash;
import rocket.modelo.Cohete;
import rocket.modelo.EstadoMision;
import rocket.modelo.Incidente;
import rocket.modelo.Mision;
import rocket.modelo.TipoFallo;


public class ControlMisiones {

    private final TablaHash<Cohete> cohetes = new TablaHash<>();
    private final TablaHash<Mision> misiones = new TablaHash<>();
    private final ColaFIFO<Mision> solicitudesPendientes = new ColaFIFO<>();
    private final ColaPrioridad<Incidente> incidentes = new ColaPrioridad<>();
    private final ConfigSimulacion config = new ConfigSimulacion();
    private int consecutivoMision = 0;

    public ConfigSimulacion getConfig() {
        return config;
    }

    // ------------------------------------------------------------------ Cohetes

    private String normalizar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio");
        }
        return codigo.trim().toUpperCase();
    }

    public Cohete registrarCohete(String codigo, String nombre, double capacidadCombustible) {
        String clave = normalizar(codigo);
        if (cohetes.contiene(clave)) {
            throw new IllegalArgumentException("Ya existe un cohete con el código " + clave);
        }
        Cohete cohete = new Cohete(clave, nombre, capacidadCombustible);
        cohetes.poner(clave, cohete);
        return cohete;
    }

    /** Devuelve el cohete o lanza error si no existe. Se puede modificar con sus métodos set. */
    public Cohete consultarCohete(String codigo) {
        String clave = normalizar(codigo);
        Cohete cohete = cohetes.obtener(clave);
        if (cohete == null) {
            throw new IllegalArgumentException("No existe un cohete con el código " + clave);
        }
        return cohete;
    }

    public void eliminarCohete(String codigo) {
        Cohete cohete = consultarCohete(codigo);
        boolean[] tieneMisionesActivas = {false};
        misiones.paraCada(m -> {
            if (m.getCodigoCohete().equals(cohete.getCodigo())
                    && (m.getEstado() == EstadoMision.PROGRAMADA || m.getEstado() == EstadoMision.EN_COLA)) {
                tieneMisionesActivas[0] = true;
            }
        });
        if (tieneMisionesActivas[0]) {
            throw new IllegalStateException("No se puede eliminar: el cohete tiene misiones programadas o en cola");
        }
        cohetes.eliminar(cohete.getCodigo());
    }

    public Cohete[] listarCohetes() {
        Cohete[] lista = new Cohete[cohetes.tamanio()];
        int[] pos = {0};
        cohetes.paraCada(c -> lista[pos[0]++] = c);
        Arrays.sort(lista, Comparator.comparing(Cohete::getCodigo));
        return lista;
    }

    // ----------------------------------------------------------------- Misiones

    public Mision crearMision(String nombre, String codigoCohete, LocalDate fecha, int prioridad) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la misión es obligatorio");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (prioridad < 1 || prioridad > 5) {
            throw new IllegalArgumentException("La prioridad debe estar entre 1 (baja) y 5 (alta)");
        }
        Cohete cohete = consultarCohete(codigoCohete);
        consecutivoMision++;
        String id = String.format("M-%03d", consecutivoMision);
        Mision mision = new Mision(id, nombre.trim(), cohete.getCodigo(), fecha, prioridad);
        misiones.poner(id, mision);
        return mision;
    }

    public Mision consultarMision(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la misión es obligatorio");
        }
        Mision mision = misiones.obtener(id.trim().toUpperCase());
        if (mision == null) {
            throw new IllegalArgumentException("No existe la misión " + id.trim().toUpperCase());
        }
        return mision;
    }

    public Mision[] listarMisiones() {
        Mision[] lista = new Mision[misiones.tamanio()];
        int[] pos = {0};
        misiones.paraCada(m -> lista[pos[0]++] = m);
        Arrays.sort(lista, Comparator.comparing(Mision::getId));
        return lista;
    }

    // ------------------------------------------------------------ Lanzamientos

    /** Agrega la misión al final de la cola FIFO de solicitudes pendientes. */
    public void solicitarLanzamiento(String idMision) {
        Mision mision = consultarMision(idMision);
        if (mision.getEstado() != EstadoMision.PROGRAMADA) {
            throw new IllegalStateException("La misión " + mision.getId() + " no se puede solicitar: estado actual "
                    + mision.getEstado());
        }
        if (cohetes.obtener(mision.getCodigoCohete()) == null) {
            throw new IllegalStateException("El cohete de la misión ya no existe");
        }
        mision.setEstado(EstadoMision.EN_COLA);
        solicitudesPendientes.encolar(mision);
    }

    public Mision[] listarSolicitudesPendientes() {
        Mision[] lista = new Mision[solicitudesPendientes.tamanio()];
        int[] pos = {0};
        solicitudesPendientes.paraCada(m -> lista[pos[0]++] = m);
        return lista;
    }

    public int cantidadPendientes() {
        return solicitudesPendientes.tamanio();
    }

    /**
     * Saca la solicitud que lleva más tiempo en la cola, simula el lanzamiento,
     * actualiza el estado de la misión y registra el incidente si lo hubo.
     *
     * @param falloManual fallo elegido por el usuario, o null para usar solo las condiciones del cohete
     */
    public ResultadoSimulacion procesarSiguiente(TipoFallo falloManual) {
        if (solicitudesPendientes.estaVacia()) {
            throw new IllegalStateException("No hay solicitudes de lanzamiento pendientes");
        }
        Mision mision = solicitudesPendientes.desencolar();
        Cohete cohete = cohetes.obtener(mision.getCodigoCohete());
        ResultadoSimulacion resultado = Simulador.ejecutar(mision, cohete, falloManual, config);
        mision.setEstado(resultado.getResultado());
        if (resultado.getIncidente() != null) {
            incidentes.encolar(resultado.getIncidente(), resultado.getIncidente().getSeveridad());
        }
        return resultado;
    }

    // --------------------------------------------------------------- Incidentes

    /** Historial completo, de mayor a menor severidad (empate: orden de llegada). No altera la cola. */
    public Incidente[] historialIncidentes() {
        ColaPrioridad<Incidente> copia = incidentes.copia();
        Incidente[] lista = new Incidente[copia.tamanio()];
        for (int i = 0; i < lista.length; i++) {
            lista[i] = copia.desencolar();
        }
        return lista;
    }

    public Incidente[] incidentesDeMision(String idMision) {
        String id = consultarMision(idMision).getId();
        Incidente[] todos = historialIncidentes();
        int cantidad = 0;
        for (Incidente i : todos) {
            if (i.getIdMision().equals(id)) {
                cantidad++;
            }
        }
        Incidente[] filtrados = new Incidente[cantidad];
        int pos = 0;
        for (Incidente i : todos) {
            if (i.getIdMision().equals(id)) {
                filtrados[pos++] = i;
            }
        }
        return filtrados;
    }
}
