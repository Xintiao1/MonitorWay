package cn.mw.monitor.timetask.entity;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
@ApiModel("定时任务下载记录表")
public class MwTimeTaskDownloadHis {
    //主键
    private Integer id;

    //执行时间
    private Date downtime;

    //任务名称
    private String downresult;

    //定时任务主键
    private Integer timeId;

    private String path;

    private String name;

    private String context;

}
