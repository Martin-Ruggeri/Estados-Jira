import entity.Reporte;
import experto.ExpertoActualizarJira;


public class Main {

    public static void main(String[] args) {

        String adapter = "Excel";     // Posibles valores de adapter "Drive" o "Excel"

        ExpertoActualizarJira start = new ExpertoActualizarJira(adapter);

        start.cargarJiras();
        start.actualizarJiras();
        start.guardarJiras();
        start.printReporte();

    }
}