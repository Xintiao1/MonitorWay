package cn.mw.monitor.activiti.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2020/10/22 14:50
 * @Version 1.0
 */
@Data
public class RealTimeUpdateDataDTO {
    //被驳回数量
    private Long rejectedCount;
    //已通过数量
    private Long passCount;
    //待审核数量
    private Long auditCount;

    public RealTimeUpdateDataDTO(Long rejectedCount, Long passCount, Long auditCount) {
        this.rejectedCount = rejectedCount;
        this.passCount = passCount;
        this.auditCount = auditCount;
    }

    public RealTimeUpdateDataDTO() {
    }
}
