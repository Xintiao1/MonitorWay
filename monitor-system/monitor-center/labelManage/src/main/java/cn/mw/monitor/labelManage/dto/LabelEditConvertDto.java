package cn.mw.monitor.labelManage.dto;

import cn.mw.monitor.dropDown.api.param.AddDropDownParam;
import lombok.Data;

/**
 * @ClassName LabelEditConvertDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/7/22 11:11
 * @Version 1.0
 **/
@Data
public class LabelEditConvertDto {

    /**
     * 插入数据返回的主键
     */
    private Integer id;

    /**
     *  下拉框code
     */

    private String dropCode;
    /**
     *    下拉框key
     */
    private Integer dropKey;

    /**
     * 下拉框value
     */
    private String dropValue;

    /**
     * 标签ID
     */
    private Integer labelId;

    /**
     * 类型ID
     */
    private String typeId;

    /**
     * 模块名
     */
    private String moduleType;
}
