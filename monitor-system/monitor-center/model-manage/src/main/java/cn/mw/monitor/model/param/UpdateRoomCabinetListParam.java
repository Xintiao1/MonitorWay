package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/3/07 14:44
 */
@Data
public class UpdateRoomCabinetListParam {
    private String modelIndex;
    private Integer instanceId;
    private String esId;
    private Integer propertiesType;//属性类型
    private String updateFiled; //需要修改的字段
    private Object dataInfo; //需要修改的值
}
