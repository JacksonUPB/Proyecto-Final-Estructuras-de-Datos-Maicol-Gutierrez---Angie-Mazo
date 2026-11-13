package rocket.servicio;

public class ConfigSimulacion {

    private double minimoCombustiblePorcentaje = 30.0;
    private double presionMinima = 50.0;
    private double presionMaxima = 100.0;

    public double getMinimoCombustiblePorcentaje() {
        return minimoCombustiblePorcentaje;
    }

    public void setMinimoCombustiblePorcentaje(double valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException("El porcentaje mínimo debe estar entre 0 y 100");
        }
        this.minimoCombustiblePorcentaje = valor;
    }

    public double getPresionMinima() {
        return presionMinima;
    }

    public double getPresionMaxima() {
        return presionMaxima;
    }

    public void setRangoPresion(double minima, double maxima) {
        if (minima < 0 || maxima <= minima) {
            throw new IllegalArgumentException("El rango de presión no es válido (mínima >= 0 y máxima > mínima)");
        }
        this.presionMinima = minima;
        this.presionMaxima = maxima;
    }

    @Override
    public String toString() {
        return String.format("Combustible mínimo: %.1f%% de la capacidad | Presión permitida: %.1f a %.1f",
                minimoCombustiblePorcentaje, presionMinima, presionMaxima);
    }
}
