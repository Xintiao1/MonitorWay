package cn.mw.monitor.service.model.param;

import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/9/23
 */
@Data
public class MWModelInstanceFiled {
    //模型属性类型
    private String type;
    //新增时，页面展示的字段
    private List<MwCustomColByModelDTO> data;
    //查看时，页面展示的字段
    private List<MWModelInstancePropertiesFiledDTO> filedDTOS;
}
