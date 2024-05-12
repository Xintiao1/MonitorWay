package cn.mw.monitor.service.systemLog.param;

import lombok.Data;

import java.util.List;

@Data
public class EditLogParam {
    //模块名
    private String modelName;
    //对象名
    private String objName;
    //被修改数据集
    private List<UpdateAttribute> updateData;
}
