package cn.mw.monitor.activiti.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author syt
 * @Date 2020/11/4 14:17
 * @Version 1.0
 */
@Data
public class CommentsDTO {
    //批注人
    private String annotatePeople;
    //批注时间
    private Date annotateTime;
    //批注意见
    private String message;

    public CommentsDTO(String annotatePeople, Date annotateTime, String message) {
        this.annotatePeople = annotatePeople;
        this.annotateTime = annotateTime;
        this.message = message;
    }

    public CommentsDTO() {
    }
}
