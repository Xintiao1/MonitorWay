package cn.mw.monitor.server.serverdto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2021/2/20 9:47
 * @Version 1.0
 * `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
 * `user_id` int(11) NOT NULL COMMENT '用户id',
 * `assets_sub_type_id` int(11) NOT NULL COMMENT '资产子类型',
 * `component_layout` longtext NOT NULL COMMENT '组件布局',
 * `default_flag` tinyint(1) DEFAULT NULL COMMENT '是否为默认布局',
 */
@Data
public class ComponentLayoutDTO {
    @ApiModelProperty("主键")
    private Integer comLayoutId;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("角色id")
    private Integer roleId;
    @ApiModelProperty("资产子类型id")
    private Integer assetsTypeSubId;
    @ApiModelProperty("组件布局")
    private JSONObject componentLayout;
    @ApiModelProperty("是否为默认布局")
    private Boolean defaultFlag;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("修改人")
    private String modifier;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    @ApiModelProperty("模板id")
    private String templateId;
    @ApiModelProperty("标签id")
    private int navigationBarId;
    @ApiModelProperty("资产id")
    private Integer assetsId;
}
