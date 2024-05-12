package cn.mw.monitor.service.server.param;

import lombok.Data;

/**
 * @ClassName QueryAssetsStatusParam
 * @Description 查询资产状态参数
 * @Author gengjb
 * @Date 2023/5/10 15:47
 * @Version 1.0
 **/
@Data
public class QueryAssetsStatusParam {

    private Integer serverId;

    private String assetsId;

    /**
     * 启动监控状态
     */
    private Boolean monitorFlag;


}
