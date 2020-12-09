package adapter;

import entity.Jira;
import services.drive.ServiceDrive;

import java.util.List;
import java.util.Map;

public class DriveAdapter implements HojaCalculo {

    ServiceDrive drive;

    public DriveAdapter(ServiceDrive drive){
        this.drive = drive;
    }

    @Override
    public Map<String, List<Jira>> read() {
        return drive.leerSheets();
    }

    @Override
    public void save(Map<String, List<Jira>> sheets) {
            drive.guardarjiras(sheets);
    }
}
