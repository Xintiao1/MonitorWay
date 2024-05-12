package cn.mw.monitor.service.model.dto.rancher;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/4/15
 */
@Data
public class MwModelRancherNameSpaceDTO {
    private String id;
    private String name;
    private String type;
    //创建时间
    private String created;
    private String uuid;
    private String PId;
    private String state;
    public void setCreated(String created){
        created = created.replaceAll("T"," ").replaceAll("Z","");
        this.created = created;
    }
}
