package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.List;

@Data
public class MwConfigMapper {
    private String id;

    private String assetsId;

    private String assetsName;

    private MwAccountMapper mwAccountMapper;

    private MwTemplateMapper mwTemplateMapper;

    private String timing;


    private int delay=0;
    /**
     * 资产ID列表
     */
    private List<String> assetsIds;
}
