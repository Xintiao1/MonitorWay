package cn.mw.monitor.assets.dto;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/7/25 11:23
 * @Version 1.0
 */
@Data
public class AssetsLabelDTO {
    //资产id
    private String assetsId;
    //二级标签
    private String secondLabel;
    //一级标签
    private String firstLabel;
    //一级加二级总称
    private String totalLabel;
}
