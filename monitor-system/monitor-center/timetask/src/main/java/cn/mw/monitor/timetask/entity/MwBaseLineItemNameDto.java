package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

/**
 * @ClassName MwBaseLineItemNameDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/6 10:38
 * @Version 1.0
 **/
@Data
public class MwBaseLineItemNameDto extends BaseParam {

    //基线监控项ID
    private int id;

    //监控项名称
    private String name;

    //监控项ItemName
    private String itemName;

    //主机宏
    private String macro;
}
