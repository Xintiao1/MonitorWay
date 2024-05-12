package cn.mw.monitor.timetask.entity;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MwBaseLineManageDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/6 10:58
 * @Version 1.0
 **/
@Data
public class MwBaseLineManageDto extends BaseParam {

    //基线ID
    private int id;

    private String name;

    //启用状态
    private int enable;

    //周期类型
    private Integer dateType;

    //监控项ID
    private List<Integer> itemIds;

    //监控项ID字符串
    private String itemIdStr;

    //统计单位
    private String unit;

    //创建人
    private String creator;

    //创建时间
    private Date createDate;

    //修改人
    private String modifier;

    //修改时间
    private Date modificationDate;

    private List<MwBaseLineItemNameDto> itemNameDtos;

    List<Integer> ids;
}
