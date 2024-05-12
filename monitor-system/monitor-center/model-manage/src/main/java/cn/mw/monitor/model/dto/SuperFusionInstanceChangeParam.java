package cn.mw.monitor.model.dto;

import cn.mw.monitor.model.param.superfusion.SuperFusionTreeParam;
import lombok.Data;

import java.util.List;

@Data
public class SuperFusionInstanceChangeParam {
    //es中是否存在SuperFusion数据
    private boolean hasSuperFusionData = false;
    private List<SuperFusionTreeParam> deleteDatas;
    private List<SuperFusionTreeParam> updateDatas;
    private List<SuperFusionTreeParam> addDatas;
    private List<SuperFusionTreeParam> pIdDatas;

}
