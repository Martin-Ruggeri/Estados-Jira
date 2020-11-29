package services.selenium;


import entity.Jira;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;




public class ServiceSelenium {

    // VARIABLES GLOBALES
    private WebDriver driver;

    public void ingresarPagina(){
        // VARIABLES
        String chromePath =  System.getProperty("user.dir") + "\\Drivers\\chromedriver.exe";          // System.getProperty("user.dir") : nos devuelve la ubicacion actual del proyecto
        String URL = "https://steps.everis.com/jiraesp1/secure/Dashboard.jspa"; // Página de Jira

        // Localizar el archivo chromedriver.exe en la propiedad webdriver.chrome.driver
        System.setProperty("webdriver.chrome.driver" , chromePath);

        try {

            // Abrir el navegador
            this.driver = new ChromeDriver();
            this.driver.manage().window().maximize();   // Maximiza la ventana del navegador

            // Direcciona a la pagina de la URL
            this.driver.get(URL);
        }catch (InvalidArgumentException ex_url){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'ingresarPagina()'");
            System.out.println("La pagina ´" + URL + "´, No se encuentra. Verificar la URL" + "\n");
            cerrarPagina();
            ex_url.printStackTrace();
            System.exit(2);

        }catch (IllegalStateException ex_chrome){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'ingresarPagina()'");
            System.out.println("El chromePath '" + chromePath + "', No se encuentra disponible. Verificar ubicacion del chrome driver" + "\n");
            ex_chrome.printStackTrace();
            System.exit(2);
        }
    }

    public void loguearse(){
        // VARIABLES
        String user = "marugger";
        String password = "2019MaRu";

        try {
            // Cambiar de iframe del formulario
            this.driver.switchTo().frame("gadget-0");

            // Vaciar los campos usuario y contraseña.
            this.driver.findElement(By.xpath("/html/body/div[2]/div/div/form/div[1]/input")).clear();
            this.driver.findElement(By.xpath("//input[@id = 'login-form-password']")).clear();

            //Cargar formulario (Usuario y Contraseña)
            this.driver.findElement(By.xpath("/html/body/div[2]/div/div/form/div[1]/input")).sendKeys(user);
            this.driver.findElement(By.xpath("//input[@id = 'login-form-password']")).sendKeys(password);

            // Hacer click en el boton Iniciar Sesion
            this.driver.findElement(By.xpath("//input[@id = 'login']")).click();

            // Volver al frame default
            this.driver.switchTo().defaultContent();

        }catch (NoSuchFrameException ex_frame){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'loguearse()'");
            System.out.println("No se encuentra el frame, revisar el id del Frame " + "\n");
            cerrarPagina();
            ex_frame.printStackTrace();
            System.exit(2);
        } catch (NoSuchElementException ex_NWE){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'loguearse()'");
            System.out.println("No se encontro el WebElement" + "\n");
            cerrarPagina();
            ex_NWE.printStackTrace();
            System.exit(2);
        }catch (Exception ex){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'loguearse()'");
            System.out.println("Excepcion general del metodo loguearse()" + "\n");
            cerrarPagina();
            ex.printStackTrace();
            System.exit(2);
        }
    }

    public void esperarPagina(long miliSeconds, String Xpath){
            double tiempoEsperadoAcumulado =  0;

        for (int i = 0; i < 10 ; i++) {
            try {
                Thread.sleep(miliSeconds);
                if (Xpath.equals(""))break;
                this.driver.findElement(By.xpath(Xpath));
                break;
            }catch (InterruptedException ex_sleep) {
                System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'esperarPagina()'");
                System.out.println("Error mientras esperaba, metodo Thread.sleep()" + "\n");
                break;
            } catch (NoSuchElementException e) {
                tiempoEsperadoAcumulado += miliSeconds/1000.0;
                System.out.println("La pagina todavia no carga :" + tiempoEsperadoAcumulado + " Segundos");
            }

        }
    }

    public void esperar(long seconds){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex_sleep) {
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'esperar()'");
            System.out.println("Error mientras esperaba, metodo Thread.sleep()" + "\n");
        }
    }

    public boolean actualizarEstado(Jira jira) {
        try {
            // Buscar un Jira (Ej: busca el jira EHCOSPROD-34144)
            this.driver.findElement(By.xpath("//input[contains(@id,\"quickSearchInput\")]")).sendKeys(jira.getID());
            esperarPagina(500, "");
            this.driver.findElement(By.xpath("//input[contains(@id,\"quickSearchInput\")]")).sendKeys(Keys.ENTER);
            esperarPagina(500, "//a[contains(@data-issue-key,\"" + jira.getID() + "\")]");

            // Valida que el ID del excel concuerde con el ID de la pagina
            try {
                this.driver.findElement(By.xpath("//a[contains(@data-issue-key,\"" + jira.getID() + "\")]"));
            }catch (NoSuchElementException ex_id){
                System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'actualizarEstado()'");
                System.out.println("Fila: " + (jira.getJiraHojaCalculo().getIndexFila()+1) + ", Jira: " + jira.getID() + ", No coincide el ID del excel con el ID de la pagina");
                System.out.println("Se procede a eliminar de la lista de Jiras a actualizar" + "\n");
                return false;
            }

            // Busca y Carga Nombre
            jira.setNombre(this.driver.findElement(By.xpath("//h1[contains(@id,\"summary-val\")]")).getText());

            // Buscar y Cargar Prioridad
            jira.setPrioridad(this.driver.findElement(By.xpath("//span[contains(@id,\"priority-val\")]")).getText());

            // Buscar y Cargar Estado
            jira.setEstado(this.driver.findElement(By.xpath("//span[contains(@id,\"status-val\")]")).getText());

            // Buscar y Cargar Resolucion
            jira.setResolucion(this.driver.findElement(By.xpath("//span[contains(@id,\"resolution-val\")]")).getText());

            // Buscar y Cargar Fecha de Creado
            jira.setFechaCreado(string_a_calendar(this.driver.findElement(By.xpath("//dt[contains(text(),\"Creada:\")]//following-sibling::*[1]")).getAttribute("title").substring(1, 11)));

            // Buscar y Cargar, si existe, la Fecha de Actualizacion
            try {
                jira.setFechaActualizado(string_a_calendar(this.driver.findElement(By.xpath("//dt[contains(text(),\"Actualizada:\")]//following-sibling::*[1]")).getAttribute("title").substring(1, 11)));
            } catch (NoSuchElementException ex_FA) {
                System.out.println("Jira: " + jira.getID() + ", No tiene Fecha Ultima actualizacion");
            }

            // Buscar y Cargar, si existe, la Fecha de Resuelto
            try {
                jira.setFechaResuelto(string_a_calendar(this.driver.findElement(By.xpath("//dt[contains(text(),\"Resuelta:\")]//following-sibling::*[1]")).getAttribute("title").substring(1, 11)));
            } catch (NoSuchElementException ex_FR) {
                System.out.println("Jira: " + jira.getID() + ", No tiene Fecha Resuelto");
            }

        }catch (NoSuchElementException ex_ENE){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'actualizarEstado()'");
            System.out.println("Fila: " + (jira.getJiraHojaCalculo().getIndexFila()+1) + ", Jira: " + jira.getID() + ", Actualizacion incompleta, se elimina de la lista de Jiras a actualizar" + "\n");
            ex_ENE.printStackTrace();
            esperar(1);
            return false;
        }
        return true;
    }
    private Calendar string_a_calendar(String fecha){
        try {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            calendar.setTime(sdf.parse(fecha));
            return calendar;
        } catch (ParseException ex_FS) {
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'string_a_calendar()'");
            System.out.println("El String: " + fecha + ", no se puede pasar a Calendar" + "\n");
            ex_FS.printStackTrace();
            esperar(1);
            return null;
        }
    }

    public void cerrarPagina(){
        try {
            driver.close();
        }catch (NullPointerException e){
            System.out.println("\n" + "ERROR en 'org.everis.jira.service.selenium', clase 'Service_Selenium_Jira' , metodo 'cerrarPagina()'");
            System.out.println("El navegador no se peude cerrar" + "\n");
        }
    }

}