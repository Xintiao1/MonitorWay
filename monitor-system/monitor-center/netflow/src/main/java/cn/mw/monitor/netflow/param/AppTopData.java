package cn.mw.monitor.netflow.param;

import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className AppTopData
 * @description 应用TOP数据
 * @date 2022/9/1
 */
@Data
public class AppTopData extends NetFlowTopData {

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 端口列表
     */
    private List<AppTopData> childList;


}
