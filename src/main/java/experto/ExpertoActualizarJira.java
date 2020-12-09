package experto;


import adapter.DriveAdapter;
import adapter.ExcelAdapter;
import adapter.HojaCalculo;
import entity.Jira;
import entity.Reporte;
import services.drive.ServiceDrive;
import services.excel.ServiceExcel;
import services.selenium.ServiceSelenium;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpertoActualizarJira {


    //VARIABLES GLOBALES
    private String adapter;

    private ServiceSelenium selenium;
    private HojaCalculo hojaCalculo;

    private Map< String, List<Jira> > sheets;
    private List<Reporte> reportes;


    //CONSTRUCTOR
    public ExpertoActualizarJira(String adapter) {  // Posibles valores de adapter "Drive" o "Excel"
        this.selenium = new ServiceSelenium();
        this.reportes = new ArrayList<>();
        this.adapter = adapter;

        switch (adapter){
            case "Excel":
                this.hojaCalculo = new ExcelAdapter(new ServiceExcel());
                break;

            case "Drive":
            default:
                this.hojaCalculo = new DriveAdapter(new ServiceDrive());
                break;
        }
    }

    //FUNCIONALIDADES

    public void cargarJiras(){
        this.sheets = this.hojaCalculo.read();
    }

    public void actualizarJiras() {

        // Si no hay jiras que actualizar, sale del metodo
        if (sheets.size() == 0) return;

        // Abrimos navegador y cargamos la pagina de Jira
        this.selenium.ingresarPagina();

        try{
            // --------------------------  PASOS EN LA PAGINA DE JIRA  -----------------------------------------------------------------------------
            this.selenium.loguearse();       // loguearse a la pagina (Usuario y Contraseña)
            this.selenium.esperarPagina(1500, "//h3[contains(@id,\"gadget-10003-title\")]");        // Espera a que la pagina se esta completamente cargada

            sheets.forEach((sheet,jiras) -> {    // Por cada pestaña

                // Creamos un reporte por pestaña
                Reporte reporte = new Reporte(sheet, adapter);

                // Actualiza los atributos de los Jiras que contenga cada pestaña (esta filtrando por estado distinto de cerrado o rechazado)
                for (Jira jira : jiras) {
                    boolean isActualizado = this.selenium.actualizarEstado(jira);   // Retorna true en caso de que se actualizace correctamente, sino false.
                    if (isActualizado) {
                        reporte.setStatus(jira);
                    } else {
                        jiras.remove(jira);
                        reporte.setPerdido(jira);
                    }
                }

                reportes.add(reporte);

        });
        }catch (Exception ex){
            System.out.println("\n" + "ERROR en la clase 'ExpertoActualizarJira' , metodo 'actualizarJiras()'" + "\n");
            ex.printStackTrace();
            System.exit(2);
        }finally {
            // Cerrar la pagina
            this.selenium.cerrarPagina();
        }
    }

    public void guardarJiras() {
        hojaCalculo.save(sheets);
    }

    public void printReporte(){
        for (Reporte reporte: reportes) {
            reporte.printReporte();
        }
    }


}
