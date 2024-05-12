package cn.mw.monitor.model.param.citrix;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/10/13
 */
@Data
public class MwQueryCitrixParam  extends BaseParam {
    @ApiModelProperty("模型索引")
    private String modelIndex;
    @ApiModelProperty("模型Id")
    private Integer modelId;
    @ApiModelProperty("模型实例Id")
    private Integer modelInstanceId;
    @ApiModelProperty("依附实例Id")
    private Integer relationInstanceId;
    private String type;
    private Boolean isRelationQuery;
    private String url;
    private String userName;
    private String password;
    private String port;
    private Boolean isGSLBQuery;
    private String searchName;


    //导出的字段
    private List<String> header;
    //导出的字段名
    private List<String> headerName;
    //是否定时任务启动
    private Boolean isTimeTask;
}
