package cn.mw.monitor.service.assets.model;

import lombok.Data;

/**
 * @author bkc
 * @date 2020/8/26
 */
@Data
public class LabelDTOModel {

    //标签id
    private Integer labelId;
    //格式:1.文本 2.日期 3.下拉4.其他
    private Integer inputFormat;


}
