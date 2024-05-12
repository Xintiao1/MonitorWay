package cn.mw.monitor.labelManage.model;

import lombok.Data;

import java.util.Date;

@Data
public class MwAssetslabelTable {

    private Integer id;

    /**
     * 标签id
     */
    private Integer labelId;

    /**
     * 资产id
     */
    private Integer assetsId;

    /**
     * 文本标签值
     */
    private String tagboard;

    /**
     * 日期标签值
     */
    private Date dateTagboard;

}