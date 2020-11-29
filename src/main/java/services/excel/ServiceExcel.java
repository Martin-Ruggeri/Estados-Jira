package services.excel;



import entity.Jira;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;


import java.io.*;
import java.util.*;


public class ServiceExcel {

    // VARIABLES GLOBALES EXCEL
    public FileInputStream file;
    public XSSFWorkbook excel;
    List<XSSFSheet> sheets = new ArrayList<>();

    public String ubicacionExcel = "C:\\Users\\marugger\\Google Drive\\Everis\\report jiras.xlsx";

    // VARIABLES
    private final int ID = 0;                 // ID               = Columna A
    private final int fechaCreado = 1;        // fechaCreado      = Columna B
    private final int fechaActualizado = 2;   // fechaActualizado = Columna C
    private final int fechaResuelto = 3;      // fechaResuelto    = Columna D
    private final int nombre = 4;             // nombre           = Columna E
    private final int prioridad = 5;          // prioridad        = Columna F
    private final int resolucion = 6;         // resolucion       = Columna G
    private final int estado = 7;             // estado           = Columna H

    // CONSTRUCTOR
    public ServiceExcel(){}


    // FUNCIONALIDADES

    public Map< String, List<Jira> > leerSheets(){
        Map< String, List<Jira> > listJiras = new HashMap<>();
        cargarArchivo();

        for (XSSFSheet sheet : sheets) {
            List<Jira> jiras = leerGiras(sheet);
            if (jiras.size() > 0) listJiras.put(sheet.toString() , jiras);
        }

        return listJiras;
    }

    private List<XSSFSheet> cargarArchivo(){
        try {
            // Abrir o Leer el archivo
            this.file = new FileInputStream(new File(ubicacionExcel));

            // Extraer la informacion del archivo
            this.excel = new XSSFWorkbook(file);

            // Obtener las pestañas
            for (int i = 0; i < excel.getNumberOfSheets(); i++) {
                sheets.add(excel.getSheetAt(i));
            }

        }catch (FileNotFoundException ex_fileNE) {
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'cargarArchivo()'");
            System.out.println("No se puede abrir el excel: '" + ubicacionExcel + "', verificar la ubicacionExcel" + "\n");
            ex_fileNE.printStackTrace();
            System.exit(3);
        }catch (IOException ex_excelF) {
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'cargarArchivo()'");
            System.out.println("Error en new XSSFWorkbook(file)" + "\n");
            ex_excelF.printStackTrace();
            System.exit(3);
        }catch (IllegalArgumentException ex_sheet){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'cargarArchivo()'");
            System.out.println("Error en cargar la pestaña del excel" + "\n");
            ex_sheet.printStackTrace();
            System.exit(3);
        }
        return null;
    }
    private List<Jira> leerGiras(XSSFSheet sheet){

        //VARIABLES
        List<Jira> jiras = new ArrayList<>();
        int filaInicial = 2;        // fila incial 3 del excel

        // Calcular cuantas filas tiene esta pestaña
        int numFilas = sheet.getLastRowNum();     // numFilas = 5 ==> Fila 1,2,3,4,5 y 6
        // Recorremos cada fila
        for (int f = filaInicial; f <= numFilas; f++) {
            Row fila = sheet.getRow(f);       // Trae la fila de la iteracion ´f´

            //Filtramos segun validaciones. EJ: si los campos estan vacios ; o en caso de ser cerrada y != rechazada.
            String validacion = validarFila(fila);
            if(validacion.equals("break"))break;
            if(validacion.equals("continue"))continue;

            Jira jira = new Jira();       // Carga los atributos del jira

            jira.setID(fila.getCell(ID).getStringCellValue());

            // Index del jira en el excel
            jira.getJiraHojaCalculo().setIndexFila(f);

            jiras.add(jira);
        }

        return jiras;
    }
    private String validarFila(Row fila){
        try {
            // Valida si es la ultima fila de la tabla, en tal caso sale del for.
            if (    (fila.getCell(ID) == null                 || fila.getCell(ID).getStringCellValue().isEmpty())             &&
                    (fila.getCell(fechaCreado) == null        || fila.getCell(fechaCreado).getNumericCellValue() <= 0)        &&
                    (fila.getCell(fechaActualizado) == null   || fila.getCell(fechaActualizado).getNumericCellValue() <= 0)   &&
                    (fila.getCell(fechaResuelto) == null      || fila.getCell(fechaResuelto).getNumericCellValue() <= 0)      &&
                    (fila.getCell(nombre) == null             || fila.getCell(nombre).getStringCellValue().isEmpty())         &&
                    (fila.getCell(prioridad) == null          || fila.getCell(prioridad).getStringCellValue().isEmpty())      &&
                    (fila.getCell(resolucion) == null         || fila.getCell(resolucion).getStringCellValue().isEmpty())     &&
                    (fila.getCell(estado) == null             || fila.getCell(estado).getStringCellValue().isEmpty())
            ) return "break";

            // Valida si el campo ID es nulo, en tal caso pasa a la proxima iteracion.
            if (fila.getCell(ID) == null || fila.getCell(ID).getStringCellValue().isEmpty()) return "continue";

            // filtrar si el estado es cerrada o rechazada, pasa a la proxima iteracion.
            if (fila.getCell(estado) != null && !fila.getCell(estado).getStringCellValue().isEmpty())
                if (fila.getCell(estado).getStringCellValue().equals("Cerrada") || fila.getCell(estado).getStringCellValue().equals("Rechazada"))
                    return "continue";

        }catch (IllegalStateException ex_tipoCelda){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'validarFila()'");
            System.out.println("Fila: " + (fila.getRowNum()+1) +", Error CellType distinto de String, se saltea Jira" + "\n");
            ex_tipoCelda.printStackTrace();
            esperar(1);
            return "continue";
        } catch (Exception ex_general){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'validarFila()'");
            System.out.println("Fila: " + (fila.getRowNum()+1) + ", Error General del metodo validarFila(), se saltea jira" + "\n");
            ex_general.printStackTrace();
            esperar(1);
            return "continue";
        }

        // Si no se cumple ninguno de los casos anteriores, sigue la ejecucion
        return "";
    }




    public void guardarJiras(List<Jira> jiras, String pestania){

        if (jiras.size() == 0)return;

        XSSFSheet sheet = null;
        for (XSSFSheet sheeti: sheets) {
            if (sheeti.toString().equals(pestania)){
                sheet = sheeti;
            }
        }
        if (sheet == null) return;

        for (Jira jira : jiras) {
            XSSFRow fila = sheet.getRow(jira.getJiraHojaCalculo().getIndexFila());

            XSSFCell celdaFechaCreado = fila.createCell(fechaCreado);
            celdaFechaCreado.setCellValue(jira.getFechaCreado());
            styleFechaCorta(celdaFechaCreado);

            XSSFCell celdaFechaActualizado = fila.createCell(fechaActualizado);
            celdaFechaActualizado.setCellValue(jira.getFechaActualizado());
            styleFechaCorta(celdaFechaActualizado);

            XSSFCell celdaFechaResuelto = fila.createCell(fechaResuelto);
            celdaFechaResuelto.setCellValue(jira.getFechaResuelto());
            styleFechaCorta(celdaFechaResuelto);

            XSSFCell celdaNombre = fila.createCell(nombre);
            celdaNombre.setCellValue(jira.getNombre());

            XSSFCell celdaPrioridad = fila.createCell(prioridad);
            celdaPrioridad.setCellValue(jira.getPrioridad());
            centrarTexto(celdaPrioridad);

            XSSFCell celdaResolucion = fila.createCell(resolucion);
            celdaResolucion.setCellValue(jira.getResolucion());
            centrarTexto(celdaResolucion);

            XSSFCell celdaEstado = fila.createCell(estado);
            celdaEstado.setCellValue(jira.getEstado());
            centrarTexto(celdaEstado);
        }

        // Actualizar Cabecera. Ultima vez actualizado el excel
        actualizarCabecera(sheet);

    }
    private void actualizarCabecera( XSSFSheet sheet){
        try {
            // VARIABLES
            final short colorFondo = IndexedColors.GREEN.getIndex();
            final short colorLetra = IndexedColors.GREY_25_PERCENT.getIndex();
            final boolean negrita = true;

            //Actualizar fecha actualizado
            XSSFRow filaCabecera = sheet.getRow(0);                       // rownum = 0       => fila 1
            filaCabecera.createCell(0).setCellValue(fechaActual());     // colimnindex = 0  => Columna A

            // Cambiar el color de la celda
            XSSFCellStyle style = excel.createCellStyle();
            style.setFillForegroundColor(colorFondo);
            style.setFillPattern(FillPatternType.forInt((short) FillPatternType.SOLID_FOREGROUND.ordinal()));

            // Poner las letras en negrita
            XSSFFont font = excel.createFont();
            font.setBold(negrita);
            font.setColor(colorLetra);
            style.setFont(font);

            // Le asignamos a la celda, el estilo creado
            filaCabecera.getCell(0).setCellStyle(style);
        }catch (Exception ex){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'actualizarCabecera()'");
            System.out.println("Error al actualizar la Cabecera, Style" + "\n");
            ex.printStackTrace();
            esperar(1);
        }
    }
    private String fechaActual(){
        try {
            //VARIABLES
            String dia, mes, years, hora, minutos;
            Calendar fecha = new GregorianCalendar();

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

            // Año
            years = String.valueOf(fecha.get(Calendar.YEAR));

            return "Actualizado " + dia + "/" + mes + "/" + years + " a las " + hora + ":" + minutos + " hs";

        }catch (Exception ex){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'fechaActual()'");
            System.out.println("Error cargar la fecha actual" + "\n");
            ex.printStackTrace();
            esperar(1);
            return "";
        }
    }
    private void styleFechaCorta(XSSFCell celda){
        try {
            XSSFCellStyle style = excel.createCellStyle();
            // Celda tipo personalizado => Fecha Corta
            CreationHelper createHelper = excel.getCreationHelper();
            style.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
            // Centrar celda
            style.setAlignment(HorizontalAlignment.CENTER);
            // Guardar style
            celda.setCellStyle(style);
        }catch (Exception ex){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'styleFechaCorta()'");
            System.out.println("Error al preparar el Style fecha corta" + "\n");
            ex.printStackTrace();
            esperar(1);
        }
    }
    private void centrarTexto(XSSFCell celda){
        try {
            XSSFCellStyle style = excel.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);
            celda.setCellStyle(style);
        }catch (Exception ex){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.excel', clase 'Service_Excel' , metodo 'centrarTexto()'");
            System.out.println("Error al centrar texto, Style" + "\n");
            ex.printStackTrace();
            esperar(1);
        }
    }


    public void guardarArchivo(){
        // Guardar el archivo
        try {
            this.file.close();

            FileOutputStream guardarExcel = new FileOutputStream(ubicacionExcel);
            excel.write(guardarExcel);
            guardarExcel.close();
        } catch (FileNotFoundException e) {
            e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void esperar(long seconds){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
