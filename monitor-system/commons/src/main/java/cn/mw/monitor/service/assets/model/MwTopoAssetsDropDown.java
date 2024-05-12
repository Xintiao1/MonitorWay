package cn.mw.monitor.service.assets.model;

import lombok.Data;

@Data
public class MwTopoAssetsDropDown {
    private String id;
    private String assetsName;
    private String inBandIp;
    private Integer assetsTypeId;

    private String modelInstanceId;

    public void extractFrom(MwTangibleassetsTableView view){
        this.id = view.getId();
        this.assetsName = view.getAssetsName();
        this.inBandIp = view.getInBandIp();
        this.assetsTypeId = view.getAssetsTypeId();
        if(null == view.getModelInstanceId()){
            this.modelInstanceId = view.getId();
        }else{
            this.modelInstanceId = view.getModelInstanceId().toString();
        }
    }
}
