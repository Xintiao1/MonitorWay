package cn.mw.monitor.hybridclouds.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @Date 2021/6/6
 */
@Data
@ApiModel(value = "查询QueryNewHostParam实体类")
public class QueryNewHostParam extends BaseParam {
    private int monitorServerId;
    private String assetHostId;

    private String ip;//ip
    private String hostId;
    private String flag; //  1级：assetsHost   2级：group   3级：hybridCloud
    private String groupId;
    private String groupName;
    @ApiModelProperty("监控项名称")
    private List<String> itemNames;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;
    //每级对应展示的主机
    @ApiModelProperty("每级对应展示的主机")
    private List<GroupHost> groupList;
    //每级对应展示的混合云
    @ApiModelProperty("每级对应展示的混合云")
    private List<GroupHosts> hcList;
}
