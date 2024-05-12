package cn.mw.monitor.service.rule;

public enum RuleLevel {
    alert("alert", 1),severity("severity", 2), urgent("urgent", 3);

    private String name;
    private int level;

    RuleLevel(String name, int level){
        this.name = name;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}
