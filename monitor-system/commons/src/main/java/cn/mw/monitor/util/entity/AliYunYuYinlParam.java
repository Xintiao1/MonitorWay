package cn.mw.monitor.util.entity;

import lombok.Data;

/**
 * 阿里云语音参数实体类
 * @author
 *
 */
@Data
public class AliYunYuYinlParam {

    private String ruleId;
    private String accessKeyId;
    private String accessKeySecret;
    private String ttsCode;
    private String calledNumber;
    private String calledShowNumber;
    //0：公共模式；1：专属模式
    private Integer type;

}
