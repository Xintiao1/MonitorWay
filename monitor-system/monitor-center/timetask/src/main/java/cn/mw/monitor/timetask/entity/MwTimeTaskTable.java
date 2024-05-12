package cn.mw.monitor.timetask.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
public class MwTimeTaskTable {
    //主键
    private Integer id;

    private String taskname;
    private String timetype;
    private String plan;
    private String type;

    private String lastResult;
    private Date lastTime;
    private Date afterTime;

    //月
    private String month;

    //周
    private String week;

    //天
    private String day;

    private String hms;

    private Boolean timeCustom;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    private String className;
    private String method;
    private String cron;
    private Boolean status;

    private List<MwTimeTaskConfigMapper> assetsId;

    //下载配置类型
    private String configType;

    //执行脚本的内容
    private String cmds;

    //定时间隔
    private String timing;

    //查看关联配置url
    private String selectUrl;

    //查看关联配置查询字段名称
    private String selectId;

}
