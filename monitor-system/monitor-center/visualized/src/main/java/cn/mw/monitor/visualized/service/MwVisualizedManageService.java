package cn.mw.monitor.visualized.service;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.visualized.dto.MwVisualizedClassifyDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleBusinSatusDto;
import cn.mw.monitor.visualized.dto.MwVisualizedViewDto;
import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.param.MwVisualizedZkSoftWareParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @ClassName MwVisualizedManageService
 * @Author gengjb
 * @Date 2022/4/21 15:06
 * @Version 1.0
 **/
public interface MwVisualizedManageService {

    /**
     * 添加视图分类
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    Reply addVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto);

    /**
     * 查询视图分类
     * @return
     */
    Reply selectVisualizedClassify();

    /**
     * 修改视图分类
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    Reply updateVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto);

    /**
     * 删除视图分类
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    Reply deleteVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto);


    /**
     * 添加视图数据
     * @param visualizedViewDto 视图信息
     * @return
     */
    Reply addVisualizedView(MwVisualizedViewDto visualizedViewDto);

    /**
     * 修改视图数据
     * @param visualizedViewDto 视图信息
     * @return
     */
    Reply updateVisualizedView(MwVisualizedViewDto visualizedViewDto);


    /**
     * 删除视图数据
     * @param visualizedViewDto 视图信息
     * @return
     */
    Reply deleteVisualizedView(MwVisualizedViewDto visualizedViewDto);


    /**
     * 查询视图数据
     * @return
     */
    Reply selectVisualizedView(MwVisualizedViewDto visualizedViewDto);


    /**
     * 查询可视化明细数据
     * @param params
     * @return
     */
    Reply queryVisualizedItem(MwVisualizedIndexQueryParam params);

    /**
     * 查询可视化视图渲染信息
     * @param viewDto
     * @return
     */
    Reply visualizedUpdateQuery(MwVisualizedViewDto viewDto);

    Reply saveVisualizedQueryValue(MwVisualizedIndexQueryParam viewDto);

    Reply getVisualizedQueryValue(Integer id);

    /**
     * 获取中控数据
     * @param param
     * @return
     */
    Reply selectVisualizedZkSoftWare(MwVisualizedZkSoftWareParam param);

    /**
     * 可视化中控大屏告警趋势
     * @param param
     * @return
     */
    Reply selectVisualizedZkSoftWareAlertTrend(MwVisualizedZkSoftWareParam param);

    /**
     * 获取模型实例信息
     * @param moduleParam
     * @param queryAssetsState
     * @return
     */
    List<MwTangibleassetsDTO> getModelAssets(MwVisualizedModuleParam moduleParam, Boolean queryAssetsState) throws Exception;


    /**
     * 可视化组件区数据查询
     * @param param
     * @return
     */
    Reply selectVisualizedModule(MwVisualizedModuleParam param);


    /**
     * 获取选择业务信息
     * @return
     */
    Reply getBusinessTreeInfo();


    /**
     * 获取资产类型分组
     * @return
     */
    Reply getAssetsTypeGroup(MwVisualizedModuleParam moduleParam);

    /**
     * 获取下拉数据
     * @return
     */
    Reply gettVisualizedDropDownInfo(MwVisualizedModuleParam moduleParam);

    /**
     * 获取容器选择数据
     * @param moduleParam
     * @return
     */
    Reply getVisualizedContaineDropDown(MwVisualizedModuleParam moduleParam);

    /**
     * 创建可视化业务状态标题分区信息
     * @param businSatusDtos
     * @return
     */
    Reply createVisualizedBusinStatusTitle(List<MwVisualizedModuleBusinSatusDto> businSatusDtos);

    /**
     * 查询可视化业务状态标题分区信息
     * @param
     * @return
     */
    Reply selectVisualizedBusinStatusTitle(MwVisualizedModuleBusinSatusDto businSatusDto);

    /**
     * 查询业务状态下拉
     * @return
     */
    Reply selectVisualizedBusinStatusDropDown();

}
