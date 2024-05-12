package cn.mw.monitor.activiti.model;

import cn.mw.monitor.user.model.MwModule;
import lombok.Data;

@Data
public class ModuleView {
    public static final String ID_SEP = "-";

    private String nodeId;
    private String nodeName;
    private String nodeProtocol;

    public void extractFromMwModule(MwModule mwModule){
        this.nodeId = mwModule.getId().toString();
        this.nodeName = mwModule.getModuleName();
        this.nodeProtocol = mwModule.getNodeProtocol();
    }

    public ModuleView(){

    }

    public ModuleView(String nodeId, String nodeName, String nodeProtocol){
        this.nodeId = nodeProtocol + ID_SEP + nodeId;
        this.nodeName = nodeName;
        this.nodeProtocol = nodeProtocol;
    }
}
