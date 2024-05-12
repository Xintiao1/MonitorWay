package cn.mw.monitor.es.action;

public interface EsUpdateData {
    String getField();
    String getType();
    String getId();
    Object getValue();
}
