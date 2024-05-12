package cn.mw.monitor.activiti.param;

import lombok.Data;

import java.util.List;

@Data
public class Artificial {
    private int checkTactics;
    private String name;
    private String nodeType;
    private List<String> group;
    private List<String> people;
    private List<String> role;
    private int setCheckPerson;
}
