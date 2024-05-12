package cn.mw.monitor.templatemanage.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


@Data
@ApiModel("模板管理主表")
public class AddTemplateManageParam {
    //主键
    private Integer id;

    //模板名称
    private String template;

    //系统oid
    private String oid;

    //系统描述
    private String desc;

    //主机品牌
    private String brand;

    //主机型号
    private String model;

    //自动匹配
    private Boolean autoMatch;

    //备注
    @Size(max = 256, message = "备注最大长度不能超过256字符！")
    private String note;

    //xml模板
    private String xml;

    //下载配置类型
    private String downloadType;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;
    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

}
