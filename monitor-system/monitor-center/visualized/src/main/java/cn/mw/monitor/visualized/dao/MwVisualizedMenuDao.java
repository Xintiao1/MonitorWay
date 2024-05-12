package cn.mw.monitor.visualized.dao;

import cn.mw.monitor.visualized.dto.MwVisualizedAssetsDto;
import cn.mw.monitor.visualized.dto.MwVisualizedChartDto;
import cn.mw.monitor.visualized.dto.MwVisualizedDimensionDto;
import cn.mw.monitor.visualized.dto.MwVisualizedIndexDto;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedMenuDao
 * @Description 可视化分区类型接口
 * @Author gengjb
 * @Date 2022/4/14 10:31
 * @Version 1.0
 **/
public interface MwVisualizedMenuDao {

    /**
     * 查询可视化分区类型数据
     * @return
     */
    List<MwVisualizedChartDto> selectVisualizedChart();


    /**
     * 查询可视化指标数据
     * @return
     */
    List<MwVisualizedIndexDto> selectVisualizedIndex();

    /**
     * 添加可视化分区类型数据
     * @param visualizedChartDto
     * @return
     */
    int addVisualizedChart(MwVisualizedChartDto visualizedChartDto);

    /**
     * 获取资产的品牌列表
     *
     * @return 资产的品牌列表数据
     */
    List<MwVisualizedDimensionDto> selectAssetsVendorList();

    /**
     * 获取资产的资产类型数据列表
     *
     * @return 资产的资产类型数据
     */
    List<MwVisualizedDimensionDto> selectVisualizedAssetsTypeList();


    /**
     * 获取资产的标签数据列表（同一个资产会有多个标签）
     *
     * @return 资产的标签列表数据
     */
    List<MwVisualizedDimensionDto> selectVisualizedAssetsLabelList();

    /**
     * 获取机构下的资产数据信息
     *
     * @return 当前机构下的所有资产数据
     */
    List<MwVisualizedAssetsDto> selectVisualizedAssetsOrgList(Map criteria);

}
