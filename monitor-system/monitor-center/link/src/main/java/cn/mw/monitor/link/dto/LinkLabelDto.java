package cn.mw.monitor.link.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author xhy
 * @date 2020/8/3 11:58
 */
@Data
public class LinkLabelDto {

    private Integer labelId;

    private String value;

    private Integer inputFormat;

    private String labelName;

    private Integer dropKey;


    /**
     * 线路id
     */
    private String linkId;

    /**
     * 文本标签值
     */
    private String tagboard;

    /**
     * 日期标签值
     */
    private Date dateTagboard;
}
