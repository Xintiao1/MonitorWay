package cn.mw.monitor.ipaddressmanage.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/8/28 11:58
 */
@Data
public class LinkLabel {

    private Integer labelId;

    private String value;

    private Integer inputFormat;

    private String labelName;

    private Integer dropKey;

    private Integer linkId;

    //文本标签值
    private String tagboard;

    //日期标签值
    private Date dateTagboard;
}
