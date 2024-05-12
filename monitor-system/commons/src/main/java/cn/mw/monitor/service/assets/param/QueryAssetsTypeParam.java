package cn.mw.monitor.service.assets.param;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/6/2 5:58
 * @Version 1.0
 */
@Data
public class QueryAssetsTypeParam {
    //每个资产类型对应的id
    private int assetsTypeId;
    //对应不同表格数据
    private String tableName;
    //对应无形资产（2），有形资产（1），带外资产（3）
    private Integer tableType;
    /**
     * 配置使能项（1：查询开启配置状态，非1：全部查询）
     */
    private Integer settingEnable;

    private Boolean isFlagStatus;

    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;
}
