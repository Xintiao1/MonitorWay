package cn.mw.monitor.service.topo.api;

public interface MwTopoGraphDBService {
    void addTopoToGraphDB(String topoId ,String topoGraph ,GraphDBCallback graphDBCallback) throws Exception;
    String listTopoToGraphDB() throws Exception;
    void removeTopoFromGraphDB(String topoId ,GraphDBCallback graphDBCallback);
}
