package cn.mw.monitor.templatemanage.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class QueryTemplateManageParam extends BaseParam {

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

    @ApiModelProperty("创建开始时间")
    private Date createDateStart;
    @ApiModelProperty("创建结束时间")
    private Date createDateEnd;
    @ApiModelProperty("更新开始时间")
    private Date modificationDateStart;
    @ApiModelProperty("更新结束时间")
    private Date modificationDateEnd;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改人")
    private String modifier;

    private String perm;
    private Integer userId;
    private List<Integer> groupIds;
    private List<Integer> orgIds;

    //下载配置类型
    private String downloadType;

    private Boolean isAdmin;

    /**
     * 模糊查询字段
     */
    private String fuzzyQuery;
}
