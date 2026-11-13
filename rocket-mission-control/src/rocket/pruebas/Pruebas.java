package rocket.pruebas;

import java.time.LocalDate;
import rocket.estructuras.ColaFIFO;
import rocket.estructuras.ColaPrioridad;
import rocket.estructuras.TablaHash;
import rocket.modelo.Cohete;
import rocket.modelo.EstadoMision;
import rocket.modelo.Etapa;
import rocket.modelo.Incidente;
import rocket.modelo.Mision;
import rocket.modelo.TipoFallo;
import rocket.servicio.ControlMisiones;
import rocket.servicio.ResultadoSimulacion;


public class Pruebas {

    private static int total = 0;
    private static int fallos = 0;

    public static void main(String[] args) {
        pruebasColaFIFO();
        pruebasColaPrioridad();
        pruebasTablaHash();
        pruebasSimulacion();
        pruebasServicio();

        System.out.println();
        System.out.println("Pruebas ejecutadas: " + total + " | Fallidas: " + fallos);
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ------------------------------------------------------------------ helpers

    private static void verificar(String nombre, boolean condicion) {
        total++;
        if (!condicion) {
            fallos++;
        }
        System.out.println((condicion ? "  OK    " : "  FALLO ") + nombre);
    }

    private static boolean lanza(Runnable accion, Class<? extends Exception> tipo) {
        try {
            accion.run();
            return false;
        } catch (Exception e) {
            return tipo.isInstance(e);
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
    }

    // --------------------------------------------------------------- Cola FIFO

    private static void pruebasColaFIFO() {
        titulo("Cola FIFO");

        ColaFIFO<String> cola = new ColaFIFO<>();
        verificar("Cola nueva está vacía y tiene tamaño 0", cola.estaVacia() && cola.tamanio() == 0);

        cola.encolar("A");
        cola.encolar("B");
        cola.encolar("C");
        verificar("Tamaño 3 después de encolar 3", cola.tamanio() == 3);
        verificar("verFrente devuelve el primero sin sacarlo", cola.verFrente().equals("A") && cola.tamanio() == 3);
        verificar("Sale en orden de llegada (A, B, C)",
                cola.desencolar().equals("A") && cola.desencolar().equals("B") && cola.desencolar().equals("C"));
        verificar("Queda vacía al sacar todo", cola.estaVacia());

        verificar("Desencolar una cola vacía lanza IllegalStateException",
                lanza(cola::desencolar, IllegalStateException.class));
        verificar("verFrente en cola vacía lanza IllegalStateException",
                lanza(cola::verFrente, IllegalStateException.class));
        verificar("Encolar null lanza IllegalArgumentException",
                lanza(() -> cola.encolar(null), IllegalArgumentException.class));

        cola.encolar("X");
        cola.desencolar();
        cola.encolar("Y");
        verificar("Se puede reutilizar después de vaciarse", cola.verFrente().equals("Y") && cola.tamanio() == 1);

        ColaFIFO<Integer> grande = new ColaFIFO<>();
        for (int i = 0; i < 10000; i++) {
            grande.encolar(i);
        }
        boolean ordenCorrecto = true;
        for (int i = 0; i < 10000; i++) {
            if (grande.desencolar() != i) {
                ordenCorrecto = false;
            }
        }
        verificar("10000 elementos salen en el orden exacto de llegada", ordenCorrecto && grande.estaVacia());
    }

    // ------------------------------------------------------------ Cola prioridad

    private static void pruebasColaPrioridad() {
        titulo("Cola de prioridad");

        ColaPrioridad<String> cola = new ColaPrioridad<>();
        verificar("Cola nueva está vacía", cola.estaVacia());
        verificar("Desencolar vacía lanza IllegalStateException",
                lanza(cola::desencolar, IllegalStateException.class));

        cola.encolar("baja", 1);
        cola.encolar("critica", 5);
        cola.encolar("media", 3);
        verificar("Sale primero la mayor severidad", cola.desencolar().equals("critica"));
        verificar("Luego la siguiente", cola.desencolar().equals("media"));
        verificar("Y al final la menor", cola.desencolar().equals("baja") && cola.estaVacia());

        ColaPrioridad<String> empates = new ColaPrioridad<>();
        empates.encolar("primero", 3);
        empates.encolar("segundo", 3);
        empates.encolar("tercero", 3);
        verificar("Con empate se conserva el orden de llegada",
                empates.desencolar().equals("primero")
                        && empates.desencolar().equals("segundo")
                        && empates.desencolar().equals("tercero"));

        ColaPrioridad<String> mezcla = new ColaPrioridad<>();
        mezcla.encolar("a3", 3);
        mezcla.encolar("b5", 5);
        mezcla.encolar("c3", 3);
        mezcla.encolar("d5", 5);
        mezcla.encolar("e1", 1);
        String orden = mezcla.desencolar() + mezcla.desencolar() + mezcla.desencolar()
                + mezcla.desencolar() + mezcla.desencolar();
        verificar("Mezcla de prioridades y empates (b5 d5 a3 c3 e1)", orden.equals("b5d5a3c3e1"));

        ColaPrioridad<Integer> grande = new ColaPrioridad<>();
        for (int i = 0; i < 1000; i++) {
            grande.encolar(i, i % 5);
        }
        boolean noCreciente = true;
        int anterior = Integer.MAX_VALUE;
        int ultimoDeGrupo[] = new int[5];
        for (int g = 0; g < 5; g++) {
            ultimoDeGrupo[g] = -1;
        }
        boolean tieOk = true;
        while (!grande.estaVacia()) {
            int valor = grande.desencolar();
            int prioridad = valor % 5;
            if (prioridad > anterior) {
                noCreciente = false;
            }
            anterior = prioridad;
            if (valor < ultimoDeGrupo[prioridad]) {
                tieOk = false;
            }
            ultimoDeGrupo[prioridad] = valor;
        }
        verificar("1000 elementos (crece más allá de la capacidad inicial) salen ordenados por severidad", noCreciente);
        verificar("En esos 1000, dentro de cada severidad se respeta el orden de llegada", tieOk);

        ColaPrioridad<String> original = new ColaPrioridad<>();
        original.encolar("x", 2);
        original.encolar("y", 4);
        ColaPrioridad<String> copia = original.copia();
        copia.desencolar();
        verificar("Desencolar la copia no altera la cola original", original.tamanio() == 2 && copia.tamanio() == 1);
    }

    // --------------------------------------------------------------- Tabla hash

    private static void pruebasTablaHash() {
        titulo("Tabla hash");

        TablaHash<String> tabla = new TablaHash<>();
        verificar("Tabla nueva está vacía", tabla.estaVacia() && tabla.tamanio() == 0);
        verificar("Buscar clave inexistente devuelve null", tabla.obtener("NOPE") == null);
        verificar("contiene en tabla vacía es false", !tabla.contiene("NOPE"));

        verificar("Insertar clave nueva devuelve true", tabla.poner("ATL-01", "Atlas"));
        verificar("Reemplazar clave existente devuelve false", !tabla.poner("ATL-01", "Atlas II"));
        verificar("Reemplazar no duplica la clave", tabla.tamanio() == 1 && tabla.obtener("ATL-01").equals("Atlas II"));

        verificar("Eliminar devuelve el valor", "Atlas II".equals(tabla.eliminar("ATL-01")));
        verificar("Eliminar clave inexistente devuelve null", tabla.eliminar("ATL-01") == null);
        verificar("Tabla vacía después de eliminar", tabla.estaVacia());
        verificar("Clave nula lanza IllegalArgumentException",
                lanza(() -> tabla.obtener(null), IllegalArgumentException.class));

        // "Aa" y "BB" producen el mismo valor en la función hash polinómica base 31: colisión garantizada.
        TablaHash<String> colision = new TablaHash<>();
        colision.poner("Aa", "uno");
        colision.poner("BB", "dos");
        verificar("Dos claves que colisionan conviven y se encuentran bien",
                colision.obtener("Aa").equals("uno") && colision.obtener("BB").equals("dos")
                        && colision.longitudMaximaCadena() >= 2);
        colision.eliminar("Aa");
        verificar("Eliminar una de las que colisionan no afecta a la otra",
                colision.obtener("Aa") == null && colision.obtener("BB").equals("dos"));

        TablaHash<Integer> grande = new TablaHash<>();
        int capacidadInicial = grande.capacidad();
        for (int i = 0; i < 5000; i++) {
            grande.poner("COD-" + i, i);
        }
        boolean todas = true;
        for (int i = 0; i < 5000; i++) {
            Integer v = grande.obtener("COD-" + i);
            if (v == null || v != i) {
                todas = false;
            }
        }
        verificar("5000 claves: todas se encuentran tras varios redimensionamientos",
                todas && grande.tamanio() == 5000 && grande.capacidad() > capacidadInicial);
        int[] contador = {0};
        grande.paraCada(v -> contador[0]++);
        verificar("paraCada recorre exactamente todas las claves", contador[0] == 5000);
    }

    // ---------------------------------------------------------------- Simulación

    private static ControlMisiones sistemaConCohete(String codigo) {
        ControlMisiones s = new ControlMisiones();
        s.registrarCohete(codigo, "Prueba", 1000);
        return s;
    }

    private static ResultadoSimulacion lanzar(ControlMisiones s, String codigo, TipoFallo manual) {
        Mision m = s.crearMision("Misión de prueba", codigo, LocalDate.now().plusDays(1), 3);
        s.solicitarLanzamiento(m.getId());
        return s.procesarSiguiente(manual);
    }

    private static void pruebasSimulacion() {
        titulo("Simulación y reglas de resultado");

        ControlMisiones s = sistemaConCohete("T-1");
        ResultadoSimulacion r = lanzar(s, "T-1", null);
        verificar("Sin problemas: lanzamiento EXITOSO y sin incidente",
                r.getResultado() == EstadoMision.EXITOSA && r.getIncidente() == null);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setNivelCombustible(100);
        r = lanzar(s, "T-1", null);
        verificar("Combustible bajo el mínimo: ABORTADA en Preparación",
                r.getResultado() == EstadoMision.ABORTADA
                        && r.getIncidente().getTipo() == TipoFallo.COMBUSTIBLE_INSUFICIENTE
                        && r.getIncidente().getEtapa() == Etapa.PREPARACION);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setNivelCombustible(300);
        r = lanzar(s, "T-1", null);
        verificar("Combustible exactamente en el mínimo (30%) NO es error (caso límite)",
                r.getResultado() == EstadoMision.EXITOSA);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setPresion(120);
        r = lanzar(s, "T-1", null);
        verificar("Presión fuera de rango: ABORTADA en Cuenta regresiva",
                r.getResultado() == EstadoMision.ABORTADA
                        && r.getIncidente().getTipo() == TipoFallo.PRESION_FUERA_DE_RANGO
                        && r.getIncidente().getEtapa() == Etapa.CUENTA_REGRESIVA);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setPresion(100);
        r = lanzar(s, "T-1", null);
        verificar("Presión exactamente en el límite superior NO es error (caso límite)",
                r.getResultado() == EstadoMision.EXITOSA);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setMotorOperativo(false);
        r = lanzar(s, "T-1", null);
        verificar("Motor no operativo: FALLIDA en Despegue",
                r.getResultado() == EstadoMision.FALLIDA
                        && r.getIncidente().getTipo() == TipoFallo.FALLO_MOTOR
                        && r.getIncidente().getEtapa() == Etapa.DESPEGUE);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setSistemaElectricoDisponible(false);
        r = lanzar(s, "T-1", null);
        verificar("Sistema eléctrico no disponible: FALLIDA en Despegue",
                r.getResultado() == EstadoMision.FALLIDA
                        && r.getIncidente().getTipo() == TipoFallo.FALLA_ELECTRICA);

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setComunicacionActiva(false);
        r = lanzar(s, "T-1", null);
        verificar("Comunicación caída: FALLIDA en Ascenso",
                r.getResultado() == EstadoMision.FALLIDA
                        && r.getIncidente().getTipo() == TipoFallo.PERDIDA_COMUNICACION
                        && r.getIncidente().getEtapa() == Etapa.ASCENSO);

        for (TipoFallo tipo : TipoFallo.values()) {
            s = sistemaConCohete("T-1");
            r = lanzar(s, "T-1", tipo);
            EstadoMision esperado = tipo.getEtapa().despegueIniciado() ? EstadoMision.FALLIDA : EstadoMision.ABORTADA;
            verificar("Fallo manual '" + tipo.getDescripcion() + "' -> " + esperado,
                    r.getResultado() == esperado && r.getIncidente().getTipo() == tipo);
        }

        s = sistemaConCohete("T-1");
        s.consultarCohete("T-1").setNivelCombustible(100);
        s.consultarCohete("T-1").setMotorOperativo(false);
        r = lanzar(s, "T-1", null);
        verificar("Dos problemas a la vez: se detiene en el primero (antes del despegue -> ABORTADA)",
                r.getResultado() == EstadoMision.ABORTADA && r.getIncidente().getTipo() == TipoFallo.COMBUSTIBLE_INSUFICIENTE);
    }

    // ------------------------------------------------------------------ Servicio

    private static void pruebasServicio() {
        titulo("Servicio: cohetes, misiones, cola de lanzamientos e incidentes");

        ControlMisiones s = new ControlMisiones();
        s.registrarCohete("atl-01", "Atlas", 1000);
        verificar("El código se normaliza a mayúsculas y se encuentra por código",
                s.consultarCohete("ATL-01").getNombre().equals("Atlas"));
        verificar("Código duplicado se rechaza",
                lanza(() -> s.registrarCohete("ATL-01", "Otro", 500), IllegalArgumentException.class));
        verificar("Capacidad 0 se rechaza",
                lanza(() -> s.registrarCohete("X-1", "Malo", 0), IllegalArgumentException.class));
        verificar("Nombre vacío se rechaza",
                lanza(() -> s.registrarCohete("X-1", " ", 100), IllegalArgumentException.class));
        verificar("Consultar un código inexistente da error",
                lanza(() -> s.consultarCohete("ZZZ"), IllegalArgumentException.class));

        s.consultarCohete("ATL-01").setNombre("Atlas V");
        s.consultarCohete("ATL-01").setCapacidadCombustible(1500);
        verificar("Actualizar nombre y capacidad",
                s.consultarCohete("ATL-01").getNombre().equals("Atlas V")
                        && s.consultarCohete("ATL-01").getCapacidadCombustible() == 1500);

        verificar("Misión con cohete inexistente se rechaza",
                lanza(() -> s.crearMision("M", "NOPE", LocalDate.now(), 3), IllegalArgumentException.class));
        verificar("Prioridad fuera de 1 a 5 se rechaza",
                lanza(() -> s.crearMision("M", "ATL-01", LocalDate.now(), 6), IllegalArgumentException.class));
        verificar("Procesar con la cola vacía da error",
                lanza(() -> s.procesarSiguiente(null), IllegalStateException.class));

        Mision m1 = s.crearMision("Primera", "ATL-01", LocalDate.now().plusDays(5), 1);
        Mision m2 = s.crearMision("Segunda", "ATL-01", LocalDate.now().plusDays(6), 5);
        Mision m3 = s.crearMision("Tercera", "ATL-01", LocalDate.now().plusDays(7), 3);
        verificar("Misión nueva queda PROGRAMADA", m1.getEstado() == EstadoMision.PROGRAMADA);
        verificar("No se puede eliminar un cohete con misiones programadas",
                lanza(() -> s.eliminarCohete("ATL-01"), IllegalStateException.class));

        s.solicitarLanzamiento(m1.getId());
        s.solicitarLanzamiento(m2.getId());
        s.solicitarLanzamiento(m3.getId());
        verificar("Misión solicitada queda EN_COLA", m1.getEstado() == EstadoMision.EN_COLA);
        verificar("No se puede solicitar dos veces la misma misión",
                lanza(() -> s.solicitarLanzamiento(m1.getId()), IllegalStateException.class));
        verificar("Hay 3 solicitudes pendientes", s.cantidadPendientes() == 3);

        ResultadoSimulacion r1 = s.procesarSiguiente(null);
        verificar("Se procesa primero la que llegó primero (FIFO), sin importar la prioridad",
                r1.getMision().getId().equals(m1.getId()));
        ResultadoSimulacion r2 = s.procesarSiguiente(TipoFallo.PERDIDA_COMUNICACION);
        ResultadoSimulacion r3 = s.procesarSiguiente(TipoFallo.FALLO_MOTOR);
        verificar("Segunda y tercera en orden de llegada",
                r2.getMision().getId().equals(m2.getId()) && r3.getMision().getId().equals(m3.getId()));
        verificar("Estados finales guardados en las misiones",
                m1.getEstado() == EstadoMision.EXITOSA && m2.getEstado() == EstadoMision.FALLIDA
                        && m3.getEstado() == EstadoMision.FALLIDA);

        Incidente[] historial = s.historialIncidentes();
        verificar("El historial tiene 2 incidentes", historial.length == 2);
        verificar("El historial va de mayor a menor severidad (motor 5 antes que comunicación 2)",
                historial[0].getTipo() == TipoFallo.FALLO_MOTOR && historial[1].getTipo() == TipoFallo.PERDIDA_COMUNICACION);
        verificar("Consultar el historial no lo vacía", s.historialIncidentes().length == 2);
        verificar("Incidentes de una misión concreta",
                s.incidentesDeMision(m2.getId()).length == 1
                        && s.incidentesDeMision(m2.getId())[0].getTipo() == TipoFallo.PERDIDA_COMUNICACION
                        && s.incidentesDeMision(m1.getId()).length == 0);

        // Empate de severidad: se conserva el orden de llegada.
        ControlMisiones t = sistemaConCohete("E-1");
        Mision a = t.crearMision("A", "E-1", LocalDate.now(), 1);
        Mision b = t.crearMision("B", "E-1", LocalDate.now(), 1);
        t.solicitarLanzamiento(a.getId());
        t.solicitarLanzamiento(b.getId());
        t.procesarSiguiente(TipoFallo.COMBUSTIBLE_INSUFICIENTE);
        t.procesarSiguiente(TipoFallo.PRESION_FUERA_DE_RANGO);
        Incidente[] empate = t.historialIncidentes();
        verificar("Incidentes con la misma severidad (3) salen por orden de llegada",
                empate[0].getIdMision().equals(a.getId()) && empate[1].getIdMision().equals(b.getId()));

        // Eliminar cohete cuando ya no tiene misiones activas.
        s.eliminarCohete("ATL-01");
        verificar("Con todas las misiones terminadas, el cohete se puede eliminar",
                lanza(() -> s.consultarCohete("ATL-01"), IllegalArgumentException.class));
    }
}
