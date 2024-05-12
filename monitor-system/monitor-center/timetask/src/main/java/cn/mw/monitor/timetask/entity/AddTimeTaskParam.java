package cn.mw.monitor.timetask.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;


@Data
@ApiModel("定时任务主表")
public class AddTimeTaskParam {
    private List<AddTimeTaskParam> dels;

    //主键
    private Integer id;

    //任务名称
    private String taskname;

    //h  w  m y  时  周  月  年
    private String timetype;

    //月
    private String month;

    //周
    private String week;

    //天
    private String day;

    private String hms;

    private Boolean timeCustom;

    //任务计划； 具体时间
    private String plan;

    //任务类型  配置备份
    private String type;

    //上次执行结果
    private String lastResult;
    //上次执行时间
    private Date lastTime;
    //下次执行时间
    private Date afterTime;

    private List<MwTimeTaskConfigMapper> assetsId;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;


    private String className;
    private String method;

    //执行时间规则
    private String cron;

    //是否开启
    private Boolean status;

    //定时间隔
    private String timing;

    //查看关联配置url
    private String selectUrl;

    //查看关联配置查询字段名称
    private String selectId;

    //下载配置类型
    private String configType;

    //执行脚本的内容
    @Size(max = 256, message = "cmds最大长度不能超过256字符！")
    private String cmds;

    @ApiModelProperty("taskType 0.表示自定义定时任务 1.表示系统定时任务")
    private Integer taskType;
}
