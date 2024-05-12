package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/19 16:21
 * @Version 1.0
 */
@Data
public class FlowNodeDTO {
    private String id;
    private double x;
    private double y;
    //节点的名称
    private String label;
    //节点的属性
    private String clazz;
    //表示当前节点是否正在执行
    private Boolean active;
}
