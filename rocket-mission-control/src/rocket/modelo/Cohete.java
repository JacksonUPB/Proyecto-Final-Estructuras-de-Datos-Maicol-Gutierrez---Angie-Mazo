package rocket.modelo;


public class Cohete {

    private final String codigo;
    private String nombre;
    private double capacidadCombustible;
    private double nivelCombustible;
    private double presion = 75.0;
    private boolean motorOperativo = true;
    private boolean sistemaElectricoDisponible = true;
    private boolean comunicacionActiva = true;

    public Cohete(String codigo, String nombre, double capacidadCombustible) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del cohete es obligatorio");
        }
        this.codigo = codigo;
        setNombre(nombre);
        setCapacidadCombustible(capacidadCombustible);
        this.nivelCombustible = capacidadCombustible;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del cohete es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public double getCapacidadCombustible() {
        return capacidadCombustible;
    }

    public void setCapacidadCombustible(double capacidadCombustible) {
        if (capacidadCombustible <= 0) {
            throw new IllegalArgumentException("La capacidad de combustible debe ser mayor que 0");
        }
        this.capacidadCombustible = capacidadCombustible;
        if (nivelCombustible > capacidadCombustible) {
            nivelCombustible = capacidadCombustible;
        }
    }

    public double getNivelCombustible() {
        return nivelCombustible;
    }

    public void setNivelCombustible(double nivelCombustible) {
        if (nivelCombustible < 0 || nivelCombustible > capacidadCombustible) {
            throw new IllegalArgumentException(
                    "El nivel de combustible debe estar entre 0 y la capacidad (" + capacidadCombustible + ")");
        }
        this.nivelCombustible = nivelCombustible;
    }

    public double getPresion() {
        return presion;
    }

    public void setPresion(double presion) {
        if (presion < 0) {
            throw new IllegalArgumentException("La presión no puede ser negativa");
        }
        this.presion = presion;
    }

    public boolean isMotorOperativo() {
        return motorOperativo;
    }

    public void setMotorOperativo(boolean motorOperativo) {
        this.motorOperativo = motorOperativo;
    }

    public boolean isSistemaElectricoDisponible() {
        return sistemaElectricoDisponible;
    }

    public void setSistemaElectricoDisponible(boolean sistemaElectricoDisponible) {
        this.sistemaElectricoDisponible = sistemaElectricoDisponible;
    }

    public boolean isComunicacionActiva() {
        return comunicacionActiva;
    }

    public void setComunicacionActiva(boolean comunicacionActiva) {
        this.comunicacionActiva = comunicacionActiva;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | combustible %.1f/%.1f | presión %.1f | motor %s | eléctrico %s | comunicación %s",
                codigo, nombre, nivelCombustible, capacidadCombustible, presion,
                motorOperativo ? "OK" : "NO OPERATIVO",
                sistemaElectricoDisponible ? "OK" : "NO DISPONIBLE",
                comunicacionActiva ? "OK" : "CAÍDA");
    }
}
