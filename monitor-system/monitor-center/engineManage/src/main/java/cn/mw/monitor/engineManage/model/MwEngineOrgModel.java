package cn.mw.monitor.engineManage.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MwEngineOrgModel {
    private Integer id;

    private Integer orgId;

    private Integer engineId;
}
