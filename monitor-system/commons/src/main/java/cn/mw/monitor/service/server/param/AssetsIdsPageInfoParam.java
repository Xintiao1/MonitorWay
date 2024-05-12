package cn.mw.monitor.service.server.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Map;

/**
 * @author xhy
 * @date 2020/4/30 17:22
 */
@Data
public class AssetsIdsPageInfoParam extends BaseParam {
    //    资产主键id
    @ApiModelProperty("资产主键id")
    private String id;
    //    主机id
    @ApiModelProperty("主机id")
    private String assetsId;
    //    主机id
    @ApiModelProperty("资产ip地址")
    private String assetsIp;
    //    第三方监控服务器id
    @ApiModelProperty("第三方监控服务器id")
    private int monitorServerId;

    @ApiModelProperty("key值为排序的属性对应的监控项名称；value值为正派还是倒排；0为正排；1为倒排")
    private Map<String, Integer> sortInfo;

    @ApiModelProperty("区分返回的告警有哪些")
    private String webMonitorName;

    @ApiModelProperty("拓扑id")
    private String topoId;

    @ApiModelProperty("拓扑节点index")
    private int graphIndex;

    private String fuzzyQuery;

    //接口名称
    private String interfaceName;
    //接口描述
    private String interfaceDescr;
    //接口状态
    private String state;
    //高级标记
    private Boolean alertTag;
    //是否是页面查询
    private boolean isQueryFlag;
}
