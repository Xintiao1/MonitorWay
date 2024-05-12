package cn.mw.monitor.common.util;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.common.util.GroupHosts;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/6/30 10:38
 * @Version 1.0
 */

@Data
public class QueryHostParam extends BaseParam {
    private int monitorServerId;
    private String assetHostId;

    //是否纳管（可以跳转到资产详情的为已纳管）
    private Boolean isManage;
    private String ip;//ip
    private String hostId;
    private String storeName;
    //虚拟机名称
    private String vHostName;
    //    用于区分是存储还是虚拟化 "vHost"是虚拟化；"store"是存储
    private String flag;
    @ApiModelProperty(value = "'host':主机 'vHost'：虚拟化 'store'：数据存储")
    private String tableType;

    private String groupId;
    private String groupName;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;
    //每级对应展示的主机
    private List<GroupHosts> hostList;
    //每级对应展示的虚拟机
    private List<GroupHosts> vmList;
    //每级对应展示的存储数据
    private List<GroupHosts> storeList;

    private List<String> header;

    private List<String> headerName;

}
