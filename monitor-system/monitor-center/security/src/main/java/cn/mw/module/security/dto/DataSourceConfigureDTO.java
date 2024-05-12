package cn.mw.module.security.dto;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 日志数据源配置DTO
 * @author qzg
 * @date 2021/12/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSourceConfigureDTO extends BaseParam {
    @ApiModelProperty("id")
    private String id;
    @ApiModelProperty("ids")
    private List<Long> ids;
    @ApiModelProperty("数据源名称")
    private String dataSourceName;
    @ApiModelProperty("数据源类型")
    private Integer dataSourceType;
    @ApiModelProperty("数据源类型名称")
    private String dataSourceTypeName;
    @ApiModelProperty("ip地址")
    private String ip;
    @ApiModelProperty("端口")
    private Integer port;
    @ApiModelProperty("连接类型")
    private Integer connectionType;
    @ApiModelProperty("连接类型名称")
    private String connectionTypeName;
    @ApiModelProperty("是否认证")
    private Boolean isPass;
    @ApiModelProperty("是否认证名称 0否，1是")
    private String isPassName;
    @ApiModelProperty("账号")
    private String userName;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("启用状态 0:不启用 1启用")
    private Integer state;
    @ApiModelProperty("启用状态名称 0:不启用 1启用")
    private String stateName;
    @ApiModelProperty("日志查询索引")
    private String queryEsIndex;
    @ApiModelProperty("删除标志")
    private Boolean deleteFlag;
    @ApiModelProperty("认证方式id")
    private Integer authType;
    @ApiModelProperty("认证方式名称")
    private Integer authTypeName;
    @ApiModelProperty("kafka的topic")
    private List<TopicDTO> topic;
    @ApiModelProperty("运行状态 0:关闭 1启动")
    private Integer status;
    @ApiModelProperty("idsAndTypes")
    private Map<String,Integer> idsAndTypes;
    //模糊查询
    private String fuzzyQuery;
}
