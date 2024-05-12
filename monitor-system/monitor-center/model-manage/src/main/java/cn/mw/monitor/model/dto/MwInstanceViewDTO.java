package cn.mw.monitor.model.dto;

import cn.mw.monitor.model.param.AddMwInstanceViewParam;
import cn.mw.monitor.model.param.UpdMwInstanceViewParam;
import lombok.Data;

import java.util.Date;

@Data
public class MwInstanceViewDTO {
    private static String DefaultName = "默认视图";
    private long id;
    private String viewName;
    private Integer instanceId;
    private String creator;
    private Date createTime;
    private String modifier;
    private Date updTime;

    public void extractFromParam(AddMwInstanceViewParam addMwInstanceViewParam){
        this.viewName = addMwInstanceViewParam.getViewName();
        this.instanceId = addMwInstanceViewParam.getInstanceId();
    }

    public void extractFromParam(UpdMwInstanceViewParam updMwInstanceViewParam){
        this.id = updMwInstanceViewParam.getId();
        this.viewName = updMwInstanceViewParam.getViewName();
    }

    public static MwInstanceViewDTO getDefault(){
        MwInstanceViewDTO mwInstanceViewDTO = new MwInstanceViewDTO();
        mwInstanceViewDTO.setId(-1);
        mwInstanceViewDTO.setViewName(DefaultName);
        mwInstanceViewDTO.setInstanceId(-1);
        return mwInstanceViewDTO;
    }
}
