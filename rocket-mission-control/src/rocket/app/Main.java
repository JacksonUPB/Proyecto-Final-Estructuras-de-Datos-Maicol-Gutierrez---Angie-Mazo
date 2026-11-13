package rocket.app;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import rocket.modelo.Cohete;
import rocket.modelo.Incidente;
import rocket.modelo.Mision;
import rocket.modelo.TipoFallo;
import rocket.servicio.ControlMisiones;
import rocket.servicio.ResultadoSimulacion;

public class Main {

    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final ControlMisiones SISTEMA = new ControlMisiones();

    public static void main(String[] args) {
        boolean salir = false;
        while (!salir) {
            System.out.println();
            System.out.println("=== ROCKET MISSION CONTROL ===");
            System.out.println("1. Cohetes");
            System.out.println("2. Misiones");
            System.out.println("3. Lanzamientos y simulación");
            System.out.println("4. Incidentes");
            System.out.println("5. Configuración de reglas");
            System.out.println("9. Cargar datos de ejemplo");
            System.out.println("0. Salir");
            switch (leerEntero("Opción: ")) {
                case 1: menuCohetes(); break;
                case 2: menuMisiones(); break;
                case 3: menuLanzamientos(); break;
                case 4: menuIncidentes(); break;
                case 5: menuConfiguracion(); break;
                case 9: cargarEjemplos(); break;
                case 0: salir = true; break;
                default: System.out.println("Opción no válida");
            }
        }
        System.out.println("Hasta luego.");
    }

    // ------------------------------------------------------------------ Cohetes

    private static void menuCohetes() {
        System.out.println();
        System.out.println("--- Cohetes ---");
        System.out.println("1. Registrar  2. Consultar  3. Listar  4. Actualizar  5. Eliminar  0. Volver");
        int opcion = leerEntero("Opción: ");
        try {
            switch (opcion) {
                case 1: {
                    String codigo = leer("Código único: ");
                    String nombre = leer("Nombre: ");
                    double capacidad = leerDecimal("Capacidad de combustible: ");
                    Cohete c = SISTEMA.registrarCohete(codigo, nombre, capacidad);
                    System.out.println("Cohete registrado: " + c);
                    break;
                }
                case 2:
                    System.out.println(SISTEMA.consultarCohete(leer("Código: ")));
                    break;
                case 3: {
                    Cohete[] lista = SISTEMA.listarCohetes();
                    if (lista.length == 0) {
                        System.out.println("No hay cohetes registrados");
                    }
                    for (Cohete c : lista) {
                        System.out.println(c);
                    }
                    break;
                }
                case 4:
                    actualizarCohete();
                    break;
                case 5:
                    SISTEMA.eliminarCohete(leer("Código a eliminar: "));
                    System.out.println("Cohete eliminado");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void actualizarCohete() {
        Cohete c = SISTEMA.consultarCohete(leer("Código del cohete a actualizar: "));
        System.out.println("Actual: " + c);
        System.out.println("1. Nombre  2. Capacidad de combustible  3. Nivel de combustible  4. Presión");
        System.out.println("5. Motor operativo (sí/no)  6. Sistema eléctrico (sí/no)  7. Comunicación (sí/no)");
        switch (leerEntero("Campo a cambiar: ")) {
            case 1: c.setNombre(leer("Nuevo nombre: ")); break;
            case 2: c.setCapacidadCombustible(leerDecimal("Nueva capacidad: ")); break;
            case 3: c.setNivelCombustible(leerDecimal("Nuevo nivel: ")); break;
            case 4: c.setPresion(leerDecimal("Nueva presión: ")); break;
            case 5: c.setMotorOperativo(leerSiNo("¿Motor operativo? (s/n): ")); break;
            case 6: c.setSistemaElectricoDisponible(leerSiNo("¿Sistema eléctrico disponible? (s/n): ")); break;
            case 7: c.setComunicacionActiva(leerSiNo("¿Comunicación activa? (s/n): ")); break;
            default: System.out.println("Opción no válida"); return;
        }
        System.out.println("Actualizado: " + c);
    }

    // ----------------------------------------------------------------- Misiones

    private static void menuMisiones() {
        System.out.println();
        System.out.println("--- Misiones ---");
        System.out.println("1. Crear  2. Consultar estado  3. Listar  0. Volver");
        int opcion = leerEntero("Opción: ");
        try {
            switch (opcion) {
                case 1: {
                    String nombre = leer("Nombre de la misión: ");
                    String codigo = leer("Código del cohete: ");
                    LocalDate fecha = leerFecha("Fecha (aaaa-mm-dd): ");
                    int prioridad = leerEntero("Prioridad (1 baja a 5 alta): ");
                    Mision m = SISTEMA.crearMision(nombre, codigo, fecha, prioridad);
                    System.out.println("Misión creada: " + m);
                    break;
                }
                case 2:
                    System.out.println(SISTEMA.consultarMision(leer("Id de la misión (ej. M-001): ")));
                    break;
                case 3: {
                    Mision[] lista = SISTEMA.listarMisiones();
                    if (lista.length == 0) {
                        System.out.println("No hay misiones creadas");
                    }
                    for (Mision m : lista) {
                        System.out.println(m);
                    }
                    break;
                }
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------- Lanzamientos

    private static void menuLanzamientos() {
        System.out.println();
        System.out.println("--- Lanzamientos y simulación ---");
        System.out.println("1. Solicitar lanzamiento de una misión");
        System.out.println("2. Ver solicitudes pendientes");
        System.out.println("3. Procesar siguiente solicitud (condiciones del cohete)");
        System.out.println("4. Procesar siguiente solicitud eligiendo un fallo manual");
        System.out.println("0. Volver");
        int opcion = leerEntero("Opción: ");
        try {
            switch (opcion) {
                case 1:
                    SISTEMA.solicitarLanzamiento(leer("Id de la misión: "));
                    System.out.println("Solicitud agregada al final de la cola");
                    break;
                case 2: {
                    Mision[] lista = SISTEMA.listarSolicitudesPendientes();
                    if (lista.length == 0) {
                        System.out.println("No hay solicitudes pendientes");
                    }
                    for (int i = 0; i < lista.length; i++) {
                        System.out.println((i + 1) + ". " + lista[i]);
                    }
                    break;
                }
                case 3:
                    mostrarResultado(SISTEMA.procesarSiguiente(null));
                    break;
                case 4: {
                    TipoFallo fallo = elegirFallo();
                    mostrarResultado(SISTEMA.procesarSiguiente(fallo));
                    break;
                }
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static TipoFallo elegirFallo() {
        TipoFallo[] fallos = TipoFallo.values();
        for (int i = 0; i < fallos.length; i++) {
            System.out.println((i + 1) + ". " + fallos[i].getDescripcion()
                    + " (se detecta en: " + fallos[i].getEtapa().getNombre() + ")");
        }
        System.out.println("0. Ninguno");
        int n = leerEntero("Fallo a simular: ");
        if (n < 0 || n > fallos.length) {
            throw new IllegalArgumentException("Fallo no válido");
        }
        return n == 0 ? null : fallos[n - 1];
    }

    private static void mostrarResultado(ResultadoSimulacion r) {
        System.out.println();
        System.out.print(r.getBitacora());
        System.out.println("Estado final de la misión " + r.getMision().getId() + ": " + r.getResultado());
        if (r.getIncidente() != null) {
            System.out.println("Incidente registrado: " + r.getIncidente());
        }
    }

    // --------------------------------------------------------------- Incidentes

    private static void menuIncidentes() {
        System.out.println();
        System.out.println("--- Incidentes ---");
        System.out.println("1. Historial completo (de mayor a menor severidad)");
        System.out.println("2. Historial de una misión");
        System.out.println("0. Volver");
        int opcion = leerEntero("Opción: ");
        try {
            switch (opcion) {
                case 1:
                    imprimirIncidentes(SISTEMA.historialIncidentes());
                    break;
                case 2:
                    imprimirIncidentes(SISTEMA.incidentesDeMision(leer("Id de la misión: ")));
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void imprimirIncidentes(Incidente[] lista) {
        if (lista.length == 0) {
            System.out.println("No hay incidentes registrados");
        }
        for (int i = 0; i < lista.length; i++) {
            System.out.println((i + 1) + ". " + lista[i]);
        }
    }

    // ------------------------------------------------------------ Configuración

    private static void menuConfiguracion() {
        System.out.println();
        System.out.println("--- Configuración de reglas ---");
        System.out.println(SISTEMA.getConfig());
        System.out.println("1. Cambiar combustible mínimo (%)  2. Cambiar rango de presión  0. Volver");
        int opcion = leerEntero("Opción: ");
        try {
            switch (opcion) {
                case 1:
                    SISTEMA.getConfig().setMinimoCombustiblePorcentaje(leerDecimal("Nuevo porcentaje mínimo: "));
                    break;
                case 2: {
                    double min = leerDecimal("Presión mínima: ");
                    double max = leerDecimal("Presión máxima: ");
                    SISTEMA.getConfig().setRangoPresion(min, max);
                    break;
                }
                case 0:
                    return;
                default:
                    System.out.println("Opción no válida");
                    return;
            }
            System.out.println("Configuración actual: " + SISTEMA.getConfig());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ Datos ejemplo

    private static void cargarEjemplos() {
        try {
            SISTEMA.registrarCohete("ATL-01", "Atlas", 1000);
            Cohete falcon = SISTEMA.registrarCohete("FAL-02", "Falcon", 800);
            falcon.setNivelCombustible(100);
            Cohete orion = SISTEMA.registrarCohete("ORI-03", "Orion", 1200);
            orion.setMotorOperativo(false);
            Mision m1 = SISTEMA.crearMision("Satélite alfa", "ATL-01", LocalDate.now().plusDays(10), 3);
            Mision m2 = SISTEMA.crearMision("Satélite beta", "FAL-02", LocalDate.now().plusDays(20), 5);
            Mision m3 = SISTEMA.crearMision("Sonda gamma", "ORI-03", LocalDate.now().plusDays(30), 1);
            SISTEMA.solicitarLanzamiento(m1.getId());
            SISTEMA.solicitarLanzamiento(m2.getId());
            SISTEMA.solicitarLanzamiento(m3.getId());
            System.out.println("Datos de ejemplo cargados: 3 cohetes, 3 misiones y 3 solicitudes en cola.");
            System.out.println("Atlas debería salir exitoso, Falcon abortado (poco combustible) y Orion fallido (motor).");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("No se pudieron cargar los datos de ejemplo: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ Lectura

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        if (!ENTRADA.hasNextLine()) {
            System.out.println();
            System.out.println("Fin de la entrada. Saliendo.");
            System.exit(0);
        }
        return ENTRADA.nextLine().trim();
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leer(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número entero");
            }
        }
    }

    private static double leerDecimal(String mensaje) {
        while (true) {
            String texto = leer(mensaje).replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("Escribe un número válido");
            }
        }
    }

    private static boolean leerSiNo(String mensaje) {
        while (true) {
            String texto = leer(mensaje).toLowerCase();
            if (texto.equals("s") || texto.equals("si") || texto.equals("sí")) {
                return true;
            }
            if (texto.equals("n") || texto.equals("no")) {
                return false;
            }
            System.out.println("Responde s o n");
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            String texto = leer(mensaje);
            try {
                return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de fecha no válido, usa aaaa-mm-dd");
            }
        }
    }
}
