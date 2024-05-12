package cn.mw.monitor.model.param.virtual;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/15
 */
@Data
public class QueryVirtualInstanceParam  extends BaseParam {
    @ApiModelProperty("虚拟化设备类型")
    private String virtualType;
    @ApiModelProperty("虚拟化设备Id")
    private String virtualId;
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("虚拟设备名称")
    private String virtualName;
    @ApiModelProperty("监控项名称")
    private String nameInfo;
    @ApiModelProperty("监控项名称")
    private List<String> nameInfos;
    @ApiModelProperty("应用集列表")
    private List<String> groupInfo;
    @ApiModelProperty("查询间隔：最近一小时为实时查询，间隔20S，其余为历史查询，间隔300S")
    private Integer interval;
    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month 5:自定义")
    private Integer dateType;
    @ApiModelProperty("当时间类型为 5:自定义时,开始时间")
    private String  dateStart;
    @ApiModelProperty("当时间类型为 5:自定义时,结束时间")
    private String dateEnd;
    private String format;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;

    @ApiModelProperty("hostSystem or VirtualMachine,区分显示的类型")
    private String queryType;
    @ApiModelProperty("是否纳管")
    private Boolean isConnect;
    //导出的字段
    private List<String> header;
    //导出的字段名
    private List<String> headerName;

    @ApiModelProperty("关联依附实例Id")
    private Integer relationInstanceId;

    //左侧树分组类型
    private Integer groupType;

    public void extractFrom(AddAndUpdateModelInstanceParam addAndUpdateModelInstanceParam){
        this.modelId = addAndUpdateModelInstanceParam.getModelId();
        this.modelInstanceId = addAndUpdateModelInstanceParam.getInstanceId();
        this.modelIndex = addAndUpdateModelInstanceParam.getModelIndex();
    }

    private String url;
    private String userName;
    private String password;
    private Integer monitorServerId;
    private String monitorServerName;

}
