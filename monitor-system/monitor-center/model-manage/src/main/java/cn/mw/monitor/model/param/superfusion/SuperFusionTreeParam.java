package cn.mw.monitor.model.param.superfusion;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/8/1
 */
@Data
public class SuperFusionTreeParam {
    private String id;
    private String name;
    private String pId;
    private String type;
    private Integer num;
    //(主机、虚拟机)host、（存储）storage
    private String treeType;

    private Integer modelId;
    private Integer modelInstanceId;
    private String instanceName;
    private String modelIndex;
    private String esId;
    public void setName(String name){
        this.name = name;
        this.instanceName = name;
    }
}
