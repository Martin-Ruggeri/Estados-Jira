package services.drive;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import entity.Jira;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class ServiceDrive {

    // VARIABLES GLOBALES
    private static Sheets sheetService;
    private static String APPPLICATION_NAME = "Example Sheets and Java";
    private static String SPREADSHEET_ID = "16lwemhPK84LrJT8WDHDCxr0025UIIFXoBRLFmPJZzOk";

    // VARIABLES
    private final int ID = 0;                 // ID               = Columna A
    private final int estado = 7;             // ID               = Columna A


    // CONSTRUCTOR
    public ServiceDrive() { }

    // METODOS
    private static Credential authorize() throws IOException, GeneralSecurityException{
        InputStream in = ServiceDrive.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load( JacksonFactory.getDefaultInstance() , new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");

        return credential;
    }
    private static Sheets getSheetService() throws IOException,GeneralSecurityException{
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),credential)
                .setApplicationName(APPPLICATION_NAME)
                .build();
    }

    private String[] getSheets(){
        String sheet1 = "Jiras Producto";
        String sheet2 = "Jiras Proyecto";

        String[] sheet = { sheet1, sheet2 };

        return sheet;
    }

    public Map< String, List<Jira> > leerSheets(){
        Map< String, List<Jira> > listJiras = new HashMap<>();
        String[] sheets = getSheets();

        for (String sheet : sheets){
            List<Jira> jiras = leerJiras(sheet);
            if (jiras.size() > 0) listJiras.put(sheet, jiras);
        }

        return listJiras;
    }
    private List<Jira> leerJiras(String sheet){

        //VARIABLES
        List<Jira> jiras = new ArrayList<>();
        int filaInicial = 2;        // fila incial 3 del excel
        int maxPrueba = 3;

        String range = "'" + sheet + "'" + "!A:Z";
        List<List<Object>> values = new ArrayList<>();
        for (int i = 1; i <= maxPrueba ; i++) {
            try {
                this.sheetService = getSheetService();

                ValueRange response = this.sheetService.spreadsheets().values().get(SPREADSHEET_ID, range).execute();
                values = response.getValues();
                break;
            } catch (IOException ex) {
                System.out.println("\n" + "Error IOException, Ejecucion :" + i + "\n");
                // ex.printStackTrace();

            } catch (GeneralSecurityException ex) {
                System.out.println("\n" + "Error GeneralSecurityException, Ejecucion :" + i + "\n");
                //ex.printStackTrace();
            }
        }

        // Validar si trae valores, en caso negativo, sale de la funcion retornando una lista vacia
        if (values == null || values.isEmpty()) {
            System.out.println("\n" + "No hay datos para leer del drive , metodo leerJiras() , clase ServiceDrive" + "\n");
            return jiras;
        }

            // Por cada fila del drive traida de la pestaña shet
            for (int f = filaInicial ; f < values.size() ; f++) {
                List<Object> fila = values.get(f);

                //Validaciones
                if (fila.isEmpty())continue;
                if (fila.get(this.ID) == "")continue;
                // Validar que la columna estado exista.  (podria ser vacia)
                if (fila.size() < (estado+1) )continue;
                // Validar que el estado sea distinto de Cerrado o Rechazado
                if (fila.get(this.estado).equals("Cerrada") || fila.get(this.estado).equals("Rechazada")) continue;


                Jira jira = new Jira();

                jira.setID((String) fila.get(this.ID));

                jira.getJiraHojaCalculo().setIndexFila(f+1);

                jiras.add(jira);
            }

        return jiras;
    }

    public void guardarjiras(Map<String, List<Jira>> sheets) {

        sheets.forEach((sheet,jiras) -> {    // Por cada pestaña

            for (Jira jira : jiras) {
                try {

                String range = "'" + sheet + "'" + "!A" + jira.getJiraHojaCalculo().getIndexFila() + ":Z" + jira.getJiraHojaCalculo().getIndexFila();

                ValueRange body = new ValueRange()
                    .setValues(Arrays.asList(
                            Arrays.asList(jira.getID(), calendar_a_string(jira.getFechaCreado()), calendar_a_string(jira.getFechaActualizado()), calendar_a_string(jira.getFechaResuelto()), jira.getNombre(), jira.getPrioridad(), jira.getResolucion(), jira.getEstado())
                    ));


                    UpdateValuesResponse result = sheetService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();
                } catch (IOException e) {
                    System.out.println("\n" + "ERROR en la clase 'ServiceDrive' , metodo 'guardarjiras()'");
                    System.out.println("Al intentar guardar los jiras en el Drive a ocurrido un fallo" + "\n");
                    e.printStackTrace();
                    System.exit(2);
                }
            }

        });

        actualizarCabecera();
    }

    private String calendar_a_string(Calendar fecha){
        try {

            if (fecha == null)return "";

            //VARIABLES
            String dia, mes, anio;

            // Dia
            if (String.valueOf(fecha.get(Calendar.DAY_OF_MONTH)).length() == 2) {
                dia = String.valueOf(fecha.get(Calendar.DAY_OF_MONTH));
            } else {
                dia = "0" + String.valueOf(fecha.get(Calendar.DAY_OF_MONTH));
            }

            // Mes
            if (String.valueOf(fecha.get(Calendar.MONTH) + 1).length() == 2) {
                mes = String.valueOf(fecha.get(Calendar.MONTH) + 1);
            } else {
                mes = "0" + String.valueOf(fecha.get(Calendar.MONTH) + 1);
            }

            // Año
            anio = String.valueOf(fecha.get(Calendar.YEAR));

            return dia + "/" + mes + "/" + anio;

        }catch (Exception ex){
            System.out.println("\n" + "ERROR clase 'ServiceDrive' , metodo 'calendar_a_string()'");
            System.out.println("Error al parsear fecha a String" + "\n");
            ex.printStackTrace();
            return "";
        }
    }
    private  String calendar_a_string_completo(Calendar fecha){
        try {

            if (fecha == null)return "";

            //VARIABLES
            String dia, mes, anio, hora, minutos;

            // Dia
            if (String.valueOf(fecha.get(Calendar.DAY_OF_MONTH)).length() == 2) {
                dia = String.valueOf(fecha.get(Calendar.DAY_OF_MONTH));
            } else {
                dia = "0" + String.valueOf(fecha.get(Calendar.DAY_OF_MONTH));
            }

            // Mes
            if (String.valueOf(fecha.get(Calendar.MONTH) + 1).length() == 2) {
                mes = String.valueOf(fecha.get(Calendar.MONTH) + 1);
            } else {
                mes = "0" + String.valueOf(fecha.get(Calendar.MONTH) + 1);
            }

            // Año
            anio = String.valueOf(fecha.get(Calendar.YEAR));

            // Hora
            if (String.valueOf(fecha.get(Calendar.HOUR_OF_DAY)).length() == 2) {
                hora = String.valueOf(fecha.get(Calendar.HOUR_OF_DAY));
            } else {
                hora = "0" + String.valueOf(fecha.get(Calendar.HOUR_OF_DAY));
            }

            // minutos
            if (String.valueOf(fecha.get(Calendar.MINUTE)).length() == 2) {
                minutos = String.valueOf(fecha.get(Calendar.MINUTE));
            } else {
                minutos = "0" + String.valueOf(fecha.get(Calendar.MINUTE));
            }

            return dia + "/" + mes + "/" + anio + " a las " + hora + ":" + minutos;

        }catch (Exception ex){
            System.out.println("\n" + "ERROR clase 'ServiceDrive' , metodo 'calendar_a_string()'");
            System.out.println("Error al parsear fecha a String" + "\n");
            ex.printStackTrace();
            return "";
        }
    }

    private void actualizarCabecera() {

        String[] sheets = getSheets();

        for (String sheet : sheets) {
            String range = "'" + sheet + "'" + "!A1";
            String descripcion = "Ultima actualizacion: " + calendar_a_string_completo(new GregorianCalendar()) + " hs";

            try {
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(
                                Arrays.asList(descripcion)
                        ));

                UpdateValuesResponse result = sheetService.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();

            } catch (Exception ex) {
                System.out.println("\n" + "ERROR clase 'ServiceDrive' , metodo 'actualizarCabecera()'");
                System.out.println("Error al actualziar cabecera con la fecha actual" + "\n");
                ex.printStackTrace();
            }
        }
    }


}
