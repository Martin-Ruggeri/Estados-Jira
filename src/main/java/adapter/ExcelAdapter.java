package adapter;

import entity.Jira;
import services.excel.ServiceExcel;

import java.util.List;
import java.util.Map;

public class ExcelAdapter implements HojaCalculo {

    private ServiceExcel excel;

    public ExcelAdapter(ServiceExcel excel){
        this.excel = excel;
    }

    @Override
    public Map<String, List<Jira>> read() {
        return excel.leerSheets();
    }

    @Override
    public void save(Map<String, List<Jira>> sheets) {
        sheets.forEach((sheet,jiras) -> {    // Por cada pestaña
            excel.guardarJiras(jiras, sheet);
        });

        excel.guardarArchivo();
    }
}
