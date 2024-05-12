package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class MwTemplateMapper {

    //自增序列
    private Integer id;
    //模板
    private String template;
    //模板主键主键
    private Integer templateId;
    //资产主键
    private String assetsId;

    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("修改人")
    private String modifier;

}
