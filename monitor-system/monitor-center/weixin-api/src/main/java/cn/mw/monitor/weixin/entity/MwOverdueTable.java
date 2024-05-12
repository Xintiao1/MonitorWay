package cn.mw.monitor.weixin.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("告警消息过期表")
public class MwOverdueTable extends BaseParam {

    private List<Integer> ids;

    private Integer id;
    private String context;

    private Date conTime;
    private Date conTimeStart;
    private Date conTimeEnd;

    private Date startTime;
    private Date startTimeStart;
    private Date startTimeEnd;

    private Date createDate;
    private Date createDateStart;
    private Date createDateEnd;

    private Date modificationDate;
    private Date modificationDateStart;
    private Date modificationDateEnd;

    private String modifier;
    private Boolean isSend;
    private Boolean deleteFlag;


}
