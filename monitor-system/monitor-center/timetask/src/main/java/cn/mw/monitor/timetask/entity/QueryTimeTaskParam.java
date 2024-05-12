package cn.mw.monitor.timetask.entity;


import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@ApiModel("定时任务主表")
@Data
public class QueryTimeTaskParam extends BaseParam {
    //主键
    private Integer id;

    private String taskname;

    private String plan;
    private String type;
    private String lastResult;


    private Date lastTimeEnd;
    private Date lastTimeStart;

    private Date afterTimeEnd;
    private Date afterTimeStart;

    private Date creationDateEnd;
    private Date creationDateStart;

    private Date modificationDateEnd;
    private Date modificationDateStart;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    private String className;
    private String method;
    private String cron;
    private Boolean status;

    //下载配置类型
    private String configType;

    //执行脚本的内容
    private String cmds;


    @ApiModelProperty("taskType 0.表示自定义定时任务 1.表示系统定时任务")
    private Integer taskType = 0;
}
