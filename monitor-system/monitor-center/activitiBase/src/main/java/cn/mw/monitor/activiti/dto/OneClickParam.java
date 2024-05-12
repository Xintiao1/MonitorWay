package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/10/14 11:51
 * @Version 1.0
 */
@Data
public class OneClickParam {
    /**
     * 一键通过从前台接收的参数
     */
    private List<BatchPassParam> batchPassParamList;
    /**
     * 通过或者驳回状态
     */
    private Boolean passFlag;
    /**
     * 驳回意见
     */
    private String rejectedReason;
}
