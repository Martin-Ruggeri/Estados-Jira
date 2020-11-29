package entity;

import java.util.Calendar;

public class Jira {

    // ATRIBUTOS
    private String ID;
    private String nombre;
    private String estado;
    private String resolucion;
    private String prioridad;
    private Calendar fechaCreado;
    private Calendar fechaActualizado;
    private Calendar fechaResuelto;

    // RELACIONES
    private JiraHojaCalculo jiraHojaCalculo;

    //CONSTRUCTOR
    public Jira() {
        this.jiraHojaCalculo = new JiraHojaCalculo();
    }


    // Getters AND Setters
    public String getID() {
        return ID;
    }
    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }
    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getResolucion() {
        return resolucion;
    }
    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public JiraHojaCalculo getJiraHojaCalculo() {
        return jiraHojaCalculo;
    }

    public String getPrioridad() {
        return prioridad;
    }
    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public Calendar getFechaCreado() {
        return fechaCreado;
    }
    public void setFechaCreado(Calendar fechaCreado) {
        this.fechaCreado = fechaCreado;
    }

    public Calendar getFechaActualizado() {
        return fechaActualizado;
    }
    public void setFechaActualizado(Calendar fechaActualizado) {
        this.fechaActualizado = fechaActualizado;
    }

    public Calendar getFechaResuelto() {
        return fechaResuelto;
    }
    public void setFechaResuelto(Calendar fechaResuelto) {
        this.fechaResuelto = fechaResuelto;
    }
}
