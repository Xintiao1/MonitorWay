package cn.mw.monitor.service.assets.param;

import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import lombok.Data;

import java.util.List;

/**
 * @ClassName AssetsSearchTermFuzzyParam
 * @Author gengjb
 * @Date 2021/7/24 14:37
 * @Version 1.0
 * 资产列表查询条件模糊搜索的返回参数
 **/
@Data
public class AssetsSearchTermFuzzyParam {

    /**
     * 所有资产ID的集合
     */
    private List<String> assetsIds;

    /**
     * 所有资产名称的集合
     */
    private List<String> assetsName;

    /**
     * 所有资产主机名称的集合
     */
    private List<String> hostName;

    /**
     * 所有资产IP地址的集合
     */
    private List<String> inBandIp;

    /**
     * 所有资产规格的集合
     */
    private List<String> specifications;

    /**
     * 资产类型
     */
    private List<DropdownDTO> assetsTypes;

    /**
     * 资产子类型
     */
    private List<DropdownDTO> assetsSubTypes;

    /**
     * 模糊查询所有字段的条件
     */
    private String value;

    private List<String> fuzzyQuery;

    private boolean assetsIOTFlag;
}
