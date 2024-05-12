package cn.mw.monitor.templatemanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
public class MwQueryTemplateManageTable {
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
    private String note;

    //xml模板
    private String xml;

    //下载配置类型
    private String downloadType;

    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("修改人")
    private String modifier;

    private List<Integer> principal;

    private List<Integer> orgIdss;
    private List<List<Integer>> orgIds = new ArrayList<>();

    private List<Integer> groupIds;


}
