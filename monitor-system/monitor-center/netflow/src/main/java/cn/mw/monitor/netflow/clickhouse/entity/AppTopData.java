package cn.mw.monitor.netflow.clickhouse.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/8/1
 * @Description 应用TOP数据
 */
@Data
public class AppTopData  extends NetFlowTopData implements Serializable {


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

    public AppTopData() {
        super();
        this.childList = new ArrayList<>();
    }

}
