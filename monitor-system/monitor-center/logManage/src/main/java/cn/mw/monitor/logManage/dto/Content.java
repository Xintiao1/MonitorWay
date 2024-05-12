package cn.mw.monitor.logManage.dto;

import lombok.Data;

@Data
public class Content {

    private String srcField;
    private String targetField;
    private String fieldType;
    private String dictionaryName;


    public Content() {}

    /**
     * 记录类
     *
     * @param srcField       源字段
     * @param targetField    目标字段
     * @param fieldType      字段类型
     * @param dictionaryName 字典名
     */
    public Content(String srcField, String targetField, String fieldType, String dictionaryName) {
        this.srcField = srcField;
        this.targetField = targetField;
        this.fieldType = fieldType;
        this.dictionaryName = dictionaryName;
    }

}
