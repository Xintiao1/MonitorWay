package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.List;
import java.util.Map;


/**
 * @author qzg
 * @date 2021/12/06
 */
@Data
public class ModelExportSendEmailParam {
    //表头字段
    private List<String> lable;
    //表头字段名
    private List<String> lableName;
    //导出数据
    private List<Map<String, Object>> listMap;
    //模型id
    private Integer modelId;
    //导出文件路径
    private String fieldPath;
}
