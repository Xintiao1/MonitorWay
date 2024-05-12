package cn.mw.monitor.assets.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class AssetsComponentLayoutDTO {
    @ApiModelProperty("主键")
    private Integer id;
    @ApiModelProperty("用户id")
    private Integer userId;
    @ApiModelProperty("角色id")
    private Integer roleId;
    @ApiModelProperty("资产子类型id")
    private Integer assetsTypeSubId;
    @ApiModelProperty("资产子类型Name")
    private String assetsTypeSubName;
    @ApiModelProperty("组件布局")
    private String componentLayout;
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
    @ApiModelProperty("标签名称")
    private String navigationBarName;
    @ApiModelProperty("资产id")
    private Integer assetsId;
    private Integer monitorMode;
    private String manufacturer;
    private String specification;
}
