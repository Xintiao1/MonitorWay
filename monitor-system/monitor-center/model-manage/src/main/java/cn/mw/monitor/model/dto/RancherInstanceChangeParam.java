package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;
import lombok.Data;

import java.util.List;

@Data
public class RancherInstanceChangeParam {
    //es中是否存在Rancher数据
    private boolean hasRancherData = false;
    private List<MwModelRancherDataInfoDTO> deleteDatas;
    private List<MwModelRancherDataInfoDTO> updateDatas;
    private List<MwModelRancherDataInfoDTO> addDatas;
    private List<MwModelRancherDataInfoDTO> pIdDatas;

}
