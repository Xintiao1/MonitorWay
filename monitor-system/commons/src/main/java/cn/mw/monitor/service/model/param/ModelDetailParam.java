package cn.mw.monitor.service.model.param;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import lombok.Data;

/**
 * @ClassName ModelDetailParam
 * @Description 模型详情参数
 * @Author gengjb
 * @Date 2023/2/12 14:37
 * @Version 1.0
 **/
@Data
public class ModelDetailParam extends BaseDetailParam{

    private String modelIndex;

    private Integer modelId;

    private String groupNodes;

    private String monitorMode;

    private Integer modelInstanceId;

    private String instanceName;

    private String assetsName;


    public void extractFrom(MwTangibleassetsDTO mwTangibleassetsDTO){
        super.extractFrom(mwTangibleassetsDTO);
        this.modelIndex = mwTangibleassetsDTO.getModelIndex();
        this.groupNodes = mwTangibleassetsDTO.getGroupNodes();
        this.modelId = mwTangibleassetsDTO.getModelId();
        this.monitorMode = String.valueOf(mwTangibleassetsDTO.getMonitorMode());
        this.instanceName = mwTangibleassetsDTO.getInstanceName();
        this.modelInstanceId = mwTangibleassetsDTO.getModelInstanceId();
        this.assetsName = mwTangibleassetsDTO.getAssetsName()!=null?mwTangibleassetsDTO.getAssetsName():mwTangibleassetsDTO.getHostName();
    }

}
