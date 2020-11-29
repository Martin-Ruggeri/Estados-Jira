package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reporte {

    // ATRIBUTOS
    private Map< String , List<Integer> > status;
    private String sheet;
    private int jirasActualizados = 0;
    private String adapter;


    // CONSTRUCTOR
    public Reporte(String sheet, String adapter) {
        this.sheet = sheet;
        this.status = new HashMap<>();
        this.adapter = adapter;

        status.put("Perdidos", new ArrayList<>());
        status.put("Otros Estados", new ArrayList<>());
        status.put("Registrada", new ArrayList<>());
        status.put("Aceptada", new ArrayList<>());
        status.put("Pendiente de estimar", new ArrayList<>());
        status.put("Pendiente Estimación", new ArrayList<>());
        status.put("En progreso Funcional", new ArrayList<>());
        status.put("En progreso Desarrollo", new ArrayList<>());
        status.put("En progreso", new ArrayList<>());
        status.put("Resuelta", new ArrayList<>());
        status.put("Cerrada", new ArrayList<>());
        status.put("Rechazada", new ArrayList<>());
        status.put("Pausada", new ArrayList<>());
        status.put("Bloqueada", new ArrayList<>());
        status.put("Devuelta", new ArrayList<>());
        status.put("Pendiente Despliegue", new ArrayList<>());
        status.put("Pendiente Testing", new ArrayList<>());
        status.put("Pendiente Cierre", new ArrayList<>());
        status.put("Pendiente Liberación", new ArrayList<>());

    }


    // GETTERS AND SETTERS

    public void setStatus(Jira jira){
        // Aumenta en 1, el contador de jiras actualizados
        jirasActualizados ++;

        // Insertar la fila del jira en el estado correspondiente (En caso que no coincida con ningun estado lo guarda en 'Otros Estados'). EJ: Aceptada : [5,3]
        switch (adapter) {
            case "Excel":
                status.getOrDefault(jira.getEstado(), status.get("Otros Estados")).add( (jira.getJiraHojaCalculo().getIndexFila() +1) );
                break;

            case "Drive":
            default:
                status.getOrDefault(jira.getEstado(), status.get("Otros Estados")).add(jira.getJiraHojaCalculo().getIndexFila());
                break;
        }
    }

    public void setPerdido(Jira jira){
        switch (adapter) {
            case "Excel":
                status.getOrDefault(jira.getEstado(), status.get("Perdidos")).add( (jira.getJiraHojaCalculo().getIndexFila() +1) );
                break;

            case "Drive":
            default:
                status.getOrDefault(jira.getEstado(), status.get("Perdidos")).add(jira.getJiraHojaCalculo().getIndexFila());
                break;
        }
    }

    public void printReporte(){

        System.out.println("\n" + "Pestaña: " + this.sheet);
        System.out.println("Jiras Actualizados : " + this.jirasActualizados + ".");

        this.status.forEach((state,count) -> { // Por cada estado
             if(count.size() > 0) System.out.println("Jiras " + state + ": " + count.size() + "; " + count.toString());
        });

        System.out.println("\n");

        }


    }

