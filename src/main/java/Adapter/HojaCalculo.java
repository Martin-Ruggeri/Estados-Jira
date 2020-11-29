package Adapter;

import entity.Jira;

import java.util.List;
import java.util.Map;

public interface HojaCalculo {

    public Map<String, List<Jira>> read();

    public void save(Map<String, List<Jira>> sheets);
}
