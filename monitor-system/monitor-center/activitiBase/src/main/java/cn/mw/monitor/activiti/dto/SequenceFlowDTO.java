package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/19 16:36
 * @Version 1.0
 */
@Data
public class SequenceFlowDTO {
    //线的起始节点id
    private String source;
    //线的最终节点id
    private String target;
    //线的属性
    private String clazz;
    //线上的注释
    private String label;
//    //起始节点的连接点
//    private int sourceAnchor;
//    //最终节点的连接点
//    private int targetAnchor;
}
